package service;

import dao.impl.ProductCategoryDao;
import exception.nsee.CategoryNotFoundException;
import model.ProductCategory;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class ProductCategoryService {
    private static ProductCategoryService instance;
    private final ProductCategoryDao categoryDao = new ProductCategoryDao();

    private ProductCategoryService() {}

    public static synchronized ProductCategoryService getInstance() {
        if (instance == null) {
            instance = new ProductCategoryService();
        }
        return instance;
    }

    public List<ProductCategory> getAllCategories() {
        return findAndValidate(categoryDao::findAll);
    }

    
    public ProductCategory getCategoryById(Long id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория с ID " + id + " не найдена"));
    }

    private List<ProductCategory> findAndValidate(Supplier<List<ProductCategory>> supplier) {
        List<ProductCategory> categories = supplier.get();

        if (categories.isEmpty()) {
            LoggerUtil.warn("Категории продуктов не были найдены!");
            throw new CategoryNotFoundException("Категории продуктов не были найдены!");
        }

        LoggerUtil.info("Получено категорий: " + categories.size());
        return categories;
    }
}