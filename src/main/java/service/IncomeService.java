package service;

import dao.impl.IncomeDao;
import exception.nsee.IncomeNotFoundException;
import model.Income;
import model.IncomeSource;
import model.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static util.EntityUtil.findAndValidate;
import static util.LoggerUtil.*;
import static util.ValidationUtil.*;

public class IncomeService {
    private static IncomeService instance;
    private final IncomeDao incomeDao;
    private final UserService userService;
    private final IncomeSourceService sourceService;
    private final MonthlyBudgetService budgetService;

    private IncomeService() {
        this(new IncomeDao(),
                UserService.getInstance(),
                IncomeSourceService.getInstance(),
                MonthlyBudgetService.getInstance());
    }

    IncomeService(IncomeDao incomeDao,
                  UserService userService,
                  IncomeSourceService sourceService,
                  MonthlyBudgetService budgetService) {
        this.incomeDao = incomeDao;
        this.userService = userService;
        this.sourceService = sourceService;
        this.budgetService = budgetService;
    }

    public static synchronized IncomeService getInstance() {
        if (instance == null) {
            instance = new IncomeService();
        }
        return instance;
    }

    public List<Income> getAllIncomes() {
        return findAndValidate(incomeDao::findAll, "Доходы не найдены");
    }

    public Income getIncomeById(Long id) {
        return incomeDao.findById(id)
                .orElseThrow(() -> new IncomeNotFoundException("Доход с ID " + id + " не найден"));
    }

    public List<Income> getIncomesBySource(Long sourceId) {
        sourceService.getIncomeSourceById(sourceId);

        return findAndValidate(() -> incomeDao.findBySource(sourceId),
                "Доходы от источника с ID " + sourceId + " не найдены");
    }

    public List<Income> getIncomesByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        Timestamp start = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(endDate.atTime(23, 59, 59));

        return findAndValidate(() -> incomeDao.findByDateRange(start, end),
                "Доходы за период с " + startDate + " по " + endDate + " не найдены");
    }

    public BigDecimal getTotalIncome(List<Income> incomes) {
        return incomes.stream()
                .map(Income::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long addIncome(Income income) {
        validateIncome(income);

        Long id = incomeDao.save(income);
        if (id != null) {
            info("Добавлен новый доход с ID " + id);

            try {
                LocalDate date = income.getIncomeDate().toLocalDateTime().toLocalDate();
                BigDecimal total = income.getTotalAmount();
                budgetService.updateMonthlyBudgetIncome(date, total);
            } catch (Exception e) {
                error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
            }
        }

        return id;
    }

    public void addIncome(Long sourceId, BigDecimal amount, LocalDate incomeDate) {
        validateId(sourceId, "ID источника дохода должен быть указан");
        validatePositiveAmount(amount, "Сумма дохода должна быть положительным числом");

        IncomeSource source = sourceService.getIncomeSourceById(sourceId);
        User accountant = userService.getCurrentUser();
        Timestamp date = incomeDate != null ? Timestamp.valueOf(incomeDate.atStartOfDay()) : Timestamp.valueOf(LocalDateTime.now());

        Income income = new Income(
                null,
                source,
                amount,
                date,
                accountant
        );

        addIncome(income);
    }

    public boolean updateIncome(Income income) {
        Income oldIncome = getIncomeById(income.getId());
        validateIncome(income);

        boolean updated = incomeDao.update(income);

        if (updated) {
            info("Обновлен доход с ID " + income.getId());

            try {
                if (oldIncome.getTotalAmount().compareTo(income.getTotalAmount()) != 0) {
                    budgetService.updateMonthlyBudgetIncome(
                            oldIncome.getIncomeDate().toLocalDateTime().toLocalDate(),
                            oldIncome.getTotalAmount().negate());

                    budgetService.updateMonthlyBudgetIncome(
                            income.getIncomeDate().toLocalDateTime().toLocalDate(),
                            income.getTotalAmount());
                } else if (!oldIncome.getIncomeDate().equals(income.getIncomeDate())) {
                    budgetService.updateMonthlyBudgetIncome(
                            oldIncome.getIncomeDate().toLocalDateTime().toLocalDate(),
                            oldIncome.getTotalAmount().negate());

                    budgetService.updateMonthlyBudgetIncome(
                            income.getIncomeDate().toLocalDateTime().toLocalDate(),
                            income.getTotalAmount());
                }
            } catch (Exception e) {
                error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
            }
        } else {
            warn("Не удалось обновить доход с ID " + income.getId());
        }

        return updated;
    }

    public boolean updateIncome(Long incomeId, Long sourceId, BigDecimal amount, LocalDate incomeDate) {
        Income income = getIncomeById(incomeId);

        IncomeSource incomeSource = sourceService.getIncomeSourceById(sourceId);
        income.setSource(incomeSource);
        income.setTotalAmount(amount);
        income.setIncomeDate(Timestamp.valueOf(incomeDate.atStartOfDay()));

        return updateIncome(income);
    }

    public boolean deleteIncome(Long id) {
        Income income = getIncomeById(id);

        boolean deleted = incomeDao.deleteById(id);

        if (deleted) {
            info("Удален доход с ID " + id);
            try {
                LocalDate incomeDate = income.getIncomeDate().toLocalDateTime().toLocalDate();
                BigDecimal amount = income.getTotalAmount().negate();
                budgetService.updateMonthlyBudgetIncome(incomeDate, amount);
            } catch (Exception e) {
                error("Ошибка при обновлении месячного бюджета: " + e.getMessage(), e);
            }
        } else {
            warn("Не удалось удалить доход с ID " + id);
        }
        return deleted;
    }

    private void validateIncome(Income income) {
        validateId(income.getSource().id(), "Источник дохода должен быть указан");
        validatePositiveAmount(income.getTotalAmount(), "Сумма дохода должна быть положительным числом");
        sourceService.getIncomeSourceById(income.getSource().id());
        userService.getUserById(income.getAccountant().getId());
    }
}