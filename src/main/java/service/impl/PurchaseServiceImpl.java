package service.impl;

import dao.impl.PurchaseDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.PurchaseNotFoundException;
import exception.StockUpdateException;
import model.Product;
import model.Purchase;
import model.Stock;
import model.User;
import service.ProductService;
import service.PurchaseService;
import service.StockService;
import service.UserService;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class PurchaseServiceImpl implements PurchaseService {
    private final PurchaseDao purchaseDao = new PurchaseDao();
    private final ProductService productService = new ProductServiceImpl();
    private final StockService stockService = new StockServiceImpl();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_STOCK_KEEPER = "Кладовщик";
    private static final String ROLE_ACCOUNTANT = "Бухгалтер";
    private static final String ROLE_DIRECTOR = "Директор";


    @Override
    public List<Purchase> getAllPurchases() {
        checkAuthentication();
        return findAndValidate(purchaseDao::findAll, "Закупки не найдены");
    }

    @Override
    public Purchase getPurchaseById(Long id) {
        checkAuthentication();
        return purchaseDao.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException("Закупка с ID " + id + " не найдена"));
    }

    @Override
    public List<Purchase> getPurchasesByProduct(Long productId) {
        checkAuthentication();
        productService.getProductById(productId);

        return findAndValidate(() -> purchaseDao.findByProduct(productId),
                "Закупки для продукта с ID " + productId + " не найдены");
    }

    @Override
    public List<Purchase> getPurchasesByStockKeeper(Long stockKeeperId) {
        checkAuthentication();
        userService.getUserById(stockKeeperId);

        return findAndValidate(() -> purchaseDao.findByStockKeeper(stockKeeperId),
                "Закупки, выполненные кладовщиком с ID " + stockKeeperId + " не найдены");
    }

    @Override
    public List<Purchase> getPurchasesByDateRange(Timestamp startDate, Timestamp endDate) {
        checkAuthentication();
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания периода должны быть указаны");
        }

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        return findAndValidate(() -> purchaseDao.findByDateRange(startDate, endDate),
                "Закупки за период с " + startDate + " по " + endDate + " не найдены");
    }

    @Override
    public Long addPurchase(Purchase purchase) {
        checkStockKeeperPermission();
        validatePurchase(purchase);

        if (purchase.getPurchaseDate() == null) {
            purchase.setPurchaseDate(Timestamp.from(Instant.now()));
        }

        if (purchase.getStockKeeper() == null) {
            purchase.setStockKeeper(userService.getCurrentUser());
        }

        Long purchaseId = purchaseDao.save(purchase);

        if (purchaseId != null) {
            updateStockAfterPurchase(purchase.getProduct().getId(), purchase.getQuantity());

            LoggerUtil.info("Добавлена новая закупка с ID " + purchaseId +
                    " для продукта " + purchase.getProduct().getName() +
                    ", количество: " + purchase.getQuantity());
        }

        return purchaseId;
    }

    @Override
    public Long addPurchase(Long productId, Integer quantity, BigDecimal totalCost) {
        checkStockKeeperPermission();

        if (productId == null) {
            throw new IllegalArgumentException("ID продукта должен быть указан");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным числом");
        }

        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Общая стоимость должна быть положительным числом");
        }

        Product product = productService.getProductById(productId);
        User stockKeeper = userService.getCurrentUser();

        Purchase purchase = new Purchase(
                null,
                product,
                quantity,
                stockKeeper,
                Timestamp.from(Instant.now()),
                totalCost
        );

        return addPurchase(purchase);
    }

    @Override
    public boolean updatePurchase(Purchase purchase) {
        checkStockKeeperPermission();

        if (purchase.getId() == null) {
            throw new IllegalArgumentException("ID закупки не может быть пустым при обновлении");
        }

        validatePurchase(purchase);

        Purchase existingPurchase = getPurchaseById(purchase.getId());

        boolean updated = purchaseDao.update(purchase);

        if (updated) {
            if (!existingPurchase.getQuantity().equals(purchase.getQuantity())) {
                int quantityDifference = purchase.getQuantity() - existingPurchase.getQuantity();

                updateStockAfterPurchaseUpdate(purchase.getProduct().getId(), quantityDifference);
            }

            LoggerUtil.info("Обновлена закупка с ID " + purchase.getId());
        } else {
            LoggerUtil.warn("Не удалось обновить закупку с ID " + purchase.getId());
        }

        return updated;
    }

    @Override
    public boolean deletePurchase(Long id) {
        if (!userService.hasRole(ROLE_ACCOUNTANT, ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только директор или бухгалтер может удалять закупки");
        }

        Purchase purchase = getPurchaseById(id);

        boolean deleted = purchaseDao.deleteById(id);

        if (deleted) {
            updateStockAfterPurchaseDeletion(purchase.getProduct().getId(), purchase.getQuantity());
            LoggerUtil.info("Удалена закупка с ID " + id);
        } else {
            LoggerUtil.warn("Не удалось удалить закупку с ID " + id);
        }

        return deleted;
    }

    @Override
    public BigDecimal getTotalPurchaseCost(Timestamp startDate, Timestamp endDate) {
        checkAuthentication();

        List<Purchase> purchases = getPurchasesByDateRange(startDate, endDate);

        return purchases.stream()
                .map(Purchase::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getTotalPurchasedQuantity(Long productId, Timestamp startDate, Timestamp endDate) {
        checkAuthentication();

        productService.getProductById(productId);

        List<Purchase> purchases;
        try {
            purchases = purchaseDao.findByDateRange(startDate, endDate);
        } catch (PurchaseNotFoundException e) {
            return 0;
        }

        return purchases.stream()
                .filter(p -> p.getProduct().getId().equals(productId))
                .mapToInt(Purchase::getQuantity)
                .sum();
    }


    private void validatePurchase(Purchase purchase) {
        if (purchase.getProduct() == null || purchase.getProduct().getId() == null) {
            throw new IllegalArgumentException("Продукт должен быть указан");
        }

        if (purchase.getQuantity() == null || purchase.getQuantity() <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным числом");
        }

        if (purchase.getTotalCost() == null || purchase.getTotalCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Общая стоимость должна быть положительным числом");
        }
        productService.getProductById(purchase.getProduct().getId());

        if (purchase.getStockKeeper() != null && purchase.getStockKeeper().getId() != null) {
            userService.getUserById(purchase.getStockKeeper().getId());
        }
    }

    private void updateStockAfterPurchase(Long productId, Integer quantity) {
        try {
            Stock stock = stockService.getStockByProductId(productId);

            int newQuantity = stock.getQuantity() + quantity;
            stockService.updateStockQuantity(productId, newQuantity);

            LoggerUtil.info("Обновлено количество товара с ID " + productId + " на складе: " + newQuantity);
        } catch (Exception e) {
            Stock newStock = new Stock(
                    productService.getProductById(productId),
                    quantity,
                    Timestamp.from(Instant.now()),
                    Timestamp.from(Instant.now())
            );

            stockService.addStock(newStock);
            LoggerUtil.info("Добавлен новый товар с ID " + productId + " на склад, количество: " + quantity);
        }
    }

    private void updateStockAfterPurchaseUpdate(Long productId, Integer quantityDifference) {
        try {
            Stock stock = stockService.getStockByProductId(productId);

            int newQuantity = stock.getQuantity() + quantityDifference;

            if (newQuantity < 0) {
                throw new IllegalStateException("Невозможно обновить закупку: недостаточно товара на складе");
            }

            stockService.updateStockQuantity(productId, newQuantity);

            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    " на складе после изменения закупки: " + newQuantity);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении количества товара на складе: " + e.getMessage(), e);
            throw new StockUpdateException("Ошибка при обновлении количества товара на складе");
        }
    }

    private void updateStockAfterPurchaseDeletion(Long productId, Integer quantity) {
        try {
            Stock stock = stockService.getStockByProductId(productId);

            int newQuantity = stock.getQuantity() - quantity;

            if (newQuantity < 0) {
                LoggerUtil.warn("После удаления закупки количество товара с ID " +
                        productId + " стало бы отрицательным. Устанавливаем 0.");
                newQuantity = 0;
            }

            stockService.updateStockQuantity(productId, newQuantity);

            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    " на складе после удаления закупки: " + newQuantity);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении количества товара на складе: " + e.getMessage(), e);
        }
    }

    private List<Purchase> findAndValidate(Supplier<List<Purchase>> supplier, String errorMessage) {
        List<Purchase> purchases = supplier.get();

        if (purchases.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new PurchaseNotFoundException(errorMessage);
        }

        return purchases;
    }

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkStockKeeperPermission() {
        checkAuthentication();

        if (!userService.hasRole(ROLE_STOCK_KEEPER, ROLE_ACCOUNTANT, ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только кладовщик, бухгалтер или директор могут управлять закупками");
        }
    }
}