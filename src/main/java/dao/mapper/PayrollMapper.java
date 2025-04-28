package dao.mapper;

import exception.DatabaseMapException;
import model.Payroll;
import model.User;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

import static util.LoggerUtil.error;

public class PayrollMapper {
    private PayrollMapper() {}

    public static Payroll mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            Long employeeId = rs.getLong("EMPLOYEE_ID");
            Long accountantId = rs.getLong("ACCOUNTANT_ID");
            Float hoursWorked = rs.getFloat("HOURS_WORKED");
            BigDecimal hourlyRate = rs.getBigDecimal("HOURLY_RATE");
            BigDecimal totalAmount = rs.getBigDecimal("TOTAL_AMOUNT");

            LocalDate periodStart = null;
            if (rs.getDate("PERIOD_START") != null) {
                periodStart = rs.getDate("PERIOD_START").toLocalDate();
            }

            LocalDate periodEnd = null;
            if (rs.getDate("PERIOD_END") != null) {
                periodEnd = rs.getDate("PERIOD_END").toLocalDate();
            }

            LocalDate paymentDate = null;
            if (rs.getDate("PAYMENT_DATE") != null) {
                paymentDate = rs.getDate("PAYMENT_DATE").toLocalDate();
            }

            Boolean isPaid = rs.getBoolean("IS_PAID");
            Timestamp createdAt = rs.getTimestamp("CREATED_AT");
            Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");

            String employeeName = rs.getString("EMPLOYEE_NAME");
            String employeeSurname = null;
            String accountantName= rs.getString("ACCOUNTANT_NAME");
            String accountantSurname = null;

            try {
                employeeSurname = rs.getString("EMPLOYEE_SURNAME");
                accountantSurname = rs.getString("ACCOUNTANT_SURNAME");
            } catch (SQLException ignored) {}

            User employee = new User(
                    employeeId,
                    employeeName,
                    employeeSurname
            );

            User accountant = new User(
                    accountantId,
                    accountantName,
                    accountantSurname);

            return new Payroll(
                    id,
                    employee,
                    accountant,
                    hoursWorked,
                    hourlyRate,
                    totalAmount,
                    periodStart,
                    periodEnd,
                    paymentDate,
                    isPaid,
                    createdAt,
                    updatedAt
            );
        } catch (SQLException e) {
            error("Error mapping payroll from ResultSet", e);
            throw new DatabaseMapException("Error mapping payroll");
        }
    }
}