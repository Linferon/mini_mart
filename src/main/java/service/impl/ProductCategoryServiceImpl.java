package service.impl;

import dao.impl.ProductCategoryDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.CategoryNotFoundException;
import model.ProductCategory;
import service.ProductCategoryService;
import service.UserService;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryDao categoryDao = new ProductCategoryDao();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_STOCK_KEEPER = "Кладовщик";

    @Override
    public List<ProductCategory> getAllCategories() {
        checkManagementPermission();
        return findAndValidate(categoryDao::findAll, "Категории продуктов не были найдены!");
    }

    @Override
    public ProductCategory getCategoryById(Long id) {
        checkManagementPermission();
        return categoryDao.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория с ID " + id + " не найдена"));
    }

    @Override
    public List<ProductCategory> getCategoryByName(String name) {
        checkManagementPermission();
        return findAndValidate(() -> categoryDao.findByName(name),
                "Категории продуктов с таким именем не были найдены!");
    }

    private List<ProductCategory> findAndValidate(Supplier<List<ProductCategory>> supplier, String errorMessage) {
        List<ProductCategory> categories = supplier.get();

        if (categories.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new CategoryNotFoundException(errorMessage);
        }

        LoggerUtil.info("Получено категорий: " + categories.size());
        return categories;
    }

    private void checkManagementPermission() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }

        if (!userService.hasRole(ROLE_STOCK_KEEPER)) {
            throw new AuthorizationException("Только кладовщик может управлять категориями товаров");
        }
    }
}