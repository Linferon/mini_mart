package dao.impl;

import dao.Dao;
import model.Payroll;
import dao.mapper.PayrollMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;
public class PayrollDao extends Dao<Payroll> {

    @Override
    protected String getTableName() {
        return PAYROLL_TABLE;
    }

    @Override
    protected Function<ResultSet, Payroll> getMapper() {
        return PayrollMapper::mapRow;
    }

    @Override
    public Optional<Payroll> findById(Long id) {
        String sql = "SELECT p.*, " +
                "e.NAME as EMPLOYEE_NAME, e.SURNAME as EMPLOYEE_SURNAME, " +
                "a.NAME as ACCOUNTANT_NAME, a.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + PAYROLL_TABLE + " p " +
                "LEFT JOIN " + USER_TABLE + " e ON p.EMPLOYEE_ID = e.ID " +
                "LEFT JOIN " + USER_TABLE + " a ON p.ACCOUNTANT_ID = a.ID " +
                "WHERE p.ID = ?";
        return querySingle(sql, id);
    }

    @Override
    public List<Payroll> findAll() {
        String sql = "SELECT p.*, " +
                "e.NAME as EMPLOYEE_NAME, e.SURNAME as EMPLOYEE_SURNAME, " +
                "a.NAME as ACCOUNTANT_NAME, a.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + PAYROLL_TABLE + " p " +
                "LEFT JOIN " + USER_TABLE + " e ON p.EMPLOYEE_ID = e.ID " +
                "LEFT JOIN " + USER_TABLE + " a ON p.ACCOUNTANT_ID = a.ID";
        return queryList(sql);
    }

    public List<Payroll> findUnpaidPayrolls() {
        String sql = "SELECT p.*, " +
                "e.NAME as EMPLOYEE_NAME, e.SURNAME as EMPLOYEE_SURNAME, " +
                "a.NAME as ACCOUNTANT_NAME, a.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + PAYROLL_TABLE + " p " +
                "LEFT JOIN " + USER_TABLE + " e ON p.EMPLOYEE_ID = e.ID " +
                "LEFT JOIN " + USER_TABLE + " a ON p.ACCOUNTANT_ID = a.ID " +
                "WHERE p.IS_PAID = FALSE";
        return queryList(sql);
    }

    public List<Payroll> findByPeriod(Date periodStart, Date periodEnd) {
        String sql = "SELECT p.*, " +
                "e.NAME as EMPLOYEE_NAME, e.SURNAME as EMPLOYEE_SURNAME, " +
                "a.NAME as ACCOUNTANT_NAME, a.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + PAYROLL_TABLE + " p " +
                "LEFT JOIN " + USER_TABLE + " e ON p.EMPLOYEE_ID = e.ID " +
                "LEFT JOIN " + USER_TABLE + " a ON p.ACCOUNTANT_ID = a.ID " +
                "WHERE p.PERIOD_START >= ? AND p.PERIOD_END <= ?";
        return queryList(sql, periodStart, periodEnd);
    }

    public Long save(Payroll payroll) {
        if (payroll.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO " + PAYROLL_TABLE +
                    " (EMPLOYEE_ID, ACCOUNTANT_ID, HOURS_WORKED, HOURLY_RATE, " +
                    " PERIOD_START, PERIOD_END, PAYMENT_DATE, " +
                    "IS_PAID, CREATED_AT, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Long id = insert(sql,
                    payroll.getEmployee().getId(),
                    payroll.getAccountant().getId(),
                    payroll.getHoursWorked(),
                    payroll.getHourlyRate(),
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

        String sql = "UPDATE " + PAYROLL_TABLE +
                " SET EMPLOYEE_ID = ?, ACCOUNTANT_ID = ?, HOURS_WORKED = ?, " +
                "HOURLY_RATE = ?, PERIOD_START = ?, " +
                "PERIOD_END = ?, PAYMENT_DATE = ?, IS_PAID = ?, " +
                "UPDATED_AT = ? WHERE ID = ?";
        return update(sql,
                payroll.getEmployee().getId(),
                payroll.getAccountant().getId(),
                payroll.getHoursWorked(),
                payroll.getHourlyRate(),
                payroll.getPeriodStart(),
                payroll.getPeriodEnd(),
                payroll.getPaymentDate(),
                payroll.isPaid(),
                now,
                payroll.getId());
    }

    public boolean markAsPaid(Long id, Date paymentDate) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE " + PAYROLL_TABLE +
                " SET IS_PAID = ?, PAYMENT_DATE = ?, UPDATED_AT = ? WHERE ID = ?";
        return update(sql, true, paymentDate, now, id);
    }
}