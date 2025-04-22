package service;

import model.Payroll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PayrollService {
    List<Payroll> getAllPayrolls();

    Payroll getPayrollById(Long id);

    List<Payroll> getPayrollsByEmployee(Long employeeId);

    List<Payroll> getPayrollsByAccountant(Long accountantId);

    List<Payroll> getPayrollsByPeriod(LocalDate periodStart, LocalDate periodEnd);

    List<Payroll> getPayrollsByPaymentStatus(boolean isPaid);

    Long createPayroll(Payroll payroll);

    Long createPayroll(Long employeeId, Float hoursWorked, BigDecimal hourlyRate,
                       LocalDate periodStart, LocalDate periodEnd);

    boolean updatePayroll(Payroll payroll);

    boolean markAsPaid(Long payrollId, LocalDate paymentDate);

    boolean deletePayroll(Long id);

    BigDecimal getTotalSalaryAmount(LocalDate periodStart, LocalDate periodEnd);

    BigDecimal getTotalSalaryByEmployee(Long employeeId, LocalDate periodStart, LocalDate periodEnd);

    BigDecimal getAverageSalary(LocalDate periodStart, LocalDate periodEnd);
}
