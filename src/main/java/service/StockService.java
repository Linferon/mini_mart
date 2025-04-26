package service;

import dao.impl.StockDao;
import exception.nsee.ProductNotFoundException;
import model.Stock;
import util.LoggerUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class StockService {
    private static StockService instance;
    private final StockDao stockDao = new StockDao();
    private final ProductService productService = ProductService.getInstance();

    private StockService() {
    }

    public static synchronized StockService getInstance() {
        if (instance == null) {
            instance = new StockService();
        }
        return instance;
    }

    public Stock getStockByProductId(Long productId) {
        productService.getProductById(productId);
        return stockDao.findByProductId(productId).
                orElseThrow(() -> new ProductNotFoundException("Не было найдено такого продукта на складе!"));
    }

    public List<Stock> getAllStock() {
        return findAndValidate(stockDao::findAll, "Нет продуктов на складе!");
    }

    public List<Stock> getAvailableProducts() {
        return findAndValidate(stockDao::findAvailableProducts, "нет доступных продуктов на складе!");
    }

    public List<Stock> getOutOfStockProducts() {
        return findAndValidate(stockDao::findOutOfStockProducts, "Все товары имеются в наличии.");
    }

    public List<Stock> getLowStockProducts(int threshold) {
        return findAndValidate(() -> stockDao.findLowStockProducts(threshold), "Таких продуктов нет на складе!");
    }

    public void addStock(Stock stock) {
        validateStock(stock);

        productService.getProductById(stock.getProduct().getId());

        Timestamp now = Timestamp.from(Instant.now());
        if (stock.getCreatedAt() == null) {
            stock.setCreatedAt(now);
        }
        if (stock.getUpdatedAt() == null) {
            stock.setUpdatedAt(now);
        }

        Long productId = stockDao.save(stock);
        LoggerUtil.info("Добавлена запись о количестве товара с ID продукта " + productId +
                ", количество: " + stock.getQuantity());
    }

    public void updateStockQuantity(Long productId, Integer quantity) {
        productService.getProductById(productId);

        Stock existingStock = getStockByProductId(productId);

        existingStock.setQuantity(quantity);

        Timestamp now = Timestamp.from(Instant.now());
        existingStock.setUpdatedAt(now);

        boolean updated = stockDao.update(existingStock);

        if (updated) {
            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    ", новое количество: " + quantity);
        } else {
            LoggerUtil.warn("Не удалось обновить количество товара с ID " + productId);
        }
    }

    public boolean deleteStock(Long productId) {
        productService.getProductById(productId);

        boolean deleted = stockDao.deleteByProductId(productId);

        if (deleted) {
            LoggerUtil.info("Удалена запись о количестве товара с ID " + productId);
        } else {
            LoggerUtil.warn("Не удалось удалить запись о количестве товара с ID " + productId);
        }
        return deleted;
    }

    private void validateStock(Stock stock) {
        if (stock.getProduct() == null || stock.getProduct().getId() == null) {
            throw new IllegalArgumentException("Продукт должен быть указан");
        }

        if (stock.getQuantity() == null) {
            throw new IllegalArgumentException("Количество товара должно быть указано");
        }

        if (stock.getQuantity() < 0) {
            throw new IllegalArgumentException("Количество товара не может быть отрицательным");
        }
    }

    private List<Stock> findAndValidate(Supplier<List<Stock>> supplier, String errorMessage) {
        List<Stock> stock = supplier.get();

        if (stock.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new ProductNotFoundException(errorMessage);
        }

        LoggerUtil.info("Количество продуктов на складе: " + stock.size());
        return stock;
    }
}