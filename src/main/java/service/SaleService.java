package service;

import dao.impl.SaleDao;
import exception.StockUpdateException;
import model.Income;
import model.IncomeSource;
import model.Product;
import model.Sale;
import model.Stock;
import model.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static util.EntityUtil.findAndValidate;
import static util.LoggerUtil.*;
import static util.ValidationUtil.*;

public class SaleService {
    private static final Long SALES_INCOME_SOURCE_ID = 1L;

    private static SaleService instance;
    private final SaleDao saleDao;
    private final UserService userService;
    private final ProductService productService;
    private final StockService stockService;
    private final IncomeService incomeService;
    private final MonthlyBudgetService budgetService;

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

    public List<Sale> getSalesByProduct(Long productId) {
        validateId(productId);
        productService.getProductById(productId);

        return findAndValidate(
                () -> saleDao.findByProduct(productId),
                "Продажи товара с ID " + productId + " не найдены"
        );
    }

    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        Timestamp start = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(endDate.atTime(23, 59, 59));

        return findAndValidate(
                () -> saleDao.findByDateRange(start, end),
                "Продажи за период с " + startDate + " по " + endDate + " не найдены"
        );
    }

    public Sale addSale(Sale sale) {
        validateSale(sale);
        verifyStockAvailability(sale.getProduct().getId(), sale.getQuantity());
        prepareSaleData(sale);

        Long id = saleDao.save(sale);

        updateStockAfterSale(sale.getProduct().getId(), sale.getQuantity());
        addSaleToIncome(sale);
        info("Добавлена новая продажа с ID " + id);
        return sale;
    }

    public Sale addSale(Long productId, Integer quantity, LocalDateTime saleDateTime) {
        validateId(productId);
        validateQuantity(quantity);
        Product product = productService.getProductById(productId);
        User cashier = userService.getCurrentUser();

        BigDecimal totalAmount = calculateTotalAmount(product, quantity);
        Timestamp saleTimestamp = saleDateTime != null
                ? Timestamp.valueOf(saleDateTime)
                : Timestamp.valueOf(LocalDateTime.now());

        Sale sale = new Sale(
                null,
                product,
                quantity,
                cashier,
                totalAmount,
                saleTimestamp
        );

        return addSale(sale);
    }

    private void addSaleToIncome(Sale sale) {
        try {
            IncomeSource salesSource = IncomeSourceService.getInstance()
                    .getIncomeSourceById(SALES_INCOME_SOURCE_ID);

            Income income = new Income(
                    null,
                    salesSource,
                    sale.getTotalAmount(),
                    sale.getSaleDate(),
                    sale.getCashier()
            );

            Long incomeId = incomeService.addIncome(income);

            if (incomeId != null) {
                info("Добавлен новый доход на основе продажи с ID " + sale.getId() +
                        ", сумма: " + sale.getTotalAmount());

                updateBudgetIncome(sale.getSaleDate(), sale.getTotalAmount());
            }
        } catch (Exception e) {
            error("Ошибка при добавлении продажи в доходы: " + e.getMessage(), e);
        }
    }

    private void updateBudgetIncome(Timestamp saleDate, BigDecimal amount) {
        LocalDate date = saleDate.toLocalDateTime().toLocalDate();
        budgetService.updateMonthlyBudgetIncome(date, amount);
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

    private void verifyStockAvailability(Long productId, int requiredQuantity) {
        Stock stock = stockService.getStockByProductId(productId);
        if (stock.getQuantity() < requiredQuantity) {
            throw new IllegalStateException(
                    "Недостаточно товара на складе. Доступно: " + stock.getQuantity() +
                            ", требуется: " + requiredQuantity
            );
        }
    }

    private void calculateTotalAmount(Sale sale) {
        Product product = productService.getProductById(sale.getProduct().getId());
        sale.setTotalAmount(calculateTotalAmount(product, sale.getQuantity()));
    }

    private BigDecimal calculateTotalAmount(Product product, int quantity) {
        return product.getSellPrice().multiply(BigDecimal.valueOf(quantity));
    }

    private void updateStock(Long productId, int quantityChange) {
        try {
            Stock stock = stockService.getStockByProductId(productId);
            int newQuantity = stock.getQuantity() + quantityChange;

            if (newQuantity < 0) {
                throw new IllegalStateException("Недостаточно товара на складе для продажи");
            }

            stockService.updateStockQuantity(productId, newQuantity);
            info("Обновлено количество товара с ID " + productId +
                    " на складе после " + "продажи" + ": " + newQuantity);
        } catch (Exception e) {
            error("Ошибка при обновлении склада после " + "продажи" + ": " + e.getMessage(), e);
            throw new StockUpdateException("Ошибка при обновлении склада после продажи");
        }
    }

    private void updateStockAfterSale(Long productId, Integer quantity) {
        updateStock(productId, -quantity);
    }

    private void validateSale(Sale sale) {
        Objects.requireNonNull(sale, "Объект продажи не может быть null");
        Objects.requireNonNull(sale.getProduct(), "Товар должен быть указан");
        validateQuantity(sale.getQuantity());
        productService.getProductById(sale.getProduct().getId());
        userService.getUserById(sale.getCashier().getId());
    }
}