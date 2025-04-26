package service;

import dao.impl.PurchaseDao;
import exception.nsee.PurchaseNotFoundException;
import exception.StockUpdateException;
import model.Product;
import model.Purchase;
import model.Stock;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class PurchaseService {
    private static PurchaseService instance;
    private final PurchaseDao purchaseDao = new PurchaseDao();
    private final ProductService productService = ProductService.getInstance();
    private final StockService stockService = StockService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final ExpenseService expenseService = ExpenseService.getInstance();
    
    private PurchaseService() {}

    public static synchronized PurchaseService getInstance() {
        if (instance == null) {
            instance = new PurchaseService();
        }
        return instance;
    }
    
    public List<Purchase> getAllPurchases() {
        return findAndValidate(purchaseDao::findAll, "Закупки не найдены");
    }

    
    public Purchase getPurchaseById(Long id) {
        return purchaseDao.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException("Закупка с ID " + id + " не найдена"));
    }
    
    public List<Purchase> getPurchasesByDateRange(Timestamp startDate, Timestamp endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания периода должны быть указаны");
        }

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        return findAndValidate(() -> purchaseDao.findByDateRange(startDate, endDate),
                "Закупки за период с " + startDate + " по " + endDate + " не найдены");
    }

    public void addPurchase(Purchase purchase) {
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

        try {
            expenseService.addPurchaseExpense(purchase.getTotalCost());
            LoggerUtil.info("Автоматически добавлен расход для закупки ID " + purchaseId);
        } catch (Exception e) {
            LoggerUtil.error("Не удалось добавить расход для закупки ID " + purchaseId + ": " + e.getMessage(), e);
        }
    }

    
    public void addPurchase(Long productId, Integer quantity, BigDecimal totalCost) {
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

        Purchase purchase = new Purchase(
                product,
                quantity,
                totalCost
        );

        addPurchase(purchase);
    }
    
    public void updatePurchase(Long purchaseId, Integer quantity) {
        Purchase existingPurchase = getPurchaseById(purchaseId);

        BigDecimal newTotalCost = existingPurchase.getProduct().getBuyPrice().multiply(new BigDecimal(quantity));

        Purchase updatePurchase = new Purchase(
                purchaseId,
                existingPurchase.getProduct(),
                quantity,
                existingPurchase.getStockKeeper(),
                existingPurchase.getPurchaseDate(),
                newTotalCost
        );


        validatePurchase(updatePurchase);
        boolean updated = purchaseDao.update(updatePurchase);

        if (updated) {
            if (!existingPurchase.getQuantity().equals(updatePurchase.getQuantity())) {
                int quantityDifference = updatePurchase.getQuantity() - existingPurchase.getQuantity();
                updateStockAfterPurchaseUpdate(updatePurchase.getProduct().getId(), quantityDifference);
            }

            expenseService.updatePurchaseExpense(existingPurchase.getTotalCost(), updatePurchase.getTotalCost(),  existingPurchase.getPurchaseDate());
            LoggerUtil.info("Обновлена закупка с ID " + existingPurchase.getId());
        } else {
            LoggerUtil.warn("Не удалось обновить закупку с ID " + existingPurchase.getId());
        }

    }

    public void deletePurchase(Long id) {
        Purchase purchase = getPurchaseById(id);

        boolean deleted = purchaseDao.deleteById(id);

        if (deleted) {
            updateStockAfterPurchaseDeletion(purchase.getProduct().getId(), purchase.getQuantity());
            LoggerUtil.info("Удалена закупка с ID " + id);
            expenseService.deletePurchaseExpense(purchase.getTotalCost(), purchase.getPurchaseDate());
        } else {
            LoggerUtil.warn("Не удалось удалить закупку с ID " + id);
        }
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
                    quantity
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
}