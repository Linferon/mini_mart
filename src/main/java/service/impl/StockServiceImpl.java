package service.impl;

import dao.impl.StockDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.ProductNotFoundException;
import model.Stock;
import service.ProductService;
import service.StockService;
import service.UserService;
import util.LoggerUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StockServiceImpl implements StockService {
    private final StockDao stockDao = new StockDao();
    private final ProductService productService = new ProductServiceImpl();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_STOCK_KEEPER = "Кладовщик";

    @Override
    public Stock getStockByProductId(Long productId) {
        productService.getProductById(productId);
        return stockDao.findByProductId(productId).
                orElseThrow(() -> new ProductNotFoundException("Не было найдено такого продукта на складе!"));
    }

    @Override
    public List<Stock> getAllStock() {
        return findAndValidate(stockDao::findAll);
    }

    @Override
    public List<Stock> getOutOfStockProducts() {
        List<Stock> allStock = getAllStock();
        return allStock.stream()
                .filter(stock -> stock.getQuantity() <= 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Stock> getLowStockProducts(int threshold) {
        List<Stock> allStock = getAllStock();
        return allStock.stream()
                .filter(stock -> stock.getQuantity() > 0 && stock.getQuantity() < threshold)
                .collect(Collectors.toList());
    }

    @Override
    public Long addStock(Stock stock) {
        checkStockKeeperPermission();

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
        return productId;
    }

    @Override
    public boolean updateStockQuantity(Long productId, Integer quantity) {
        checkStockKeeperPermission();

        productService.getProductById(productId);

        Stock existingStock = getStockByProductId(productId);

        existingStock.setQuantity(quantity);
        existingStock.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        boolean updated = stockDao.update(existingStock);

        if (updated) {
            LoggerUtil.info("Обновлено количество товара с ID " + productId +
                    ", новое количество: " + quantity);
        } else {
            LoggerUtil.warn("Не удалось обновить количество товара с ID " + productId);
        }

        return updated;
    }

    @Override
    public boolean deleteStock(Long productId) {
        checkStockKeeperPermission();

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

    private List<Stock> findAndValidate(Supplier<List<Stock>> supplier) {
        List<Stock> stock = supplier.get();

        if (stock.isEmpty()) {
            LoggerUtil.warn("На складе нет продуктов!");
            throw new ProductNotFoundException("На складе нет продуктов!");
        }

        LoggerUtil.info("Количество продуктов на складе: " + stock.size());
        return stock;
    }

    private void checkStockKeeperPermission() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }

        if (!userService.hasRole(ROLE_STOCK_KEEPER)) {
            throw new AuthorizationException("Только кладовщик может управлять остатками товаров");
        }
    }
}