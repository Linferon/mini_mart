package service;

import dao.impl.SaleDao;
import exception.nsee.SaleNotFoundException;
import exception.StockUpdateException;
import model.Income;
import model.IncomeSource;
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

public class SaleService {
    private static SaleService instance;
    private final SaleDao saleDao;
    private final UserService userService;
    private final ProductService productService;
    private final StockService stockService;
    private final IncomeService incomeService;
    private final MonthlyBudgetService budgetService;

    private static final Long SALES_INCOME_SOURCE_ID = 1L;

    private SaleService() {
        this(new SaleDao(), 
             UserService.getInstance(),
             ProductService.getInstance(),
             StockService.getInstance(),
             IncomeService.getInstance(),
             MonthlyBudgetService.getInstance());
    }

    SaleService(SaleDao saleDao,
                UserService userService,
                ProductService productService,
                StockService stockService,
                IncomeService incomeService,
                MonthlyBudgetService budgetService) {
        this.saleDao = saleDao;
        this.userService = userService;
        this.productService = productService;
        this.stockService = stockService;
        this.incomeService = incomeService;
        this.budgetService = budgetService;
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

    private Income findSaleIncome(Sale sale) {
        try {
            List<Income> incomes = incomeService.getAllIncomes();

            return incomes.stream()
                .filter(income -> income.getSource().id().equals(SALES_INCOME_SOURCE_ID) &&
                                 income.getIncomeDate().equals(sale.getSaleDate()) &&
                                 income.getTotalAmount().equals(sale.getTotalAmount()))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при поиске дохода от продажи: " + e.getMessage(), e);
            return null;
        }
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

                budgetService.updateMonthlyBudgetIncome(sale.getSaleDate().toLocalDateTime().toLocalDate(), sale.getTotalAmount());
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при добавлении продажи в доходы: " + e.getMessage(), e);
        }
    }

    private void updateSaleIncome(Sale oldSale, Sale newSale) {
        try {
            Income income = findSaleIncome(oldSale);

            if (income != null) {
                income.setTotalAmount(newSale.getTotalAmount());
                income.setIncomeDate(newSale.getSaleDate());

                boolean updated = incomeService.updateIncome(income);

                if (updated) {
                    LoggerUtil.info("Обновлен доход на основе продажи с ID " + oldSale.getId() +
                            ", новая сумма: " + newSale.getTotalAmount());

                    budgetService.updateMonthlyBudgetIncome(oldSale.getSaleDate().toLocalDateTime().toLocalDate(), oldSale.getTotalAmount().negate());
                    budgetService.updateMonthlyBudgetIncome(newSale.getSaleDate().toLocalDateTime().toLocalDate(), newSale.getTotalAmount());
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении дохода от продажи: " + e.getMessage(), e);
        }
    }

    private void deleteSaleIncome(Sale sale) {
        try {
            Income income = findSaleIncome(sale);

            if (income != null) {
                boolean deleted = incomeService.deleteIncome(income.getId());

                if (deleted) {
                    LoggerUtil.info("Удален доход на основе продажи с ID " + sale.getId());
                    budgetService.updateMonthlyBudgetIncome(sale.getSaleDate().toLocalDateTime().toLocalDate(), sale.getTotalAmount().negate());
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при удалении дохода от продажи: " + e.getMessage(), e);
        }
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

    private void updateStock(Long productId, int quantityChange, String operationType) {
        try {
            Stock stock = stockService.getStockByProductId(productId);
            int newQuantity = stock.getQuantity() + quantityChange;

            if (newQuantity < 0) {
                throw new IllegalStateException("Недостаточно товара на складе для " + operationType);
            }

            stockService.updateStockQuantity(productId, newQuantity);

            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    " на складе после " + operationType + ": " + newQuantity);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении склада после " + operationType + ": " + e.getMessage(), e);
            throw new StockUpdateException("Ошибка при обновлении склада после " + operationType);
        }
    }

    private void updateStockAfterSale(Long productId, Integer quantity) {
        updateStock(productId, -quantity, "продажи");
    }

    private void updateStockAfterSaleReturn(Long productId, Integer quantity) {
        updateStock(productId, quantity, "возврата");
    }

    private void updateStockAfterSaleUpdate(Long productId, Integer quantityDifference) {
        updateStock(productId, -quantityDifference, "обновления продажи");
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