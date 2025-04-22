package service.impl;

import dao.impl.PayrollDao;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.PayrollNotFoundException;
import model.Payroll;
import model.User;
import service.PayrollService;
import service.UserService;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class PayrollServiceImpl implements PayrollService {
    private final PayrollDao payrollDao = new PayrollDao();
    private final UserService userService = new UserServiceImpl();

    private static final String ROLE_DIRECTOR = "Директор";
    private static final String ROLE_ACCOUNTANT = "Бухгалтер";

    @Override
    public List<Payroll> getAllPayrolls() {
        checkAuthentication();
        return findAndValidate(payrollDao::findAll, "Записи о зарплатах не найдены");
    }

    @Override
    public Payroll getPayrollById(Long id) {
        checkAuthentication();
        return payrollDao.findById(id)
                .orElseThrow(() -> new PayrollNotFoundException("Запись о зарплате с ID " + id + " не найдена"));
    }

    @Override
    public List<Payroll> getPayrollsByEmployee(Long employeeId) {
        checkAuthentication();
        userService.getUserById(employeeId);

        return findAndValidate(
                () -> payrollDao.findByEmployee(employeeId),
                "Записи о зарплатах для сотрудника с ID " + employeeId + " не найдены"
        );
    }

    @Override
    public List<Payroll> getPayrollsByAccountant(Long accountantId) {
        checkAuthentication();
        userService.getUserById(accountantId);

        return findAndValidate(
                () -> payrollDao.findByAccountant(accountantId),
                "Записи о зарплатах, созданные бухгалтером с ID " + accountantId + " не найдены"
        );
    }

    @Override
    public List<Payroll> getPayrollsByPeriod(LocalDate periodStart, LocalDate periodEnd) {
        checkAuthentication();
        validateDateRange(periodStart, periodEnd);

        return findAndValidate(
                () -> payrollDao.findByPeriod(Date.valueOf(periodStart), Date.valueOf(periodEnd)),
                "Записи о зарплатах за период с " + periodStart + " по " + periodEnd + " не найдены"
        );
    }

    @Override
    public List<Payroll> getPayrollsByPaymentStatus(boolean isPaid) {
        checkAuthentication();

        return findAndValidate(
                () -> payrollDao.findByPaymentStatus(isPaid),
                "Записи о " + (isPaid ? "выплаченных" : "невыплаченных") + " зарплатах не найдены"
        );
    }

    @Override
    public Long createPayroll(Payroll payroll) {
        checkAccountantPermission();
        validatePayroll(payroll);

        if (payroll.getTotalAmount() == null) {
            calculateTotalAmount(payroll);
        }

        if (payroll.getAccountant() == null) {
            payroll.setAccountant(userService.getCurrentUser());
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (payroll.getCreatedAt() == null) {
            payroll.setCreatedAt(now);
        }
        if (payroll.getUpdatedAt() == null) {
            payroll.setUpdatedAt(now);
        }

        Long id = payrollDao.save(payroll);
        if (id != null) {
            LoggerUtil.info("Создана новая запись о зарплате с ID " + id +
                    " для сотрудника " + payroll.getEmployee().getName() + " " +
                    payroll.getEmployee().getSurname() + ", сумма: " + payroll.getTotalAmount());
        }

        return id;
    }

    @Override
    public Long createPayroll(Long employeeId, Float hoursWorked, BigDecimal hourlyRate,
                              LocalDate periodStart, LocalDate periodEnd) {
        checkAccountantPermission();

        User employee = userService.getUserById(employeeId);
        validatePositiveFloat(hoursWorked);
        validatePositiveAmount(hourlyRate);
        validateDateRange(periodStart, periodEnd);

        BigDecimal totalAmount = hourlyRate.multiply(BigDecimal.valueOf(hoursWorked));

        User accountant = userService.getCurrentUser();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Payroll payroll = new Payroll(
                null,
                employee,
                accountant,
                hoursWorked,
                hourlyRate,
                totalAmount,
                periodStart,
                periodEnd,
                null,
                false,
                now,
                now
        );

        return createPayroll(payroll);
    }

    @Override
    public boolean updatePayroll(Payroll payroll) {
        checkAccountantPermission();

        if (payroll.getId() == null) {
            throw new IllegalArgumentException("ID записи о зарплате не может быть пустым при обновлении");
        }

        validatePayroll(payroll);

        Payroll existingPayroll = getPayrollById(payroll.getId());

        if (existingPayroll.isPaid() && !userService.hasRole( ROLE_DIRECTOR)) {
            throw new IllegalStateException("Невозможно изменить уже выплаченную зарплату");
        }

        if (!payroll.getHoursWorked().equals(existingPayroll.getHoursWorked()) ||
                !payroll.getHourlyRate().equals(existingPayroll.getHourlyRate())) {
            calculateTotalAmount(payroll);
        }

        boolean updated = payrollDao.update(payroll);
        if (updated) {
            LoggerUtil.info("Обновлена запись о зарплате с ID " + payroll.getId() +
                    " для сотрудника " + payroll.getEmployee().getName() + " " +
                    payroll.getEmployee().getSurname() + ", сумма: " + payroll.getTotalAmount());
        } else {
            LoggerUtil.warn("Не удалось обновить запись о зарплате с ID " + payroll.getId());
        }

        return updated;
    }

    @Override
    public boolean markAsPaid(Long payrollId, LocalDate paymentDate) {
        checkAccountantPermission();

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        Payroll payroll = getPayrollById(payrollId);

        if (payroll.isPaid()) {
            LoggerUtil.warn("Запись о зарплате с ID " + payrollId + " уже помечена как выплаченная");
            return false;
        }

        boolean updated = payrollDao.markAsPaid(payrollId, Date.valueOf(paymentDate));
        if (updated) {
            LoggerUtil.info("Запись о зарплате с ID " + payrollId +
                    " помечена как выплаченная с датой выплаты " + paymentDate);
        } else {
            LoggerUtil.warn("Не удалось пометить запись о зарплате с ID " + payrollId + " как выплаченную");
        }

        return updated;
    }

    @Override
    public boolean deletePayroll(Long id) {
        if (!userService.hasRole(ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только директор может удалять записи о зарплатах");
        }

        Payroll payroll = getPayrollById(id);

        if (payroll.isPaid()) {
            throw new IllegalStateException("Невозможно удалить уже выплаченную зарплату");
        }

        boolean deleted = payrollDao.deleteById(id);
        if (deleted) {
            LoggerUtil.info("Удалена запись о зарплате с ID " + id +
                    " для сотрудника " + payroll.getEmployee().getName() + " " +
                    payroll.getEmployee().getSurname());
        } else {
            LoggerUtil.warn("Не удалось удалить запись о зарплате с ID " + id);
        }

        return deleted;
    }

    @Override
    public BigDecimal getTotalSalaryAmount(LocalDate periodStart, LocalDate periodEnd) {
        checkAuthentication();
        validateDateRange(periodStart, periodEnd);

        List<Payroll> payrolls;
        try {
            payrolls = getPayrollsByPeriod(periodStart, periodEnd);
        } catch (PayrollNotFoundException e) {
            return BigDecimal.ZERO;
        }

        return payrolls.stream()
                .map(Payroll::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalSalaryByEmployee(Long employeeId, LocalDate periodStart, LocalDate periodEnd) {
        checkAuthentication();
        validateDateRange(periodStart, periodEnd);

        userService.getUserById(employeeId);

        List<Payroll> allPayrolls;
        try {
            allPayrolls = getPayrollsByPeriod(periodStart, periodEnd);
        } catch (PayrollNotFoundException e) {
            return BigDecimal.ZERO;
        }

        return allPayrolls.stream()
                .filter(p -> p.getEmployee().getId().equals(employeeId))
                .map(Payroll::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getAverageSalary(LocalDate periodStart, LocalDate periodEnd) {
        checkAuthentication();
        validateDateRange(periodStart, periodEnd);

        List<Payroll> payrolls;
        try {
            payrolls = getPayrollsByPeriod(periodStart, periodEnd);
        } catch (PayrollNotFoundException e) {
            return BigDecimal.ZERO;
        }

        if (payrolls.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Long> uniqueEmployees = payrolls.stream()
                .map(p -> p.getEmployee().getId())
                .distinct()
                .toList();

        if (uniqueEmployees.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAmount = payrolls.stream()
                .map(Payroll::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAmount.divide(BigDecimal.valueOf(uniqueEmployees.size()), 2, RoundingMode.HALF_UP);
    }

    private void validatePayroll(Payroll payroll) {
        if (payroll.getEmployee() == null || payroll.getEmployee().getId() == null) {
            throw new IllegalArgumentException("Сотрудник должен быть указан");
        }

        validatePositiveFloat(payroll.getHoursWorked());
        validatePositiveAmount(payroll.getHourlyRate());

        if (payroll.getPeriodStart() == null || payroll.getPeriodEnd() == null) {
            throw new IllegalArgumentException("Период должен быть указан полностью");
        }

        if (payroll.getPeriodStart().isAfter(payroll.getPeriodEnd())) {
            throw new IllegalArgumentException("Дата начала периода не может быть позже даты окончания");
        }

        userService.getUserById(payroll.getEmployee().getId());

        if (payroll.getAccountant() != null && payroll.getAccountant().getId() != null) {
            userService.getUserById(payroll.getAccountant().getId());
        }
    }

    private void calculateTotalAmount(Payroll payroll) {
        payroll.setTotalAmount(
                payroll.getHourlyRate().multiply(BigDecimal.valueOf(payroll.getHoursWorked()))
        );
    }

    private void validatePositiveFloat(Float value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Отработанные часы" + " должны быть положительным числом");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Почасовая ставка" + " должна быть положительным числом");
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

    private List<Payroll> findAndValidate(Supplier<List<Payroll>> supplier, String errorMessage) {
        List<Payroll> payrolls = supplier.get();

        if (payrolls.isEmpty()) {
            LoggerUtil.warn(errorMessage);
            throw new PayrollNotFoundException(errorMessage);
        }

        return payrolls;
    }

    private void checkAuthentication() {
        if (!userService.isAuthenticated()) {
            throw new AuthenticationException("Пользователь не авторизован");
        }
    }

    private void checkAccountantPermission() {
        checkAuthentication();

        if (!userService.hasRole(ROLE_ACCOUNTANT, ROLE_DIRECTOR)) {
            throw new AuthorizationException("Только бухгалтер или директор может управлять зарплатами");
        }
    }
}