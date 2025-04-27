package service;

import dao.impl.ExpenseCategoryDao;
import exception.nsee.CategoryNotFoundException;
import model.ExpenseCategory;
import util.LoggerUtil;

import java.util.List;
import java.util.function.Supplier;

public class ExpenseCategoryService {
    private static ExpenseCategoryService instance;
    private final ExpenseCategoryDao categoryDao = new ExpenseCategoryDao();

    private ExpenseCategoryService() {}
    public static synchronized ExpenseCategoryService getInstance() {
        if (instance == null) {
            instance = new ExpenseCategoryService();
        }
        return instance;
    }
    
    public List<ExpenseCategory> getAllExpenseCategories() {
        return findAndValidate(categoryDao::findAll);
    }

    public ExpenseCategory getExpenseCategoryById(Long id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория затрат с ID " + id + " не найдена"));
    }

    public ExpenseCategory getExpenseCategoryByName(String categoryName) {
        return categoryDao.findByName(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException("Категория затрат с именем " + categoryName + " не найдена"));
    }

    private List<ExpenseCategory> findAndValidate(Supplier<List<ExpenseCategory>> supplier) {
        List<ExpenseCategory> categories = supplier.get();

        if (categories.isEmpty()) {
            LoggerUtil.warn("Категории затрат не найдены");
            throw new CategoryNotFoundException("Категории затрат не найдены");
        }

        LoggerUtil.info("Получено категорий затрат: " + categories.size());
        return categories;
    }
}