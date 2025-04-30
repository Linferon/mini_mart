package service;

import dao.impl.StockDao;
import exception.nsee.ProductNotFoundException;
import model.Stock;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static util.DateTimeUtils.setupTimestamps;
import static util.EntityUtil.findAndValidate;
import static util.LoggerUtil.info;
import static util.LoggerUtil.warn;
import static util.ValidationUtil.validateId;
import static util.ValidationUtil.validateQuantity;

public class StockService {
    private static StockService instance;
    private final StockDao stockDao;
    private final ProductService productService;

    private StockService() {
        this(new StockDao(), ProductService.getInstance());
    }

    StockService(StockDao stockDao, ProductService productService) {
        this.stockDao = stockDao;
        this.productService = productService;
    }

    public static synchronized StockService getInstance() {
        if (instance == null) {
            instance = new StockService();
        }
        return instance;
    }

    public Stock getStockByProductId(Long productId) {
        validateId(productId);
        productService.getProductById(productId);

        return stockDao.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Не было найдено такого продукта на складе!"));
    }

    public List<Stock> getAllStock() {
        return findAndValidate(stockDao::findAll, "Нет продуктов на складе!");
    }

    public List<Stock> getAvailableProducts() {
        return findAndValidate(stockDao::findAvailableProducts, "Нет доступных продуктов на складе!");
    }

    public List<Stock> getOutOfStockProducts() {
        return findAndValidate(stockDao::findOutOfStockProducts, "Все товары имеются в наличии.");
    }

    public List<Stock> getLowStockProducts(int threshold) {
        validateQuantity(threshold, "Пороговое значение должно быть положительным");

        return findAndValidate(
                () -> stockDao.findLowStockProducts(threshold),
                "Таких продуктов нет на складе!"
        );
    }

    public void addStock(Stock stock) {
        validateStock(stock);
        setupTimestamps(stock);

        Long productId = stockDao.save(stock);
        info("Добавлена запись о количестве товара с ID продукта " + productId +
                ", количество: " + stock.getQuantity());
    }

    public void updateStockQuantity(Long productId, Integer quantity) {
        productService.getProductById(productId);
        validateQuantity(quantity);

        Stock existingStock = getStockByProductId(productId);
        existingStock.setQuantity(quantity);
        existingStock.setUpdatedAt(Timestamp.from(Instant.now()));

        boolean updated = stockDao.update(existingStock);

        if (updated) {
            info("Обновлено количество товара с ID " + productId +
                    ", новое количество: " + quantity);
        } else {
            warn("Не удалось обновить количество товара с ID " + productId);
        }
    }

    public boolean deleteStock(Long productId) {
        validateId(productId);
        productService.getProductById(productId);

        boolean deleted = stockDao.deleteByProductId(productId);
        if (deleted) {
            info("Удалена запись о количестве товара с ID " + productId);
        } else {
            warn("Не удалось удалить запись о количестве товара с ID " + productId);
        }
        return deleted;
    }

    private void validateStock(Stock stock) {
        requireNonNull(stock, "Объект Stock не может быть null");
        requireNonNull(stock.getProduct(), "Продукт должен быть указан");
        productService.getProductById(stock.getProduct().getId());
        validateQuantity(stock.getQuantity());
    }
}