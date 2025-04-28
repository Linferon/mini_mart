package service;

import dao.impl.ExpenseCategoryDao;
import exception.nsee.CategoryNotFoundException;
import model.ExpenseCategory;

import java.util.List;
import static util.EntityUtil.findAndValidate;

public class ExpenseCategoryService {
    private static ExpenseCategoryService instance;
    private final ExpenseCategoryDao categoryDao;

    private ExpenseCategoryService() {
        categoryDao = new ExpenseCategoryDao();
    }

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
}