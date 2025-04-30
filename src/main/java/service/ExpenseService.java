package service;

import dao.impl.ExpenseDao;
import exception.nsee.ExpenseNotFoundException;
import model.Expense;
import model.ExpenseCategory;
import model.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static util.DateTimeUtils.convertToTimestamp;
import static util.DateTimeUtils.extractLocalDate;
import static util.EntityUtil.findAndValidate;
import static util.LoggerUtil.*;
import static util.ValidationUtil.*;


public class ExpenseService {
    private static ExpenseService instance;
    private final ExpenseDao expenseDao;
    private final UserService userService;
    private final ExpenseCategoryService categoryService;
    private final MonthlyBudgetService budgetService;

    private ExpenseService() {
        this(new ExpenseDao(),
                UserService.getInstance(),
                ExpenseCategoryService.getInstance(),
                MonthlyBudgetService.getInstance());
    }

    ExpenseService(ExpenseDao expenseDao,
                   UserService userService,
                   ExpenseCategoryService categoryService,
                   MonthlyBudgetService budgetService) {
        this.expenseDao = expenseDao;
        this.userService = userService;
        this.categoryService = categoryService;
        this.budgetService = budgetService;
    }

    public static synchronized ExpenseService getInstance() {
        if (instance == null) {
            instance = new ExpenseService();
        }
        return instance;
    }

    public List<Expense> getAllExpenses() {
        return findAndValidate(expenseDao::findAll, "Расходы не найдены");
    }

    public Expense getExpenseById(Long id) {
        return expenseDao.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Расход с ID " + id + " не найден"));
    }

    public List<Expense> getExpensesByCategory(Long categoryId) {
        categoryService.getExpenseCategoryById(categoryId);
        return findAndValidate(
                () -> expenseDao.findByCategory(categoryId),
                "Расходы по категории с ID " + categoryId + " не найдены"
        );
    }

    public List<Expense> getExpensesByDateRange(Timestamp startDate, Timestamp endDate) {
        validateDateRange(startDate, endDate);
        return findAndValidate(
                () -> expenseDao.findByDateRange(startDate, endDate),
                "Расходы за период с " + startDate + " по " + endDate + " не найдены"
        );
    }

    public Expense getExpenseByAmountAndDate(BigDecimal totalAmount, Timestamp purchaseDate) {
        return expenseDao.findByTotalAmountAndDate(totalAmount, purchaseDate)
                .orElseThrow(() -> new ExpenseNotFoundException("Расход не был найден"));
    }

    public BigDecimal getTotalExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getTotalAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    public void addExpense(Expense expense) {
        validateExpense(expense);
        prepareExpenseBeforeSave(expense);

        expenseDao.save(expense);
        logExpenseOperation("Добавлен", expense);
        updateBudgetAfterAdd(expense);
    }

    public void addExpense(Long categoryId, BigDecimal amount, Timestamp expenseDate) {
        ExpenseCategory category = categoryService.getExpenseCategoryById(categoryId);
        User accountant = userService.getCurrentUser();

        Expense expense = new Expense(
                category,
                amount,
                expenseDate,
                accountant);

        addExpense(expense);
    }

    public void addPurchaseExpense(BigDecimal totalAmount) {
        ExpenseCategory category = categoryService.getExpenseCategoryByName("Покупка товара");
        Expense expense = new Expense(
                category,
                totalAmount);

        addExpense(expense);
    }

    public void addSalaryExpense(BigDecimal amount, LocalDate paymentDate) {
        try {
            ExpenseCategory salaryCategory = categoryService.getExpenseCategoryByName("Заработная плата");
            Timestamp paymentTimestamp = convertToTimestamp(paymentDate);

            Expense expense = new Expense(
                    salaryCategory,
                    amount,
                    paymentTimestamp,
                    userService.getCurrentUser());

            expenseDao.save(expense);
            budgetService.updateMonthlyBudgetExpense(paymentDate, amount);
            info("Добавлен расход на зарплату на сумму " + amount);
        } catch (Exception e) {
            error("Ошибка при добавлении расхода на зарплату: " + e.getMessage(), e);
        }
    }

    public boolean updateExpense(Expense expense) {
        Expense oldExpense = getExpenseById(expense.getId());
        validateExpense(expense);

        boolean updated = expenseDao.update(expense);

        if (updated) {
            logExpenseOperation("Обновлен", expense);
            updateBudgetAfterUpdate(oldExpense, expense);
        } else {
            warn("Не удалось обновить расход с ID " + expense.getId());
        }
        return updated;
    }

