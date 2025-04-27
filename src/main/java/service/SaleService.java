package service;

import dao.impl.SaleDao;
import exception.nsee.SaleNotFoundException;
import exception.StockUpdateException;
import model.Income;
import model.IncomeSource;
import model.MonthlyBudget;
import model.Product;
import model.Sale;
import model.Stock;
import model.User;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SaleService {
    private static SaleService instance;
    private final SaleDao saleDao = new SaleDao();
    private final UserService userService = UserService.getInstance();
    private final ProductService productService = ProductService.getInstance();
    private final StockService stockService = StockService.getInstance();
    private final IncomeService incomeService = IncomeService.getInstance();
    private final MonthlyBudgetService budgetService = MonthlyBudgetService.getInstance();

    private static final Long SALES_INCOME_SOURCE_ID = 1L; // ID источника дохода "Продажи"

    private SaleService() {
    }

    public static synchronized SaleService getInstance() {
        if (instance == null) {
            instance = new SaleService();
        }
        return instance;
    }

    public List<Sale> getAllSales() {
        return findAndValidate(saleDao::findAll, "Продажи не найдены");
    }

    public Sale getSaleById(Long id) {
        return saleDao.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Продажа с ID " + id + " не найдена"));
    }

    public List<Sale> getSalesByProduct(Long productId) {
        productService.getProductById(productId);

        return findAndValidate(() -> saleDao.findByProduct(productId),
                "Продажи товара с ID " + productId + " не найдены");
    }

    public List<Sale> getSalesByCashier(Long cashierId) {
        userService.getUserById(cashierId);

        return findAndValidate(() -> saleDao.findByCashier(cashierId),
                "Продажи, выполненные кассиром с ID " + cashierId + " не найдены");
    }

    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        Timestamp start = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(endDate.atTime(23, 59, 59));

        return findAndValidate(() -> saleDao.findByDateRange(start, end),
                "Продажи за период с " + startDate + " по " + endDate + " не найдены");
    }

    public BigDecimal getAvgSale(BigDecimal amount, List<Sale> sales) {
        return amount.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);
    }

    public Sale addSale(Sale sale) {
        validateSale(sale);
        verifyStockAvailability(sale.getProduct().getId(), sale.getQuantity());
        prepareSaleData(sale);
        Long id = saleDao.save(sale);

        if (id != null) {
            updateStockAfterSale(sale.getProduct().getId(), sale.getQuantity());

            addSaleToIncome(sale);

            LoggerUtil.info("Добавлена новая продажа с ID " + id +
                    " товара " + sale.getProduct().getName() +
                    ", количество: " + sale.getQuantity() +
                    ", сумма: " + sale.getTotalAmount());
        }

        return sale;
    }


    public Sale addSale(Long productId, Integer quantity, LocalDateTime saleDateTime) {
        if (productId == null) {
            throw new IllegalArgumentException("ID товара должен быть указан");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным числом");
        }

        Product product = productService.getProductById(productId);
        User cashier = userService.getCurrentUser();

        BigDecimal totalAmount = product.getSellPrice().multiply(BigDecimal.valueOf(quantity));

        Sale sale = new Sale(
                null,
                product,
                quantity,
                cashier,
                totalAmount,
                saleDateTime != null ? Timestamp.valueOf(saleDateTime) : Timestamp.valueOf(LocalDateTime.now())
        );

        return addSale(sale);
    }


    public boolean updateSale(Sale sale) {
        if (sale.getId() == null) {
            throw new IllegalArgumentException("ID продажи не может быть пустым при обновлении");
        }

        validateSale(sale);

        Sale existingSale = getSaleById(sale.getId());

        handleStockUpdates(existingSale, sale);

        if (sale.getTotalAmount() == null) {
            calculateTotalAmount(sale);
        }

        boolean updated = saleDao.update(sale);
        if (updated) {
            if (existingSale.getTotalAmount().compareTo(sale.getTotalAmount()) != 0) {
                updateSaleIncome(existingSale, sale);
            }

            LoggerUtil.info("Обновлена продажа с ID " + sale.getId() +
                    " товара " + sale.getProduct().getName() +
                    ", количество: " + sale.getQuantity() +
                    ", сумма: " + sale.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось обновить продажу с ID " + sale.getId());
        }

        return updated;
    }

    public boolean deleteSale(Long id) {
        Sale sale = getSaleById(id);

        boolean deleted = saleDao.deleteById(id);
        if (deleted) {
            updateStockAfterSaleReturn(sale.getProduct().getId(), sale.getQuantity());

            deleteSaleIncome(sale);

            LoggerUtil.info("Удалена продажа с ID " + id +
                    " товара " + sale.getProduct().getName() +
                    ", количество: " + sale.getQuantity());
        } else {
            LoggerUtil.warn("Не удалось удалить продажу с ID " + id);
        }

        return deleted;
    }

    private void addSaleToIncome(Sale sale) {
        try {
            IncomeSource salesSource = IncomeSourceService.getInstance().getIncomeSourceById(SALES_INCOME_SOURCE_ID);

            Income income = new Income(
                    null,
                    salesSource,
                    sale.getTotalAmount(),
                    sale.getSaleDate(),
                    sale.getCashier()
            );

            Long incomeId = incomeService.addIncome(income);

            if (incomeId != null) {
                LoggerUtil.info("Добавлен новый доход на основе продажи с ID " + sale.getId() +
                        ", сумма: " + sale.getTotalAmount());

                updateMonthlyBudget(sale.getSaleDate().toLocalDateTime().toLocalDate(), sale.getTotalAmount());
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при добавлении продажи в доходы: " + e.getMessage(), e);
        }
    }

    private void updateSaleIncome(Sale oldSale, Sale newSale) {
        try {
            List<Income> incomes = incomeService.getAllIncomes();

            for (Income income : incomes) {
                if (income.getSource().id().equals(SALES_INCOME_SOURCE_ID) &&
                        income.getIncomeDate().equals(oldSale.getSaleDate()) &&
                        income.getTotalAmount().equals(oldSale.getTotalAmount())) {

                    income.setTotalAmount(newSale.getTotalAmount());
                    income.setIncomeDate(newSale.getSaleDate());

                    boolean updated = incomeService.updateIncome(income);

                    if (updated) {
                        LoggerUtil.info("Обновлен доход на основе продажи с ID " + oldSale.getId() +
                                ", новая сумма: " + newSale.getTotalAmount());

                        updateMonthlyBudget(oldSale.getSaleDate().toLocalDateTime().toLocalDate(), oldSale.getTotalAmount().negate());
                        updateMonthlyBudget(newSale.getSaleDate().toLocalDateTime().toLocalDate(), newSale.getTotalAmount());
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении дохода от продажи: " + e.getMessage(), e);
        }
    }

    private void deleteSaleIncome(Sale sale) {
        try {
            List<Income> incomes = incomeService.getAllIncomes();

            for (Income income : incomes) {
                if (income.getSource().id().equals(SALES_INCOME_SOURCE_ID) &&
                        income.getIncomeDate().equals(sale.getSaleDate()) &&
                        income.getTotalAmount().equals(sale.getTotalAmount())) {

                    boolean deleted = incomeService.deleteIncome(income.getId());

                    if (deleted) {
                        LoggerUtil.info("Удален доход на основе продажи с ID " + sale.getId());

                        updateMonthlyBudget(sale.getSaleDate().toLocalDateTime().toLocalDate(), sale.getTotalAmount().negate());
                    }

                    break;
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при удалении дохода от продажи: " + e.getMessage(), e);
        }
    }

    private void updateMonthlyBudget(LocalDate date, BigDecimal amount) {
        try {
            LocalDate firstDayOfMonth = date.withDayOfMonth(1);

            MonthlyBudget budget;
            try {
                budget = budgetService.getBudgetByDate(firstDayOfMonth);
            } catch (Exception e) {
                LoggerUtil.info("Создание нового месячного бюджета на " + firstDayOfMonth);

                budget = new MonthlyBudget(
                        null,
                        firstDayOfMonth,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        null,
                        userService.getCurrentUser()
                );

                Long budgetId = budgetService.createBudget(budget);
                if (budgetId != null) {
                    budget.setId(budgetId);
                } else {
                    throw new RuntimeException("Не удалось создать месячный бюджет");
                }
            }

            BigDecimal newActualIncome = budget.getActualIncome().add(amount);

            budgetService.updateActualValues(budget.getId(), newActualIncome, budget.getActualExpenses());

            LoggerUtil.info("Обновлен месячный бюджет на " + firstDayOfMonth +
                    ", новый фактический доход: " + newActualIncome);

        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
        }
    }

    private Map<Product, Integer> groupProductsByQuantity(List<Sale> sales, int limit) {
        Map<Product, Integer> productQuantities = new HashMap<>();
        for (Sale sale : sales) {
            Product product = sale.getProduct();
            int currentQuantity = productQuantities.getOrDefault(product, 0);
            productQuantities.put(product, currentQuantity + sale.getQuantity());
        }

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void prepareSaleData(Sale sale) {
        if (sale.getSaleDate() == null) {
            sale.setSaleDate(Timestamp.valueOf(LocalDateTime.now()));
        }

        if (sale.getCashier() == null) {
            sale.setCashier(userService.getCurrentUser());
        }

        if (sale.getTotalAmount() == null) {
            calculateTotalAmount(sale);
        }
    }

    private void handleStockUpdates(Sale existingSale, Sale updatedSale) {
        boolean productChanged = !existingSale.getProduct().getId().equals(updatedSale.getProduct().getId());
        boolean quantityChanged = !existingSale.getQuantity().equals(updatedSale.getQuantity());

        if (!productChanged && !quantityChanged) {
            return;
        }

        if (productChanged) {
            handleProductChange(existingSale, updatedSale);
        } else {
            handleQuantityChange(existingSale.getProduct().getId(),
                    existingSale.getQuantity(), updatedSale.getQuantity());
        }
    }

    private void handleProductChange(Sale existingSale, Sale updatedSale) {
        updateStockAfterSaleReturn(existingSale.getProduct().getId(), existingSale.getQuantity());

        verifyStockAvailability(updatedSale.getProduct().getId(), updatedSale.getQuantity());

        updateStockAfterSale(updatedSale.getProduct().getId(), updatedSale.getQuantity());
    }

    private void handleQuantityChange(Long productId, int oldQuantity, int newQuantity) {
        int quantityDifference = newQuantity - oldQuantity;

        if (quantityDifference > 0) {
            verifyStockAvailability(productId, quantityDifference);
        }

        updateStockAfterSaleUpdate(productId, quantityDifference);
    }

    private void verifyStockAvailability(Long productId, int requiredQuantity) {
        Stock stock = stockService.getStockByProductId(productId);
        if (stock.getQuantity() < requiredQuantity) {
            throw new IllegalStateException("Недостаточно товара на складе. Доступно: " +
                    stock.getQuantity() + ", требуется: " + requiredQuantity);
        }
    }

    private void calculateTotalAmount(Sale sale) {
        Product product = productService.getProductById(sale.getProduct().getId());

        sale.setTotalAmount(product.getSellPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
    }

    private void updateStockAfterSale(Long productId, Integer quantity) {
        try {
            Stock stock = stockService.getStockByProductId(productId);

            int newQuantity = stock.getQuantity() - quantity;

            stockService.updateStockQuantity(productId, newQuantity);

            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    " на складе после продажи: " + newQuantity);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении склада после продажи: " + e.getMessage(), e);
            throw new StockUpdateException("Ошибка при обновлении склада после продажи");
        }
    }

    private void updateStockAfterSaleReturn(Long productId, Integer quantity) {
        try {
            Stock stock = stockService.getStockByProductId(productId);

            int newQuantity = stock.getQuantity() + quantity;

            stockService.updateStockQuantity(productId, newQuantity);

            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    " на складе после возврата: " + newQuantity);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении склада после возврата: " + e.getMessage(), e);
            throw new StockUpdateException("Ошибка при обновлении склада после возврата");
        }
    }

    private void updateStockAfterSaleUpdate(Long productId, Integer quantityDifference) {
        try {
            Stock stock = stockService.getStockByProductId(productId);

            int newQuantity = stock.getQuantity() - quantityDifference;

            if (newQuantity < 0) {
                throw new IllegalStateException("Недостаточно товара на складе для обновления");
            }

            stockService.updateStockQuantity(productId, newQuantity);

            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    " на складе после обновления продажи: " + newQuantity);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении склада после обновления продажи: " + e.getMessage(), e);
            throw new StockUpdateException("Ошибка при обновлении склада после обновления продажи");
        }
    }

    private void validateSale(Sale sale) {
        if (sale.getProduct() == null || sale.getProduct().getId() == null) {
            throw new IllegalArgumentException("Товар должен быть указан");
        }

        if (sale.getQuantity() == null || sale.getQuantity() <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным числом");
        }

        productService.getProductById(sale.getProduct().getId());

        if (sale.getCashier() != null && sale.getCashier().getId() != null) {
            userService.getUserById(sale.getCashier().getId());
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания периода должны быть указаны");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
    }

    private List<Sale> findAndValidate(Supplier<List<Sale>> supplier, String errorMessage) {
        List<Sale> sales = supplier.get();

        if (sales.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new SaleNotFoundException(errorMessage);
        }

        return sales;
    }
}