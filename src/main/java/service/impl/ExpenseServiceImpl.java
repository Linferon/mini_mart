package service.impl;

import dao.impl.ExpenseDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.ExpenseNotFoundException;
import model.Expense;
import model.ExpenseCategory;
import model.User;
import service.ExpenseCategoryService;
import service.ExpenseService;
import service.UserService;
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

public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseDao expenseDao = new ExpenseDao();
    private final UserService userService = new UserServiceImpl();
    private final ExpenseCategoryService categoryService = new ExpenseCategoryServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";
    private static final String ROLE_ACCOUNTANT = "Бухгалтер";

    @Override
    public List<Expense> getAllExpenses() {
        checkAuthentication();
        return findAndValidate(expenseDao::findAll, "Расходы не найдены");
    }

    @Override
    public Expense getExpenseById(Long id) {
        checkAuthentication();
        return expenseDao.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Расход с ID " + id + " не найден"));
    }

    @Override
    public List<Expense> getExpensesByCategory(Long categoryId) {
        checkAuthentication();

        categoryService.getExpenseCategoryById(categoryId);

        return findAndValidate(() -> expenseDao.findByCategory(categoryId),
                "Расходы по категории с ID " + categoryId + " не найдены");
    }

    @Override
    public List<Expense> getExpensesByAccountant(Long accountantId) {
        checkAuthentication();

        userService.getUserById(accountantId);

        return findAndValidate(() -> expenseDao.findByAccountant(accountantId),
                "Расходы, зарегистрированные бухгалтером с ID " + accountantId + " не найдены");
    }

    @Override
    public List<Expense> getExpensesByDateRange(Timestamp startDate, Timestamp endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        return findAndValidate(() -> expenseDao.findByDateRange(startDate, endDate),
                "Расходы за период с " + startDate + " по " + endDate + " не найдены");
    }

    @Override
    public Long addExpense(Expense expense) {
        checkAccountantPermission();
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
                    " по категории '" + expense.getCategory().getName() +
                    "', сумма: " + expense.getTotalAmount());
        }

        return id;
    }

    @Override
    public Long addExpense(Long categoryId, BigDecimal amount) {
        checkAccountantPermission();

        if (categoryId == null) {
            throw new IllegalArgumentException("ID категории должен быть указан");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительным числом");
        }

        ExpenseCategory category = categoryService.getExpenseCategoryById(categoryId);
        User accountant = userService.getCurrentUser();

        Expense expense = new Expense(
                null,
                category,
                amount,
                Timestamp.from(Instant.now()),
                accountant
        );

        return addExpense(expense);
    }

    @Override
    public boolean updateExpense(Expense expense) {
        checkAccountantPermission();

        if (expense.getId() == null) {
            throw new IllegalArgumentException("ID расхода не может быть пустым при обновлении");
        }

        validateExpense(expense);

        getExpenseById(expense.getId());

        boolean updated = expenseDao.update(expense);
        if (updated) {
            LoggerUtil.info("Обновлен расход с ID " + expense.getId() +
                    " по категории '" + expense.getCategory().getName() +
                    "', сумма: " + expense.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось обновить расход с ID " + expense.getId());
        }

        return updated;
    }

    @Override
    public boolean deleteExpense(Long id) {
        if (!userService.hasRole(ROLE_DIRECTOR, ROLE_ACCOUNTANT)) {
            throw new AuthorizationException("Только директор или бухгалтер может удалять расходы");
        }

        Expense expense = getExpenseById(id);

        boolean deleted = expenseDao.deleteById(id);
        if (deleted) {
            LoggerUtil.info("Удален расход с ID " + id +
                    " по категории '" + expense.getCategory().getName() +
                    "', сумма: " + expense.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось удалить расход с ID " + id);
        }

        return deleted;
    }

    @Override
    public BigDecimal getTotalExpenseAmount(Timestamp startDate, Timestamp endDate) {
        checkAuthentication();
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

    @Override
    public Map<ExpenseCategory, BigDecimal> getExpensesByCategory(Timestamp startDate, Timestamp endDate) {
        checkAuthentication();
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

    @Override
    public BigDecimal getMonthlyAverage(int numberOfMonths) {
        checkAuthentication();

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
        if (expense.getCategory() == null || expense.getCategory().getId() == null) {
            throw new IllegalArgumentException("Категория расхода должна быть указана");
        }

        if (expense.getTotalAmount() == null || expense.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма расхода должна быть положительным числом");
        }

        categoryService.getExpenseCategoryById(expense.getCategory().getId());

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

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkAccountantPermission() {
        checkAuthentication();

        if (!userService.hasRole(ROLE_ACCOUNTANT, ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только бухгалтер или директор может управлять расходами");
        }
    }
}