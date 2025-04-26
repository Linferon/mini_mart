package service;

import dao.impl.ProductDao;
import exception.nsee.ProductNotFoundException;
import model.Product;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class ProductService {
    private static ProductService instance;
    private final ProductDao productDao = new ProductDao();
    private final ProductCategoryService productCategoryService = ProductCategoryService.getInstance();

    private ProductService() {}

    public static synchronized ProductService getInstance() {
        if (instance == null) {
            instance = new ProductService();
        }
        return instance;
    }

    public List<Product> getAllProducts() {
        return findAndValidate(productDao::findAll);
    }

    
    public Product getProductById(Long id) {
        return productDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + id + " не найден"));
    }

    public void addProduct(Product product) {
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
    }

    public void updateProduct(Product product) {
        validateProduct(product);

        getProductById(product.getId());
        productCategoryService.getCategoryById(product.getCategory().getId());

        Timestamp now = Timestamp.from(Instant.now());
        product.setUpdatedAt(now);

        boolean updated = productDao.update(product);
        if (updated) {
            LoggerUtil.info("Обновлен продукт с ID " + product.getId() + ": " + product.getName());
        } else {
            LoggerUtil.warn("Не удалось обновить продукт с ID " + product.getId());
        }
    }
    
    public void deleteProduct(Long id) {
        getProductById(id);
        boolean deleted = productDao.deleteById(id);

        if (deleted) {
            LoggerUtil.info("Удален продукт с ID " + id);
        } else {
            LoggerUtil.warn("Не удалось удалить продукт с ID " + id);
        }
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

    private List<Product> findAndValidate(Supplier<List<Product>> supplier) {
        List<Product> products = supplier.get();

        if (products.isEmpty()) {
            LoggerUtil.warn("Продукты не были найдены!");
            throw new ProductNotFoundException("Продукты не были найдены!");
        }

        LoggerUtil.info("Получено категорий: " + products.size());
        return products;
    }
}