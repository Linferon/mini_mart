package service;

import dao.impl.MonthlyBudgetDao;
import exception.nsee.BudgetNotFoundException;
import model.MonthlyBudget;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.math.BigDecimal.ZERO;

public class MonthlyBudgetService {
    private static MonthlyBudgetService instance;
    private final MonthlyBudgetDao budgetDao;
    private final UserService userService;

    private MonthlyBudgetService() {
        this.budgetDao = new MonthlyBudgetDao();
        this.userService = UserService.getInstance();
    }

    public static synchronized MonthlyBudgetService getInstance() {
        if (instance == null) {
            instance = new MonthlyBudgetService();
        }
        return instance;
    }

    public List<MonthlyBudget> getAllBudgets() {
        return findAndValidate(budgetDao::findAll, "Бюджеты не найдены");
    }

    public MonthlyBudget getBudgetById(Long id) {
        return budgetDao.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException("Бюджет с ID " + id + " не найден"));
    }

    public MonthlyBudget getBudgetByDate(LocalDate date) {
        return budgetDao.findByDate(Date.valueOf(date))
                .orElseThrow(() -> new BudgetNotFoundException("Не было найдено записей на эту дату!"));
    }

    public List<MonthlyBudget> getBudgetsByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        return findAndValidate(
                () -> budgetDao.findByDateRange(Date.valueOf(startDate), Date.valueOf(endDate)),
                "Бюджеты за период с " + startDate + " по " + endDate + " не найдены"
        );
    }

    public void createBudget(MonthlyBudget budget) {
        validateBudget(budget);
        checkBudgetDateNotExists(budget);
        setupBudget(budget);

        Long id = budgetDao.save(budget);
        if (id != null) {
            LoggerUtil.info("Создан новый бюджет с ID " + id + " на " + budget.getBudgetDate());
        }
    }

    public void createBudget(LocalDate budgetDate, BigDecimal plannedIncome, BigDecimal plannedExpenses) {
        if (budgetDate == null) {
            throw new IllegalArgumentException("Дата бюджета должна быть указана");
        }

        validatePositiveAmount(plannedIncome, "Планируемый доход");
        validatePositiveAmount(plannedExpenses, "Планируемые расходы");

        MonthlyBudget budget = new MonthlyBudget(
                budgetDate,
                plannedIncome,
                plannedExpenses,
                userService.getCurrentUser()
        );

        createBudget(budget);
    }

    public MonthlyBudget getOrCreateMonthlyBudget(LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);

        try {
            return getBudgetByDate(firstDayOfMonth);
        } catch (BudgetNotFoundException e) {
            return createNewMonthlyBudget(firstDayOfMonth);
        }
    }

    private MonthlyBudget createNewMonthlyBudget(LocalDate firstDayOfMonth) {
        LoggerUtil.info("Создание нового месячного бюджета на " + firstDayOfMonth);

        MonthlyBudget budget = new MonthlyBudget(
                null,
                firstDayOfMonth,
                ZERO,
                ZERO,
                ZERO,
                ZERO,
                ZERO,
                null,
                null,
                userService.getCurrentUser()
        );

        createBudget(budget);
        return budget;
    }

    public boolean updateBudget(Long budgetId, BigDecimal plannedIncome, BigDecimal plannedExpenses, LocalDate budgetDate) {
        MonthlyBudget budget = getBudgetById(budgetId);

        budget.setPlannedIncome(plannedIncome);
        budget.setPlannedExpenses(plannedExpenses);
        budget.setBudgetDate(budgetDate);

        validateBudget(budget);
        checkBudgetDateNotExistsForUpdate(budget);

        boolean updated = budgetDao.update(budget);
        logBudgetUpdate(updated, budget);
        return updated;
    }

    public void updateActualValues(Long budgetId, BigDecimal actualIncome, BigDecimal actualExpenses) {
        validatePositiveAmount(actualIncome, "Фактический доход");
        validatePositiveAmount(actualExpenses, "Фактические расходы");

        MonthlyBudget budget = getBudgetById(budgetId);
        budget.setActualIncome(actualIncome);
        budget.setActualExpenses(actualExpenses);

        boolean updated = budgetDao.update(budget);
        if (updated) {
            LoggerUtil.info("Обновлены фактические значения для бюджета с ID " + budgetId +
                    ": доход = " + actualIncome + ", расходы = " + actualExpenses +
                    ", чистый результат = " + budget.getNetResult());
        } else {
            LoggerUtil.warn("Не удалось обновить фактические значения для бюджета с ID " + budgetId);
        }
    }

    public void updateMonthlyBudgetIncome(LocalDate date, BigDecimal amount) {
        try {
            MonthlyBudget budget = getOrCreateMonthlyBudget(date);
            BigDecimal newActualIncome = budget.getActualIncome().add(amount);

            updateActualValues(budget.getId(), newActualIncome, budget.getActualExpenses());

            LoggerUtil.info("Обновлен месячный бюджет на " + date.withDayOfMonth(1) +
                    ", новый фактический доход: " + newActualIncome);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
        }
    }

    public void updateMonthlyBudgetExpense(LocalDate date, BigDecimal amount) {
        try {
            MonthlyBudget budget = getOrCreateMonthlyBudget(date);
            BigDecimal newActualExpenses = budget.getActualExpenses().add(amount);

            updateActualValues(budget.getId(), budget.getActualIncome(), newActualExpenses);

            LoggerUtil.info("Обновлен месячный бюджет на " + date.withDayOfMonth(1) +
                    ", новые фактические расходы: " + newActualExpenses);
        } catch (Exception e) {
            LoggerUtil.error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
        }
    }

    private void checkBudgetDateNotExists(MonthlyBudget budget) {
        try {
            getBudgetByDate(budget.getBudgetDate());
            throw new IllegalArgumentException("Бюджет на " + budget.getBudgetDate() + " уже существует");
        } catch (BudgetNotFoundException ignored) {
        }
    }

    private void checkBudgetDateNotExistsForUpdate(MonthlyBudget budget) {
        try {
            MonthlyBudget existingBudget = getBudgetByDate(budget.getBudgetDate());
            if (!existingBudget.getId().equals(budget.getId())) {
                throw new IllegalArgumentException("Бюджет на " + budget.getBudgetDate() + " уже существует");
            }
        } catch (BudgetNotFoundException ignored) {
        }
    }

    private void logBudgetUpdate(boolean updated, MonthlyBudget budget) {
        if (updated) {
            LoggerUtil.info("Обновлен бюджет с ID " + budget.getId() + " на " + budget.getBudgetDate());
        } else {
            LoggerUtil.warn("Не удалось обновить бюджет с ID " + budget.getId());
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
            budget.setActualIncome(ZERO);
        }
        if (budget.getActualExpenses() == null) {
            budget.setActualExpenses(ZERO);
        }
    }

    private void validateBudget(MonthlyBudget budget) {
        if (budget.getBudgetDate() == null) {
            throw new IllegalArgumentException("Дата бюджета должна быть указана");
        }

        validatePositiveAmount(budget.getPlannedIncome(), "Планируемый доход");
        validatePositiveAmount(budget.getPlannedExpenses(), "Планируемые расходы");

        Optional.ofNullable(budget.getActualIncome())
                .ifPresent(income -> validatePositiveAmount(income, "Фактический доход"));

        Optional.ofNullable(budget.getActualExpenses())
                .ifPresent(expenses -> validatePositiveAmount(expenses, "Фактические расходы"));

        if (budget.getDirector() != null && budget.getDirector().getId() != null) {
            userService.getUserById(budget.getDirector().getId());
        }
    }

    private void validatePositiveAmount(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(ZERO) < 0) {
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

    private BigDecimal sumBudgetProperty(LocalDate startDate, LocalDate endDate,
                                         Function<MonthlyBudget, BigDecimal> propertyExtractor) {
        validateDateRange(startDate, endDate);

        List<MonthlyBudget> budgets;
        try {
            budgets = getBudgetsByDateRange(startDate, endDate);
        } catch (BudgetNotFoundException e) {
            return ZERO;
        }

        return budgets.stream()
                .map(propertyExtractor)
                .reduce(ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPlannedIncome(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getPlannedIncome);
    }

    public BigDecimal getTotalPlannedExpenses(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getPlannedExpenses);
    }

    public BigDecimal getTotalActualIncome(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getActualIncome);
    }

    public BigDecimal getTotalActualExpenses(LocalDate startDate, LocalDate endDate) {
        return sumBudgetProperty(startDate, endDate, MonthlyBudget::getActualExpenses);
    }


    private List<MonthlyBudget> findAndValidate(Supplier<List<MonthlyBudget>> supplier, String errorMessage) {
        List<MonthlyBudget> budgets = supplier.get();

        if (budgets.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new BudgetNotFoundException(errorMessage);
        }

        return budgets;
    }
}