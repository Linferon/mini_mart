package service;

import dao.impl.ProductCategoryDao;
import exception.nsee.CategoryNotFoundException;
import model.ProductCategory;

import java.util.List;

import static util.EntityUtil.findAndValidate;

public class ProductCategoryService {
    private static ProductCategoryService instance;
    private final ProductCategoryDao categoryDao;
    private ProductCategoryService() {
        categoryDao = new ProductCategoryDao();
    }

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
}