package service;

import dao.impl.PayrollDao;
import exception.nsee.PayrollNotFoundException;
import model.Payroll;
import model.User;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class PayrollService {
    private static PayrollService instance;
    private final PayrollDao payrollDao = new PayrollDao();
    private final UserService userService = UserService.getInstance();

    private PayrollService() {
    }

    public static synchronized PayrollService getInstance() {
        if (instance == null) {
            instance = new PayrollService();
        }
        return instance;
    }

    public List<Payroll> getAllPayrolls() {
        return findAndValidate(payrollDao::findAll, "Записи о зарплатах не найдены");
    }


    public Payroll getPayrollById(Long id) {
        return payrollDao.findById(id)
                .orElseThrow(() -> new PayrollNotFoundException("Запись о зарплате с ID " + id + " не найдена"));
    }

    public List<Payroll> getUnpaidPayrolls() {
       return findAndValidate(
                payrollDao::findUnpaidPayrolls,
                "Нет невыплаченных зарплат!"
        );
    }

    public List<Payroll> getPayrollsByPeriod(LocalDate periodStart, LocalDate periodEnd) {
        validateDateRange(periodStart, periodEnd);

        return findAndValidate(
                () -> payrollDao.findByPeriod(Date.valueOf(periodStart), Date.valueOf(periodEnd)),
                "Записи о зарплатах за период с " + periodStart + " по " + periodEnd + " не найдены"
        );
    }

    public Long createPayroll(Payroll payroll) {
        validatePayroll(payroll);

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

    public Long createPayroll(Long employeeId, Float hoursWorked, BigDecimal hourlyRate,
                              LocalDate periodStart, LocalDate periodEnd) {
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

    public boolean updatePayroll(Payroll payroll) {
        if (payroll.getId() == null) {
            throw new IllegalArgumentException("ID записи о зарплате не может быть пустым при обновлении");
        }

        validatePayroll(payroll);

        getPayrollById(payroll.getId());

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

    public boolean markAsPaid(Long payrollId, LocalDate paymentDate) {
        Payroll payroll = getPayrollById(payrollId);

        if (Boolean.TRUE.equals(payroll.isPaid())) {
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

    public boolean deletePayroll(Long id) {
        Payroll payroll = getPayrollById(id);

        if (Boolean.TRUE.equals(payroll.isPaid())) {
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
}