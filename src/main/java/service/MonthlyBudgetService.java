package service;

import dao.impl.MonthlyBudgetDao;
import exception.nsee.BudgetNotFoundException;
import model.MonthlyBudget;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class MonthlyBudgetService {
    private static MonthlyBudgetService instance;
    private final MonthlyBudgetDao budgetDao = new MonthlyBudgetDao();
    private final UserService userService = UserService.getInstance();

    private MonthlyBudgetService() {
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

    public Long createBudget(MonthlyBudget budget) {
        validateBudget(budget);

        try {
            getBudgetByDate(budget.getBudgetDate());
            throw new IllegalArgumentException("Бюджет на " + budget.getBudgetDate() + " уже существует");
        } catch (BudgetNotFoundException ignored) {
        }

        setupBudget(budget);

        Long id = budgetDao.save(budget);
        if (id != null) {
            LoggerUtil.info("Создан новый бюджет с ID " + id + " на " + budget.getBudgetDate());
        }

        return id;
    }


    public Long createBudget(LocalDate budgetDate, BigDecimal plannedIncome, BigDecimal plannedExpenses) {
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
                BigDecimal.ZERO,
                null,
                null,
                userService.getCurrentUser()
        );

        return createBudget(budget);
    }


    public boolean updateBudget(MonthlyBudget budget) {
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
        } catch (BudgetNotFoundException ignored) {
        }

        boolean updated = budgetDao.update(budget);
        if (updated) {
            LoggerUtil.info("Обновлен бюджет с ID " + budget.getId() + " на " + budget.getBudgetDate());
        } else {
            LoggerUtil.warn("Не удалось обновить бюджет с ID " + budget.getId());
        }

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

    public boolean deleteBudget(Long id) {
        MonthlyBudget budget = getBudgetById(id);

        boolean deleted = budgetDao.deleteById(id);
        if (deleted) {
            LoggerUtil.info("Удален бюджет с ID " + id + " на " + budget.getBudgetDate());
        } else {
            LoggerUtil.warn("Не удалось удалить бюджет с ID " + id);
        }

        return deleted;
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

    public BigDecimal getAvgBudgetExecutionRate(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<MonthlyBudget> budgets;
        try {
            budgets = getBudgetsByDateRange(startDate, endDate);
        } catch (BudgetNotFoundException e) {
            return BigDecimal.ZERO;
        }

        List<MonthlyBudget> validBudgets = budgets.stream()
                .filter(b -> b.getPlannedExpenses().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (validBudgets.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double totalExecutionRate = validBudgets.stream()
                .mapToDouble(b -> calculateExecutionRate(b).doubleValue())
                .sum();

        return BigDecimal.valueOf(totalExecutionRate / validBudgets.size());
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

    public MonthlyBudget getOrCreateMonthlyBudget(LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);

        try {
            return getBudgetByDate(firstDayOfMonth);
        } catch (Exception e) {
            LoggerUtil.info("Создание нового месячного бюджета на " + firstDayOfMonth);

            MonthlyBudget budget = new MonthlyBudget(
                    null,
                    firstDayOfMonth,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    null,
                    userService.getCurrentUser()
            );

            Long budgetId = createBudget(budget);
            if (budgetId != null) {
                budget.setId(budgetId);
                return budget;
            } else {
                throw new IllegalArgumentException("Не удалось создать месячный бюджет");
            }
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

    }

    private BigDecimal calculateExecutionRate(MonthlyBudget budget) {
        return budget.getActualExpenses()
                .divide(budget.getPlannedExpenses(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal sumBudgetProperty(LocalDate startDate, LocalDate endDate,
                                         java.util.function.Function<MonthlyBudget, BigDecimal> propertyExtractor) {
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
}
