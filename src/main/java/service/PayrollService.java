package service;

import dao.impl.PayrollDao;
import exception.nsee.PayrollNotFoundException;
import model.Payroll;
import model.User;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static util.EntityUtil.findAndValidate;
import static util.LoggerUtil.*;
import static util.ValidationUtil.*;

public class PayrollService {
    private static PayrollService instance;
    private final PayrollDao payrollDao;
    private final UserService userService;
    private final ExpenseService expenseService;

    private PayrollService() {
        payrollDao = new PayrollDao();
        userService = UserService.getInstance();
        expenseService = ExpenseService.getInstance();
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

    public void createPayroll(Payroll payroll) {
        validatePayroll(payroll);
        Long id = payrollDao.save(payroll);

        info("Создана новая запись о зарплате с ID " + id);
        if (Boolean.TRUE.equals(payroll.isPaid())) {
            expenseService.addSalaryExpense(payroll.getTotalAmount(), payroll.getPaymentDate());
        }
    }

    public void createPayroll(Long employeeId, Float hoursWorked, BigDecimal hourlyRate,
                              LocalDate periodStart, LocalDate periodEnd) {
        validatePositiveFloat(hoursWorked, "Часы работы должны быть положительным числом");
        validatePositiveAmount(hourlyRate, "Часовая ставка должна быть положительным числом");
        validateDateRange(periodStart, periodEnd);

        BigDecimal totalAmount = hourlyRate.multiply(BigDecimal.valueOf(hoursWorked));
        User employee = userService.getUserById(employeeId);
        User accountant = userService.getCurrentUser();

        Payroll payroll = new Payroll(
                employee,
                accountant,
                hoursWorked,
                hourlyRate,
                totalAmount,
                periodStart,
                periodEnd
        );

        createPayroll(payroll);
    }

    public boolean updatePayroll(Long payrollId, Long employeeId, Float hoursWorked, BigDecimal hourlyRate,
                                 LocalDate periodStart, LocalDate periodEnd) {

        Payroll existingPayroll = getPayrollById(payrollId);
        User employee = userService.getUserById(employeeId);
        BigDecimal oldTotalAmount = existingPayroll.getTotalAmount();
        boolean wasPaid = Boolean.TRUE.equals(existingPayroll.isPaid());
        LocalDate oldPaymentDate = existingPayroll.getPaymentDate() != null ?
                existingPayroll.getPaymentDate() : null;
        BigDecimal newTotalAmount = hourlyRate.multiply(BigDecimal.valueOf(hoursWorked));

        Payroll updatedPayroll = new Payroll(
                payrollId,
                employee,
                existingPayroll.getAccountant(),
                hoursWorked,
                hourlyRate,
                newTotalAmount,
                periodStart,
                periodEnd,
                existingPayroll.getPaymentDate(),
                existingPayroll.isPaid(),
                existingPayroll.getCreatedAt(),
                null
        );

        boolean updated = updatePayroll(updatedPayroll);

        if (updated && wasPaid && oldPaymentDate != null) {
            try {
                expenseService.updateSalaryExpense(oldTotalAmount, newTotalAmount, oldPaymentDate);
            } catch (Exception e) {
                error("Ошибка при обновлении расхода для зарплаты ID " + payrollId + ": " + e.getMessage(), e);
            }
        }

        return updated;
    }

    public boolean updatePayroll(Payroll payroll) {
        validatePayroll(payroll);

        boolean updated = payrollDao.update(payroll);
        if (updated) {
            info("Обновлена запись о зарплате с ID " + payroll.getId());
        } else {
            warn("Не удалось обновить запись о зарплате с ID " + payroll.getId());
        }

        return updated;
    }

    public void markAsPaid(Long payrollId, LocalDate paymentDate) {
        Payroll payroll = getPayrollById(payrollId);

        if (Boolean.TRUE.equals(payroll.isPaid())) {
            warn("Запись о зарплате с ID " + payrollId + " уже помечена как выплаченная");
            return;
        }

        boolean updated = payrollDao.markAsPaid(payrollId, Date.valueOf(paymentDate));
        if (updated) {
            info("Запись о зарплате с ID " + payrollId +
                    " помечена как выплаченная с датой выплаты " + paymentDate);

            Payroll updatedPayroll = getPayrollById(payrollId);
            expenseService.addSalaryExpense(updatedPayroll.getTotalAmount(), updatedPayroll.getPaymentDate());
        } else {
            warn("Не удалось пометить запись о зарплате с ID " + payrollId + " как выплаченную");
        }
    }

    public void deletePayroll(Long id) {
        Payroll payroll = getPayrollById(id);

        boolean deleted;
        boolean isPaid = Boolean.TRUE.equals(payroll.isPaid());

        if (isPaid) {
            BigDecimal totalAmount = payroll.getTotalAmount();
            LocalDate paymentDate = payroll.getPaymentDate();

            deleted = payrollDao.deleteById(id);

            if (deleted) {
                try {
                    expenseService.deleteSalaryExpense(totalAmount, paymentDate);
                } catch (Exception e) {
                    error("Ошибка при удалении связанных записей для зарплаты ID " + id + ": " + e.getMessage(), e);
                }
            }
        } else {
            deleted = payrollDao.deleteById(id);
        }

        if (deleted) {
            info("Удалена запись о зарплате с ID " + id);
        } else {
            warn("Не удалось удалить запись о зарплате с ID " + id);
        }
    }

    private void validatePayroll(Payroll payroll) {
        Objects.requireNonNull(payroll);
        validatePositiveFloat(payroll.getHoursWorked());
        validatePositiveAmount(payroll.getHourlyRate());
        validateDateRange(payroll.getPeriodStart(), payroll.getPeriodEnd());
        userService.getUserById(payroll.getEmployee().getId());
    }
}