package service.impl;

import dao.impl.SaleDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.nsee.SaleNotFoundException;
import exception.StockUpdateException;
import model.Product;
import model.Sale;
import model.Stock;
import model.User;
import service.ProductService;
import service.SaleService;
import service.StockService;
import service.UserService;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SaleServiceImpl implements SaleService {
    private final SaleDao saleDao = new SaleDao();
    private final UserService userService = new UserServiceImpl();
    private final ProductService productService = new ProductServiceImpl();
    private final StockService stockService = new StockServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";
    private static final String ROLE_CASHIER = "Кассир";

    @Override
    public List<Sale> getAllSales() {
        checkAuthentication();
        return findAndValidate(saleDao::findAll, "Продажи не найдены");
    }

    @Override
    public Sale getSaleById(Long id) {
        checkAuthentication();
        return saleDao.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Продажа с ID " + id + " не найдена"));
    }

    @Override
    public List<Sale> getSalesByProduct(Long productId) {
        checkAuthentication();

        productService.getProductById(productId);

        return findAndValidate(() -> saleDao.findByProduct(productId),
                "Продажи товара с ID " + productId + " не найдены");
    }

    @Override
    public List<Sale> getSalesByCashier(Long cashierId) {
        checkAuthentication();

        userService.getUserById(cashierId);

        return findAndValidate(() -> saleDao.findByCashier(cashierId),
                "Продажи, выполненные кассиром с ID " + cashierId + " не найдены");
    }

    @Override
    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        Timestamp start = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(endDate.atTime(23, 59, 59));

        return findAndValidate(() -> saleDao.findByDateRange(start, end),
                "Продажи за период с " + startDate + " по " + endDate + " не найдены");
    }

    @Override
    public Long addSale(Sale sale) {
        checkCashierPermission();
        validateSale(sale);

        verifyStockAvailability(sale.getProduct().getId(), sale.getQuantity());

        prepareSaleData(sale);

        Long id = saleDao.save(sale);

        if (id != null) {
            updateStockAfterSale(sale.getProduct().getId(), sale.getQuantity());

            LoggerUtil.info("Добавлена новая продажа с ID " + id +
                    " товара " + sale.getProduct().getName() +
                    ", количество: " + sale.getQuantity() +
                    ", сумма: " + sale.getTotalAmount());
        }

        return id;
    }

    @Override
    public Long addSale(Long productId, Integer quantity, LocalDateTime saleDateTime) {
        checkCashierPermission();

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

    @Override
    public boolean updateSale(Sale sale) {
        if (!userService.hasRole(ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только директор может изменять продажи");
        }

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
            LoggerUtil.info("Обновлена продажа с ID " + sale.getId() +
                    " товара " + sale.getProduct().getName() +
                    ", количество: " + sale.getQuantity() +
                    ", сумма: " + sale.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось обновить продажу с ID " + sale.getId());
        }

        return updated;
    }

    @Override
    public boolean deleteSale(Long id) {
        if (!userService.hasRole(ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только администратор или директор может удалять продажи");
        }

        Sale sale = getSaleById(id);

        boolean deleted = saleDao.deleteById(id);
        if (deleted) {
            updateStockAfterSaleReturn(sale.getProduct().getId(), sale.getQuantity());

            LoggerUtil.info("Удалена продажа с ID " + id +
                    " товара " + sale.getProduct().getName() +
                    ", количество: " + sale.getQuantity());
        } else {
            LoggerUtil.warn("Не удалось удалить продажу с ID " + id);
        }

        return deleted;
    }

    @Override
    public BigDecimal getTotalSalesAmount(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        List<Sale> sales;
        try {
            sales = getSalesByDateRange(startDate, endDate);
        } catch (SaleNotFoundException e) {
            return BigDecimal.ZERO;
        }

        return sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<Product, Integer> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        if (limit <= 0) {
            throw new IllegalArgumentException("Лимит должен быть положительным числом");
        }

        List<Sale> sales;
        try {
            sales = getSalesByDateRange(startDate, endDate);
        } catch (SaleNotFoundException e) {
            return new HashMap<>();
        }

        return groupProductsByQuantity(sales, limit);
    }

    @Override
    public Map<User, BigDecimal> getSalesByCashiers(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        List<Sale> sales;
        try {
            sales = getSalesByDateRange(startDate, endDate);
        } catch (SaleNotFoundException e) {
            return new HashMap<>();
        }

        return groupSalesByCashier(sales);
    }

    @Override
    public BigDecimal getDailySalesAverage(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        BigDecimal totalAmount = getTotalSalesAmount(startDate, endDate);

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        return totalAmount.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
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

    private Map<User, BigDecimal> groupSalesByCashier(List<Sale> sales) {
        Map<User, BigDecimal> cashierSales = new HashMap<>();
        for (Sale sale : sales) {
            User cashier = sale.getCashier();
            BigDecimal currentAmount = cashierSales.getOrDefault(cashier, BigDecimal.ZERO);
            cashierSales.put(cashier, currentAmount.add(sale.getTotalAmount()));
        }

        return cashierSales.entrySet().stream()
                .sorted(Map.Entry.<User, BigDecimal>comparingByValue().reversed())
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
        }
        else {
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

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkCashierPermission() {
        checkAuthentication();

        if (!userService.hasRole(ROLE_CASHIER, ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только кассир или директор может управлять продажами");
        }
    }
}