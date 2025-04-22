package service;

import model.Stock;

import java.util.List;

public interface StockService {
    Stock getStockByProductId(Long productId);

    List<Stock> getAllStock();

    List<Stock> getOutOfStockProducts();

    List<Stock> getLowStockProducts(int threshold);

    Long addStock(Stock stock);

    boolean updateStockQuantity(Long productId, Integer quantity);

    boolean deleteStock(Long productId);
}
