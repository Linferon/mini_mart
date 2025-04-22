package service.impl;

import dao.impl.MonthlyBudgetDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.BudgetNotFoundException;
import model.MonthlyBudget;
import service.MonthlyBudgetService;
import service.UserService;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class MonthlyBudgetServiceImpl implements MonthlyBudgetService {
    private final MonthlyBudgetDao budgetDao = new MonthlyBudgetDao();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";
    private static final String ROLE_ACCOUNTANT = "Бухгалтер";

    @Override
    public List<MonthlyBudget> getAllBudgets() {
        checkAuthentication();
        return findAndValidate(budgetDao::findAll, "Бюджеты не найдены");
    }

    @Override
    public MonthlyBudget getBudgetById(Long id) {
        checkAuthentication();
        return budgetDao.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException("Бюджет с ID " + id + " не найден"));
    }

    @Override
    public MonthlyBudget getBudgetByDate(LocalDate date) {
        checkAuthentication();
        return budgetDao.findByDate(Date.valueOf(date))
                .orElseThrow(() -> new BudgetNotFoundException("Не было найдено записей на эту дату!"));
    }

    @Override
    public List<MonthlyBudget> getBudgetsByDateRange(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        return findAndValidate(
                () -> budgetDao.findByDateRange(Date.valueOf(startDate), Date.valueOf(endDate)),
                "Бюджеты за период с " + startDate + " по " + endDate + " не найдены"
        );
    }

    @Override
    public List<MonthlyBudget> getBudgetsByDirector(Long directorId) {
        checkAuthentication();
        userService.getUserById(directorId);

        return findAndValidate(
                () -> budgetDao.findByDirector(directorId),
                "Бюджеты, созданные директором с ID " + directorId + " не найдены"
        );
    }

    @Override
    public Long createBudget(MonthlyBudget budget) {
        checkDirectorPermission();
        validateBudget(budget);

        try {
            getBudgetByDate(budget.getBudgetDate());
            throw new IllegalArgumentException("Бюджет на " + budget.getBudgetDate() + " уже существует");
        } catch (BudgetNotFoundException ignored) {}

        setupBudget(budget);

        Long id = budgetDao.save(budget);
        if (id != null) {
            LoggerUtil.info("Создан новый бюджет с ID " + id + " на " + budget.getBudgetDate());
        }

        return id;
    }

    @Override
    public Long createBudget(LocalDate budgetDate, BigDecimal plannedIncome, BigDecimal plannedExpenses) {
        checkDirectorPermission();

        if (budgetDate == null) {
            throw new IllegalArgumentException("Дата бюджета должна быть указана");
        }

        validatePositiveAmount(plannedIncome, "Планируемый доход");
        validatePositiveAmount(plannedExpenses, "Планируемые расходы");

        MonthlyBudget budget = new MonthlyBudget(
                null,
                budgetDate,
                plannedIncome,
                plannedExpenses,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                plannedIncome.subtract(plannedExpenses),
                null,
                null,
                userService.getCurrentUser()
        );

        return createBudget(budget);
    }

    @Override
    public boolean updateBudget(MonthlyBudget budget) {
        checkDirectorPermission();

        if (budget.getId() == null) {
            throw new IllegalArgumentException("ID бюджета не может быть пустым при обновлении");
        }

        validateBudget(budget);

        getBudgetById(budget.getId());

        try {
            MonthlyBudget existingBudget = getBudgetByDate(budget.getBudgetDate());
            if (!existingBudget.getId().equals(budget.getId())) {
                throw new IllegalArgumentException("Бюджет на " + budget.getBudgetDate() + " уже существует");
            }
        } catch (BudgetNotFoundException ignored) {}

        calculateNetResult(budget);

        boolean updated = budgetDao.update(budget);
        if (updated) {
            LoggerUtil.info("Обновлен бюджет с ID " + budget.getId() + " на " + budget.getBudgetDate());
        } else {
            LoggerUtil.warn("Не удалось обновить бюджет с ID " + budget.getId());
        }

        return updated;
    }

    @Override
    public boolean updateActualValues(Long budgetId, BigDecimal actualIncome, BigDecimal actualExpenses) {
        checkAccountantPermission();

        validatePositiveAmount(actualIncome, "Фактический доход");
        validatePositiveAmount(actualExpenses, "Фактические расходы");

        MonthlyBudget budget = getBudgetById(budgetId);
        budget.setActualIncome(actualIncome);
        budget.setActualExpenses(actualExpenses);
        calculateNetResult(budget);

        boolean updated = budgetDao.update(budget);
        if (updated) {
            LoggerUtil.info("Обновлены фактические значения для бюджета с ID " + budgetId +
                    ": доход = " + actualIncome + ", расходы = " + actualExpenses +
                    ", чистый результат = " + budget.getNetResult());
        } else {
            LoggerUtil.warn("Не удалось обновить фактические значения для бюджета с ID " + budgetId);
        }

        return updated;
    }

    @Override
    public boolean deleteBudget(Long id) {
        checkDirectorPermission();

        MonthlyBudget budget = getBudgetById(id);

        boolean deleted = budgetDao.deleteById(id);
        if (deleted) {
            LoggerUtil.info("Удален бюджет с ID " + id + " на " + budget.getBudgetDate());
        } else {
            LoggerUtil.warn("Не удалось удалить бюджет с ID " + id);
        }

        return deleted;
    }

    @Override
    public BigDecimal getTotalPlannedIncome(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getPlannedIncome);
    }

    @Override
    public BigDecimal getTotalPlannedExpenses(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getPlannedExpenses);
    }

    @Override
    public BigDecimal getTotalActualIncome(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getActualIncome);
    }

    @Override
    public BigDecimal getTotalActualExpenses(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getActualExpenses);
    }

    @Override
    public BigDecimal getTotalNetResult(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getNetResult);
    }

    @Override
    public double getAvgBudgetExecutionRate(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        List<MonthlyBudget> budgets;
        try {
            budgets = getBudgetsByDateRange(startDate, endDate);
        } catch (BudgetNotFoundException e) {
            return 0.0;
        }

        List<MonthlyBudget> validBudgets = budgets.stream()
                .filter(b -> b.getPlannedExpenses().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (validBudgets.isEmpty()) {
            return 0.0;
        }

        double totalExecutionRate = validBudgets.stream()
                .mapToDouble(b -> calculateExecutionRate(b).doubleValue())
                .sum();

        return totalExecutionRate / validBudgets.size();
    }


    private void validateBudget(MonthlyBudget budget) {
        if (budget.getBudgetDate() == null) {
            throw new IllegalArgumentException("Дата бюджета должна быть указана");
        }

        validatePositiveAmount(budget.getPlannedIncome(), "Планируемый доход");
        validatePositiveAmount(budget.getPlannedExpenses(), "Планируемые расходы");

        if (budget.getActualIncome() != null) {
            validatePositiveAmount(budget.getActualIncome(), "Фактический доход");
        }

        if (budget.getActualExpenses() != null) {
            validatePositiveAmount(budget.getActualExpenses(), "Фактические расходы");
        }

        if (budget.getDirector() != null && budget.getDirector().getId() != null) {
            userService.getUserById(budget.getDirector().getId());
        }
    }

    private void validatePositiveAmount(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " должен быть неотрицательным числом");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания периода должны быть указаны");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
    }

    private void setupBudget(MonthlyBudget budget) {
        if (budget.getDirector() == null) {
            budget.setDirector(userService.getCurrentUser());
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (budget.getCreatedAt() == null) {
            budget.setCreatedAt(now);
        }
        if (budget.getUpdatedAt() == null) {
            budget.setUpdatedAt(now);
        }

        if (budget.getActualIncome() == null) {
            budget.setActualIncome(BigDecimal.ZERO);
        }
        if (budget.getActualExpenses() == null) {
            budget.setActualExpenses(BigDecimal.ZERO);
        }

        calculateNetResult(budget);
    }

    private void calculateNetResult(MonthlyBudget budget) {
        BigDecimal income = budget.getActualIncome() != null ? budget.getActualIncome() : BigDecimal.ZERO;
        BigDecimal expenses = budget.getActualExpenses() != null ? budget.getActualExpenses() : BigDecimal.ZERO;
        budget.setNetResult(income.subtract(expenses));
    }

    private BigDecimal calculateExecutionRate(MonthlyBudget budget) {
        return budget.getActualExpenses()
                .divide(budget.getPlannedExpenses(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal sumBudgetProperty(LocalDate startDate, LocalDate endDate,
                                         java.util.function.Function<MonthlyBudget, BigDecimal> propertyExtractor) {
        checkAuthentication();
        validateDateRange(startDate, endDate);

        List<MonthlyBudget> budgets;
        try {
            budgets = getBudgetsByDateRange(startDate, endDate);
        } catch (BudgetNotFoundException e) {
            return BigDecimal.ZERO;
        }

        return budgets.stream()
                .map(propertyExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<MonthlyBudget> findAndValidate(Supplier<List<MonthlyBudget>> supplier, String errorMessage) {
        List<MonthlyBudget> budgets = supplier.get();

        if (budgets.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new BudgetNotFoundException(errorMessage);
        }

        return budgets;
    }

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkDirectorPermission() {
        checkAuthentication();

        if (!userService.hasRole(ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только директор может управлять бюджетами");
        }
    }

    private void checkAccountantPermission() {
        checkAuthentication();

        if (!userService.hasRole(ROLE_ACCOUNTANT, ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только бухгалтер или директор может обновлять фактические значения бюджетов");
        }
    }
}