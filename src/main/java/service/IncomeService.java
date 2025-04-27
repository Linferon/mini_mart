package service;

import dao.impl.IncomeDao;
import exception.nsee.IncomeNotFoundException;
import model.Income;
import model.IncomeSource;
import model.User;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

public class IncomeService {
    private static IncomeService instance;
    private final IncomeDao incomeDao = new IncomeDao();
    private final UserService userService = UserService.getInstance();
    private final IncomeSourceService sourceService = IncomeSourceService.getInstance();

    private IncomeService() {
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


    public List<Income> getIncomesByAccountant(Long accountantId) {
        userService.getUserById(accountantId);

        return findAndValidate(() -> incomeDao.findByAccountant(accountantId),
                "Доходы, зарегистрированные бухгалтером с ID " + accountantId + " не найдены");
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

        if (income.getIncomeDate() == null) {
            income.setIncomeDate(Timestamp.valueOf(LocalDateTime.now()));
        }

        if (income.getAccountant() == null) {
            income.setAccountant(userService.getCurrentUser());
        }

        Long id = incomeDao.save(income);
        if (id != null) {
            LoggerUtil.info("Добавлен новый доход с ID " + id +
                    " от источника '" + income.getSource().name() +
                    "', сумма: " + income.getTotalAmount());
        }

        return id;
    }


    public Long addIncome(Long sourceId, BigDecimal amount, LocalDate incomeDate) {
        if (sourceId == null) {
            throw new IllegalArgumentException("ID источника дохода должен быть указан");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма дохода должна быть положительным числом");
        }

        IncomeSource source = sourceService.getIncomeSourceById(sourceId);
        User accountant = userService.getCurrentUser();

        Income income = new Income(
                null,
                source,
                amount,
                incomeDate != null ? Timestamp.valueOf(incomeDate.atStartOfDay()) : Timestamp.valueOf(LocalDateTime.now()),
                accountant
        );

        return addIncome(income);
    }


    public boolean updateIncome(Income income) {
        if (income.getId() == null) {
            throw new IllegalArgumentException("ID дохода не может быть пустым при обновлении");
        }

        validateIncome(income);

        getIncomeById(income.getId());

        boolean updated = incomeDao.update(income);
        if (updated) {
            LoggerUtil.info("Обновлен доход с ID " + income.getId() +
                    " от источника '" + income.getSource().name() +
                    "', сумма: " + income.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось обновить доход с ID " + income.getId());
        }

        return updated;
    }


    public boolean deleteIncome(Long id) {
        Income income = getIncomeById(id);

        boolean deleted = incomeDao.deleteById(id);
        if (deleted) {
            LoggerUtil.info("Удален доход с ID " + id +
                    " от источника '" + income.getSource().name() +
                    "', сумма: " + income.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось удалить доход с ID " + id);
        }

        return deleted;
    }

    private void validateIncome(Income income) {
        if (income.getSource() == null || income.getSource().id() == null) {
            throw new IllegalArgumentException("Источник дохода должен быть указан");
        }

        if (income.getTotalAmount() == null || income.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма дохода должна быть положительным числом");
        }

        sourceService.getIncomeSourceById(income.getSource().id());

        if (income.getAccountant() != null && income.getAccountant().getId() != null) {
            userService.getUserById(income.getAccountant().getId());
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

    private List<Income> findAndValidate(Supplier<List<Income>> supplier, String errorMessage) {
        List<Income> incomes = supplier.get();

        if (incomes.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new IncomeNotFoundException(errorMessage);
        }

        return incomes;
    }
}