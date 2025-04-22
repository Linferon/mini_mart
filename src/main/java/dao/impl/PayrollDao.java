package dao.impl;

import dao.Dao;
import model.Payroll;
import dao.mapper.PayrollMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

public class PayrollDao extends Dao<Payroll> {

    @Override
    protected String getTableName() {
        return "PAYROLLS";
    }

    @Override
    protected Function<ResultSet, Payroll> getMapper() {
        return PayrollMapper::mapRow;
    }

    public List<Payroll> findByEmployee(Long employeeId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE EMPLOYEE_ID = ?";
        return queryList(sql, employeeId);
    }

    public List<Payroll> findByAccountant(Long accountantId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ACCOUNTANT_ID = ?";
        return queryList(sql, accountantId);
    }

    public List<Payroll> findByPeriod(Date periodStart, Date periodEnd) {
        String sql = "SELECT * FROM " + getTableName() +
                " WHERE PERIOD_START >= ? AND PERIOD_END <= ?";
        return queryList(sql, periodStart, periodEnd);
    }

    public List<Payroll> findByPaymentStatus(boolean isPaid) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE IS_PAID = ?";
        return queryList(sql, isPaid);
    }

    public Long save(Payroll payroll) {
        if (payroll.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO " + getTableName() +
                    " (EMPLOYEE_ID, ACCOUNTANT_ID, HOURS_WORKED, HOURLY_RATE, " +
                    "TOTAL_AMOUNT, PERIOD_START, PERIOD_END, PAYMENT_DATE, " +
                    "IS_PAID, CREATED_AT, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Long id = insert(sql,
                    payroll.getEmployee().getId(),
                    payroll.getAccountant().getId(),
                    payroll.getHoursWorked(),
                    payroll.getHourlyRate(),
                    payroll.getTotalAmount(),
                    payroll.getPeriodStart(),
                    payroll.getPeriodEnd(),
                    payroll.getPaymentDate(),
                    payroll.isPaid(),
                    payroll.getCreatedAt() != null ? payroll.getCreatedAt() : now,
                    payroll.getUpdatedAt() != null ? payroll.getUpdatedAt() : now);
            if (id != null) {
                payroll.setId(id);
            }
            return id;
        } else {
            boolean updated = update(payroll);
            return updated ? payroll.getId() : null;
        }
    }

    public boolean update(Payroll payroll) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE " + getTableName() +
                " SET EMPLOYEE_ID = ?, ACCOUNTANT_ID = ?, HOURS_WORKED = ?, " +
                "HOURLY_RATE = ?, TOTAL_AMOUNT = ?, PERIOD_START = ?, " +
                "PERIOD_END = ?, PAYMENT_DATE = ?, IS_PAID = ?, " +
                "UPDATED_AT = ? WHERE ID = ?";
        return update(sql,
                payroll.getEmployee().getId(),
                payroll.getAccountant().getId(),
                payroll.getHoursWorked(),
                payroll.getHourlyRate(),
                payroll.getTotalAmount(),
                payroll.getPeriodStart(),
                payroll.getPeriodEnd(),
                payroll.getPaymentDate(),
                payroll.isPaid(),
                now,
                payroll.getId());
    }

    public boolean markAsPaid(Long id, Date paymentDate) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE " + getTableName() +
                " SET IS_PAID = ?, PAYMENT_DATE = ?, UPDATED_AT = ? WHERE ID = ?";
        return update(sql, true, paymentDate, now, id);
    }
}