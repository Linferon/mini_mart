package service.impl;

import dao.impl.ProductDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.nsee.ProductNotFoundException;
import model.Product;
import service.interfaces.ProductCategoryService;
import service.interfaces.ProductService;
import service.interfaces.UserService;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao = new ProductDao();
    private final ProductCategoryService productCategoryService = new ProductCategoryServiceImpl();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_STOCK_KEEPER = "Кладовщик";

    @Override
    public List<Product> getAllProducts() {
        checkStockKeeperPermission();
        return findAndValidate(productDao::findAll, "Продукты не были найдены!");
    }

    @Override
    public Product getProductById(Long id) {
        checkStockKeeperPermission();
        return productDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + id + " не найден"));
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        checkStockKeeperPermission();
        productCategoryService.getCategoryById(categoryId);
        return findAndValidate(() -> productDao.findByCategory(categoryId),
                "Продукты с ID категории " + categoryId + " не были найдены!");
    }

    @Override
    public List<Product> getProductsByName(String name) {
        checkStockKeeperPermission();
        return findAndValidate(() -> productDao.findByName(name),
                "Продукты с именем " + name + " не были найдены!");
    }

    @Override
    public Long addProduct(Product product) {
        checkStockKeeperPermission();

        validateProduct(product);
        productCategoryService.getCategoryById(product.getCategory().getId());

        Timestamp now = Timestamp.from(Instant.now());
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(now);
        }
        if (product.getUpdatedAt() == null) {
            product.setUpdatedAt(now);
        }

        Long id = productDao.save(product);
        LoggerUtil.info("Добавлен новый продукт с ID " + id + ": " + product.getName());
        return id;
    }

    @Override
    public boolean updateProduct(Product product) {
        checkStockKeeperPermission();

        validateProduct(product);

        getProductById(product.getId());

        productCategoryService.getCategoryById(product.getCategory().getId());

        product.setUpdatedAt(Timestamp.from(Instant.now()));

        boolean updated = productDao.update(product);

        if (updated) {
            LoggerUtil.info("Обновлен продукт с ID " + product.getId() + ": " + product.getName());
        } else {
            LoggerUtil.warn("Не удалось обновить продукт с ID " + product.getId());
        }
        return updated;
    }

    @Override
    public boolean deleteProduct(Long id) {
        checkStockKeeperPermission();

        getProductById(id);
        boolean deleted = productDao.deleteById(id);

        if (deleted) {
            LoggerUtil.info("Удален продукт с ID " + id);
        } else {
            LoggerUtil.warn("Не удалось удалить продукт с ID " + id);
        }
        return deleted;
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название продукта не может быть пустым");
        }

        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Категория продукта должна быть указана");
        }

        if (product.getBuyPrice() == null || product.getBuyPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена закупки должна быть неотрицательной");
        }

        if (product.getSellPrice() == null || product.getSellPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена продажи должна быть неотрицательной");
        }
    }

    private void checkStockKeeperPermission() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }

        if (!userService.hasRole(ROLE_STOCK_KEEPER)) {
            throw new AuthorizationException("Только кладовщик может управлять продуктами");
        }
    }

    private List<Product> findAndValidate(Supplier<List<Product>> supplier, String errorMessage) {
        List<Product> products = supplier.get();

        if (products.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new ProductNotFoundException(errorMessage);
        }

        LoggerUtil.info("Получено категорий: " + products.size());
        return products;
    }
}