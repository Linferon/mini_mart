package service;

import dao.impl.ExpenseDao;
import exception.nsee.ExpenseNotFoundException;
import model.Expense;
import model.ExpenseCategory;
import model.User;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ExpenseService {
    private static ExpenseService instance;
    private final ExpenseDao expenseDao = new ExpenseDao();
    private final UserService userService = UserService.getInstance();
    private final ExpenseCategoryService categoryService = ExpenseCategoryService.getInstance();


    private ExpenseService() {
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

        return findAndValidate(() -> expenseDao.findByCategory(categoryId),
                "Расходы по категории с ID " + categoryId + " не найдены");
    }


    public List<Expense> getExpensesByAccountant(Long accountantId) {
        userService.getUserById(accountantId);

        return findAndValidate(() -> expenseDao.findByAccountant(accountantId),
                "Расходы, зарегистрированные бухгалтером с ID " + accountantId + " не найдены");
    }


    public List<Expense> getExpensesByDateRange(Timestamp startDate, Timestamp endDate) {
        validateDateRange(startDate, endDate);

        return findAndValidate(() -> expenseDao.findByDateRange(startDate, endDate),
                "Расходы за период с " + startDate + " по " + endDate + " не найдены");
    }

    public BigDecimal getTotalExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addExpense(Expense expense) {
        validateExpense(expense);

        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(Timestamp.from(Instant.now()));
        }

        if (expense.getAccountant() == null) {
            expense.setAccountant(userService.getCurrentUser());
        }

        Long id = expenseDao.save(expense);
        if (id != null) {
            LoggerUtil.info("Добавлен новый расход с ID " + id +
                    " по категории '" + expense.getCategory().name() +
                    "', сумма: " + expense.getTotalAmount());
        }
    }

    public void addPurchaseExpense(BigDecimal totalAmount) {
        ExpenseCategory category = categoryService.getExpenseCategoryByName("Покупка товара");
        Expense expense = new Expense(
                category,
                totalAmount
        );
        addExpense(expense);
    }

    public void addExpense(Long categoryId, BigDecimal amount, Timestamp expenseDate) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID категории должен быть указан");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительным числом");
        }

        ExpenseCategory category = categoryService.getExpenseCategoryById(categoryId);
        User accountant = userService.getCurrentUser();

        Expense expense = new Expense(
                category,
                amount,
                expenseDate);

        expense.setAccountant(accountant);

        addExpense(expense);
    }

    public void addExpense(Long categoryId, BigDecimal amount) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID категории должен быть указан");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительным числом");
        }

        ExpenseCategory category = categoryService.getExpenseCategoryById(categoryId);

        Expense expense = new Expense(
                category,
                amount);

        addExpense(expense);
    }

    public void updatePurchaseExpense(BigDecimal oldTotalAmount, BigDecimal newTotalAmount, Timestamp purchaseDate) {
        Expense expense = getExpenseByAmountAndDate(oldTotalAmount, purchaseDate);
        expense.setTotalAmount(newTotalAmount);
        updateExpense(expense);
    }

    public Expense getExpenseByAmountAndDate(BigDecimal totalAmount, Timestamp purchaseDate) {
        return expenseDao.findByTotalAmountAndDate(totalAmount, purchaseDate)
                .orElseThrow(() -> new ExpenseNotFoundException("Расход с не был найден"));
    }

    public boolean updateExpense(Expense expense) {
        if (expense.getId() == null) {
            throw new IllegalArgumentException("ID расхода не может быть пустым при обновлении");
        }

        validateExpense(expense);

        getExpenseById(expense.getId());

        boolean updated = expenseDao.update(expense);
        if (updated) {
            LoggerUtil.info("Обновлен расход с ID " + expense.getId() +
                    " по категории '" + expense.getCategory().name() +
                    "', сумма: " + expense.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось обновить расход с ID " + expense.getId());
        }

        return updated;
    }

    public void deletePurchaseExpense(BigDecimal totalCost, Timestamp purchaseDate) {
        Expense expense = getExpenseByAmountAndDate(totalCost, purchaseDate);
        deleteExpense(expense.getId());
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);

        boolean deleted = expenseDao.deleteById(id);
        if (deleted) {
            LoggerUtil.info("Удален расход с ID " + id +
                    " по категории '" + expense.getCategory().name() +
                    "', сумма: " + expense.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось удалить расход с ID " + id);
        }
    }


    public BigDecimal getTotalExpenseAmount(Timestamp startDate, Timestamp endDate) {
        validateDateRange(startDate, endDate);

        List<Expense> expenses;
        try {
            expenses = expenseDao.findByDateRange(startDate, endDate);
        } catch (ExpenseNotFoundException e) {
            return BigDecimal.ZERO;
        }

        return expenses.stream()
                .map(Expense::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public Map<ExpenseCategory, BigDecimal> getExpensesByCategory(Timestamp startDate, Timestamp endDate) {
        validateDateRange(startDate, endDate);

        List<Expense> expenses;
        try {
            expenses = expenseDao.findByDateRange(startDate, endDate);
        } catch (ExpenseNotFoundException e) {
            return new HashMap<>();
        }

        Map<ExpenseCategory, BigDecimal> result = new HashMap<>();

        for (Expense expense : expenses) {
            ExpenseCategory category = expense.getCategory();
            BigDecimal currentAmount = result.getOrDefault(category, BigDecimal.ZERO);
            result.put(category, currentAmount.add(expense.getTotalAmount()));
        }

        return result;
    }


    public BigDecimal getMonthlyAverage(int numberOfMonths) {
        if (numberOfMonths <= 0) {
            throw new IllegalArgumentException("Количество месяцев должно быть положительным числом");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(numberOfMonths);

        Timestamp start = Timestamp.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Timestamp end = Timestamp.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        BigDecimal totalAmount;
        try {
            totalAmount = getTotalExpenseAmount(start, end);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

        return totalAmount.divide(BigDecimal.valueOf(numberOfMonths), 2, RoundingMode.HALF_UP);
    }


    private void validateExpense(Expense expense) {
        if (expense.getCategory() == null || expense.getCategory().id() == null) {
            throw new IllegalArgumentException("Категория расхода должна быть указана");
        }

        if (expense.getTotalAmount() == null || expense.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма расхода должна быть положительным числом");
        }

        categoryService.getExpenseCategoryById(expense.getCategory().id());

        if (expense.getAccountant() != null && expense.getAccountant().getId() != null) {
            userService.getUserById(expense.getAccountant().getId());
        }
    }

    private void validateDateRange(Timestamp startDate, Timestamp endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания периода должны быть указаны");
        }

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
    }

    private List<Expense> findAndValidate(Supplier<List<Expense>> supplier, String errorMessage) {
        List<Expense> expenses = supplier.get();

        if (expenses.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new ExpenseNotFoundException(errorMessage);
        }

        return expenses;
    }
}