    public boolean updateExpense(Long expenseId, Long categoryId, BigDecimal amount, LocalDate expenseDate) {
        Expense expense = getExpenseById(expenseId);
        ExpenseCategory expenseCategory = categoryService.getExpenseCategoryById(categoryId);

        expense.setCategory(expenseCategory);
        expense.setTotalAmount(amount);
        expense.setExpenseDate(convertToTimestamp(expenseDate));

        return updateExpense(expense);
    }

    public void updatePurchaseExpense(BigDecimal oldTotalAmount, BigDecimal newTotalAmount, Timestamp purchaseDate) {
        Expense expense = getExpenseByAmountAndDate(oldTotalAmount, purchaseDate);
        expense.setTotalAmount(newTotalAmount);
        updateExpense(expense);
    }

    public void updateSalaryExpense(BigDecimal oldAmount, BigDecimal newAmount, LocalDate paymentDate) {
        try {
            Timestamp paymentTimestamp = convertToTimestamp(paymentDate);
            Expense expense = getExpenseByAmountAndDate(oldAmount, paymentTimestamp);
            expense.setTotalAmount(newAmount);

            boolean updated = updateExpense(expense);
            if (updated) {
                info("Обновлен расход на зарплату: изменение суммы с " +
                        oldAmount + " на " + newAmount);
            }
        } catch (Exception e) {
            error("Ошибка при обновлении расхода на зарплату: " + e.getMessage(), e);
        }
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);

        boolean deleted = expenseDao.deleteById(id);
        if (deleted) {
            logExpenseOperation("Удален", expense);
            updateBudgetAfterDelete(expense);
        } else {
            warn("Не удалось удалить расход с ID " + id);
        }
    }

    public void deletePurchaseExpense(BigDecimal totalCost, Timestamp purchaseDate) {
        Expense expense = getExpenseByAmountAndDate(totalCost, purchaseDate);
        deleteExpense(expense.getId());
    }

    public void deleteSalaryExpense(BigDecimal amount, LocalDate paymentDate) {
        try {
            Timestamp paymentTimestamp = convertToTimestamp(paymentDate);
            Expense expense = getExpenseByAmountAndDate(amount, paymentTimestamp);
            deleteExpense(expense.getId());

            info("Удален расход на зарплату на сумму " + amount);
        } catch (Exception e) {
            error("Ошибка при удалении расхода на зарплату: " + e.getMessage(), e);
        }
    }

    private void prepareExpenseBeforeSave(Expense expense) {
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(Timestamp.from(Instant.now()));
        }

        if (expense.getAccountant() == null) {
            expense.setAccountant(userService.getCurrentUser());
        }
    }

    private void logExpenseOperation(String operation, Expense expense) {
        info(operation + " расход с ID " + expense.getId() +
                " по категории '" + expense.getCategory().name() +
                "', сумма: " + expense.getTotalAmount());
    }

    private void updateBudgetAfterAdd(Expense expense) {
        try {
            LocalDate expenseDate = extractLocalDate(expense);
            budgetService.updateMonthlyBudgetExpense(expenseDate, expense.getTotalAmount());
        } catch (Exception e) {
            error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
        }
    }

    private void updateBudgetAfterUpdate(Expense oldExpense, Expense newExpense) {
        try {
            boolean amountChanged = !oldExpense.getTotalAmount().equals(newExpense.getTotalAmount());
            boolean dateChanged = !oldExpense.getExpenseDate().equals(newExpense.getExpenseDate());

            if (amountChanged || dateChanged) {
                LocalDate oldDate = extractLocalDate(oldExpense);
                budgetService.updateMonthlyBudgetExpense(oldDate, oldExpense.getTotalAmount().negate());

                LocalDate newDate = extractLocalDate(newExpense);
                budgetService.updateMonthlyBudgetExpense(newDate, newExpense.getTotalAmount());
            }
        } catch (Exception e) {
            error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
        }
    }

    private void updateBudgetAfterDelete(Expense expense) {
        try {
            LocalDate expenseDate = extractLocalDate(expense);
            budgetService.updateMonthlyBudgetExpense(expenseDate, expense.getTotalAmount().negate());
        } catch (Exception e) {
            error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
        }
    }

    private void validateExpense(Expense expense) {
        Objects.requireNonNull(expense.getCategory(), "Категория расхода должна быть указана");
        validatePositiveAmount(expense.getTotalAmount(), "Сумма должна быть положительным числом");
        categoryService.getExpenseCategoryById(expense.getCategory().id());
        userService.getUserById(expense.getAccountant().getId());
    }
}