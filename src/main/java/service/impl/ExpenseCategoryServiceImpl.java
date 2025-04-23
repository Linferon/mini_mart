package service.impl;

import dao.impl.ExpenseCategoryDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.nsee.CategoryNotFoundException;
import model.ExpenseCategory;
import service.interfaces.ExpenseCategoryService;
import service.interfaces.UserService;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {
    private final ExpenseCategoryDao categoryDao = new ExpenseCategoryDao();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";
    private static final String ROLE_ACCOUNTANT = "Бухгалтер";

    @Override
    public List<ExpenseCategory> getAllExpenseCategories() {
        checkManagementPermission();
        return findAndValidate(categoryDao::findAll, "Категории затрат не найдены");
    }

    @Override
    public ExpenseCategory getExpenseCategoryById(Long id) {
        checkManagementPermission();
        return categoryDao.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория затрат с ID " + id + " не найдена"));
    }

    @Override
    public ExpenseCategory getExpenseCategoryByName(String name) {
        checkManagementPermission();
        return categoryDao.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Категория затрат с названием '" + name + "' не найдена"));
    }

    private List<ExpenseCategory> findAndValidate(Supplier<List<ExpenseCategory>> supplier, String errorMessage) {
        List<ExpenseCategory> categories = supplier.get();

        if (categories.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new CategoryNotFoundException(errorMessage);
        }

        LoggerUtil.info("Получено категорий затрат: " + categories.size());
        return categories;
    }

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkManagementPermission() {
        checkAuthentication();

        if (!userService.hasRole(ROLE_DIRECTOR, ROLE_ACCOUNTANT)) {
            throw new AuthorizationException("Только директор или бухгалтер может управлять категориями затрат");
        }
    }
}