package service;

import dao.impl.ProductDao;
import model.Product;
import model.ProductCategory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static util.EntityUtil.findAndValidate;
import static util.LoggerUtil.info;
import static util.LoggerUtil.warn;
import static util.ValidationUtil.*;

public class ProductService {
    private static ProductService instance;
    private final ProductDao productDao;
    private final ProductCategoryService productCategoryService;

    private ProductService() {
        productDao = new ProductDao();
        productCategoryService = ProductCategoryService.getInstance();
    }

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

    public void addProduct(String name, Long categoryId, BigDecimal buyPrice, BigDecimal sellPrice) {
        ProductCategory category = productCategoryService.getCategoryById(categoryId);
        Product product = new Product(null, name, category, buyPrice, sellPrice);
        validateProduct(product);
        productCategoryService.getCategoryById(product.getCategory().id());

        Timestamp now = Timestamp.from(Instant.now());
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(now);
        }
        if (product.getUpdatedAt() == null) {
            product.setUpdatedAt(now);
        }

        Long id = productDao.save(product);
        info("Добавлен новый продукт с ID " + id + ": " + product.getName());
    }

    public void updateProduct(Product product) {
        validateProduct(product);

        getProductById(product.getId());
        productCategoryService.getCategoryById(product.getCategory().id());

        Timestamp now = Timestamp.from(Instant.now());
        product.setUpdatedAt(now);

        boolean updated = productDao.update(product);
        if (updated) {
            info("Обновлен продукт с ID " + product.getId() + ": " + product.getName());
        } else {
            warn("Не удалось обновить продукт с ID " + product.getId());
        }
    }

    public void deleteProduct(Long id) {
        getProductById(id);
        boolean deleted = productDao.deleteById(id);

        if (deleted) {
            info("Удален продукт с ID " + id);
        } else {
            warn("Не удалось удалить продукт с ID " + id);
        }
    }

    private void validateProduct(Product product) {
        validateString(product.getName(), "Название продукта не может быть пустым");
        validateId(product.getCategory().id(), "Категория продукта должна быть указана");
        validatePositiveAmount(product.getBuyPrice(), "Цена закупки должна быть положительной");
        validatePositiveAmount(product.getSellPrice(), "Цена продажи должна быть неотрицательной");
    }
}