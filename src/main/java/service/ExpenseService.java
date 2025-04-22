package service;

import model.Expense;
import model.ExpenseCategory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface ExpenseService {
    List<Expense> getAllExpenses();

    Expense getExpenseById(Long id);

    List<Expense> getExpensesByCategory(Long categoryId);

    List<Expense> getExpensesByAccountant(Long accountantId);

    List<Expense> getExpensesByDateRange(Timestamp startDate, Timestamp endDate);

    Long addExpense(Expense expense);

    Long addExpense(Long categoryId, BigDecimal amount);

    boolean updateExpense(Expense expense);

    boolean deleteExpense(Long id);

    BigDecimal getTotalExpenseAmount(Timestamp startDate, Timestamp endDate);

    Map<ExpenseCategory, BigDecimal> getExpensesByCategory(Timestamp startDate, Timestamp endDate);

    BigDecimal getMonthlyAverage(int numberOfMonths);
}
