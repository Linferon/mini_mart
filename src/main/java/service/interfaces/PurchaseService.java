package service.interfaces;

import model.Purchase;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public interface PurchaseService {
    List<Purchase> getAllPurchases();

    Purchase getPurchaseById(Long id);

    List<Purchase> getPurchasesByProduct(Long productId);

    List<Purchase> getPurchasesByStockKeeper(Long stockKeeperId);

    List<Purchase> getPurchasesByDateRange(Timestamp startDate, Timestamp endDate);

    Long addPurchase(Purchase purchase);

    Long addPurchase(Long productId, Integer quantity, BigDecimal totalCost);

    boolean updatePurchase(Purchase purchase);

    boolean deletePurchase(Long id);

    BigDecimal getTotalPurchaseCost(Timestamp startDate, Timestamp endDate);

    int getTotalPurchasedQuantity(Long productId, Timestamp startDate, Timestamp endDate);
}
