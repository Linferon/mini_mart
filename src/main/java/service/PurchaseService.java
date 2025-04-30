package service;

import dao.impl.PurchaseDao;
import exception.nsee.PurchaseNotFoundException;
import exception.StockUpdateException;
import model.Product;
import model.Purchase;
import model.Stock;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import static util.EntityUtil.findAndValidate;
import static util.LoggerUtil.*;
import static util.ValidationUtil.*;

public class PurchaseService {
    private static PurchaseService instance;
    private final PurchaseDao purchaseDao;
    private final ProductService productService;
    private final StockService stockService;
    private final UserService userService;
    private final ExpenseService expenseService;
    private final MonthlyBudgetService budgetService;

    private PurchaseService() {
        purchaseDao = new PurchaseDao();
        productService = ProductService.getInstance();
        stockService = StockService.getInstance();
        userService = UserService.getInstance();
        expenseService = ExpenseService.getInstance();
        budgetService = MonthlyBudgetService.getInstance();
    }

    public static synchronized PurchaseService getInstance() {
        if (instance == null) {
            instance = new PurchaseService();
        }
        return instance;
    }

    public Purchase getPurchaseById(Long id) {
        return purchaseDao.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException("Закупка с ID " + id + " не найдена"));
    }

    public List<Purchase> getPurchasesByDateRange(Timestamp startDate, Timestamp endDate) {
        validateDateRange(startDate, endDate);

        return findAndValidate(() -> purchaseDao.findByDateRange(startDate, endDate),
                "Закупки за период с " + startDate + " по " + endDate + " не найдены");
    }

    public void addPurchase(Purchase purchase) {
        validatePurchase(purchase);

        Long purchaseId = purchaseDao.save(purchase);
        info("Добавлена новая закупка с ID " + purchaseId);

        updateStockAfterPurchase(purchase.getProduct().getId(), purchase.getQuantity());

        try {
            expenseService.addPurchaseExpense(purchase.getTotalCost());
            info("Автоматически добавлен расход для закупки ID " + purchaseId);

            LocalDate date = purchase.getPurchaseDate().toLocalDateTime().toLocalDate();
            BigDecimal totalCost = purchase.getTotalCost();

            budgetService.updateMonthlyBudgetExpense(date, totalCost);
        } catch (Exception e) {
            error("Не удалось добавить расход для закупки ID " + purchaseId + ": " + e.getMessage(), e);
        }
    }


    public void addPurchase(Long productId, Integer quantity, BigDecimal totalCost) {
        validateId(productId, "ID продукта должен быть указан");
        validateQuantity(quantity);
        validatePositiveAmount(totalCost, "Общая стоимость должна быть положительным числом");

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

            try {
                BigDecimal costDifference = updatePurchase.getTotalCost().subtract(existingPurchase.getTotalCost());

                expenseService.updatePurchaseExpense(existingPurchase.getTotalCost(), updatePurchase.getTotalCost(), existingPurchase.getPurchaseDate());
                info("Обновлена закупка с ID " + existingPurchase.getId());

                if (costDifference.compareTo(BigDecimal.ZERO) != 0) {
                    budgetService.updateMonthlyBudgetExpense(existingPurchase.getPurchaseDate().toLocalDateTime().toLocalDate(), costDifference);
                }
            } catch (Exception e) {
                error("Не удалось обновить расход для закупки ID " + existingPurchase.getId() + ": " + e.getMessage(), e);
            }
        } else {
            warn("Не удалось обновить закупку с ID " + existingPurchase.getId());
        }
    }

    public void deletePurchase(Long id) {
        Purchase purchase = getPurchaseById(id);
        boolean deleted = purchaseDao.deleteById(id);

        if (deleted) {
            updateStockAfterPurchaseDeletion(purchase.getProduct().getId(), purchase.getQuantity());
            info("Удалена закупка с ID " + id);

            try {
                expenseService.deletePurchaseExpense(purchase.getTotalCost(), purchase.getPurchaseDate());
                budgetService.updateMonthlyBudgetExpense(purchase.getPurchaseDate().toLocalDateTime().toLocalDate(), purchase.getTotalCost().negate());
            } catch (Exception e) {
                error("Не удалось удалить расход для закупки ID " + id + ": " + e.getMessage(), e);
            }
        } else {
            warn("Не удалось удалить закупку с ID " + id);
        }
    }

    private void validatePurchase(Purchase purchase) {
        validateQuantity(purchase.getQuantity());
        validatePositiveAmount(purchase.getTotalCost(), "Общая стоимость должна быть положительным числом");
        productService.getProductById(purchase.getProduct().getId());
        userService.getUserById(purchase.getStockKeeper().getId());
    }

    private void updateStockAfterPurchase(Long productId, Integer quantity) {
        try {
            Stock stock = stockService.getStockByProductId(productId);

            int newQuantity = stock.getQuantity() + quantity;
            stockService.updateStockQuantity(productId, newQuantity);

            info("Обновлено количество товара с ID " + productId + " на складе: " + newQuantity);

        } catch (Exception e) {
            Stock newStock = new Stock(
                    productService.getProductById(productId),
                    quantity
            );

            stockService.addStock(newStock);
            info("Добавлен новый товар с ID " + productId + " на склад, количество: " + quantity);
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
            info("Обновлено количество товара с ID " + productId +
                    " на складе после изменения закупки: " + newQuantity);
        } catch (Exception e) {
            error("Ошибка при обновлении количества товара на складе: " + e.getMessage(), e);
            throw new StockUpdateException("Ошибка при обновлении количества товара на складе");
        }
    }

    private void updateStockAfterPurchaseDeletion(Long productId, Integer quantity) {
        try {
            Stock stock = stockService.getStockByProductId(productId);
            int newQuantity = Math.max(stock.getQuantity() - quantity, 0);
            stockService.updateStockQuantity(productId, newQuantity);

            info("Обновлено количество товара с ID " + productId +
                    " на складе после удаления закупки: " + newQuantity);
        } catch (Exception e) {
            error("Ошибка при обновлении количества товара на складе: " + e.getMessage(), e);
        }
    }
}