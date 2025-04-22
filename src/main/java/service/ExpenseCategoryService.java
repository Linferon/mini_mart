package service;

import model.ExpenseCategory;

import java.util.List;

public interface ExpenseCategoryService {
    List<ExpenseCategory> getAllExpenseCategories();

    ExpenseCategory getExpenseCategoryById(Long id);

    ExpenseCategory getExpenseCategoryByName(String name);
}
