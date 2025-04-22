package service.impl;

import dao.impl.IncomeDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.IncomeNotFoundException;
import model.Income;
import model.IncomeSource;
import model.User;
import service.IncomeService;
import service.IncomeSourceService;
import service.UserService;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class IncomeServiceImpl implements IncomeService {
    private final IncomeDao incomeDao = new IncomeDao();
    private final UserService userService = new UserServiceImpl();
    private final IncomeSourceService sourceService = new IncomeSourceServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";
    private static final String ROLE_ACCOUNTANT = "Бухгалтер";
    private static final String ROLE_CASHIER = "Кассир";

    @Override
    public List<Income> getAllIncomes() {
        checkAuthentication();
        return findAndValidate(incomeDao::findAll, "Доходы не найдены");
    }

    @Override
    public Income getIncomeById(Long id) {
        checkAuthentication();
        return incomeDao.findById(id)
                .orElseThrow(() -> new IncomeNotFoundException("Доход с ID " + id + " не найден"));
    }

    @Override
    public List<Income> getIncomesBySource(Long sourceId) {
        checkAuthentication();
        
        sourceService.getIncomeSourceById(sourceId);
        
        return findAndValidate(() -> incomeDao.findBySource(sourceId),
                "Доходы от источника с ID " + sourceId + " не найдены");
    }

    @Override
    public List<Income> getIncomesByAccountant(Long accountantId) {
        checkAuthentication();
        
        userService.getUserById(accountantId);
        
        return findAndValidate(() -> incomeDao.findByAccountant(accountantId),
                "Доходы, зарегистрированные бухгалтером с ID " + accountantId + " не найдены");
    }

    @Override
    public List<Income> getIncomesByDateRange(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);
        
        Timestamp start = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(endDate.atTime(23, 59, 59));
        
        return findAndValidate(() -> incomeDao.findByDateRange(start, end),
                "Доходы за период с " + startDate + " по " + endDate + " не найдены");
    }

    @Override
    public Long addIncome(Income income) {
        checkIncomeManagementPermission();
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
                    " от источника '" + income.getSource().getName() + 
                    "', сумма: " + income.getTotalAmount());
        }
        
        return id;
    }

    @Override
    public Long addIncome(Long sourceId, BigDecimal amount, LocalDate incomeDate) {
        checkIncomeManagementPermission();
        
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

    @Override
    public boolean updateIncome(Income income) {
        checkIncomeManagementPermission();
        
        if (income.getId() == null) {
            throw new IllegalArgumentException("ID дохода не может быть пустым при обновлении");
        }
        
        validateIncome(income);
        
        getIncomeById(income.getId());
        
        boolean updated = incomeDao.update(income);
        if (updated) {
            LoggerUtil.info("Обновлен доход с ID " + income.getId() + 
                    " от источника '" + income.getSource().getName() + 
                    "', сумма: " + income.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось обновить доход с ID " + income.getId());
        }
        
        return updated;
    }

    @Override
    public boolean deleteIncome(Long id) {
        if (!userService.hasRole(ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только директор может удалять доходы");
        }
        
        Income income = getIncomeById(id);
        
        boolean deleted = incomeDao.deleteById(id);
        if (deleted) {
            LoggerUtil.info("Удален доход с ID " + id + 
                    " от источника '" + income.getSource().getName() + 
                    "', сумма: " + income.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось удалить доход с ID " + id);
        }
        
        return deleted;
    }

    @Override
    public BigDecimal getTotalIncomeAmount(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);
        
        List<Income> incomes;
        try {
            incomes = getIncomesByDateRange(startDate, endDate);
        } catch (IncomeNotFoundException e) {
            return BigDecimal.ZERO;
        }
        
        return incomes.stream()
                .map(Income::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<IncomeSource, BigDecimal> getIncomesBySource(LocalDate startDate, LocalDate endDate) {
        checkAuthentication();
        validateDateRange(startDate, endDate);
        
        List<Income> incomes;
        try {
            incomes = getIncomesByDateRange(startDate, endDate);
        } catch (IncomeNotFoundException e) {
            return new HashMap<>();
        }
        
        Map<IncomeSource, BigDecimal> result = new HashMap<>();
        
        for (Income income : incomes) {
            IncomeSource source = income.getSource();
            BigDecimal currentAmount = result.getOrDefault(source, BigDecimal.ZERO);
            result.put(source, currentAmount.add(income.getTotalAmount()));
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
        
        BigDecimal totalAmount;
        try {
            totalAmount = getTotalIncomeAmount(startDate, endDate);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
        
        return totalAmount.divide(BigDecimal.valueOf(numberOfMonths), 2, RoundingMode.HALF_UP);
    }


    private void validateIncome(Income income) {
        if (income.getSource() == null || income.getSource().getId() == null) {
            throw new IllegalArgumentException("Источник дохода должен быть указан");
        }
        
        if (income.getTotalAmount() == null || income.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма дохода должна быть положительным числом");
        }
        
        sourceService.getIncomeSourceById(income.getSource().getId());
        
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
    
    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }
    
    private void checkIncomeManagementPermission() {
        checkAuthentication();
        
        if (!userService.hasRole(ROLE_ACCOUNTANT, ROLE_CASHIER, ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только бухгалтер, кассир или директор может управлять доходами");
        }
    }
}