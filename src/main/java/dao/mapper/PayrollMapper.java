package dao.mapper;

import dao.impl.UserDao;
import exception.DatabaseMapException;
import model.Payroll;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PayrollMapper {
    private static final UserDao userDao = new UserDao();

    private PayrollMapper() {}

    public static Payroll mapRow(ResultSet rs) {
        try {
            Long employeeId = rs.getLong("EMPLOYEE_ID");
            Long accountantId = rs.getLong("ACCOUNTANT_ID");

            User employee = userDao.findById(employeeId)
                    .orElse(new User(employeeId, "Unknown", "Employee", "", "", null, null, null));

            User accountant = userDao.findById(accountantId)
                    .orElse(new User(accountantId, "Unknown", "Accountant", "", "", null, null, null));

            return new Payroll(
                    rs.getLong("ID"),
                    employee,
                    accountant,
                    rs.getFloat("HOURS_WORKED"),
                    rs.getBigDecimal("HOURLY_RATE"),
                    rs.getBigDecimal("TOTAL_AMOUNT"),
                    rs.getDate("PERIOD_START").toLocalDate(),
                    rs.getDate("PERIOD_END").toLocalDate(),
                    rs.getDate("PAYMENT_DATE").toLocalDate(),
                    rs.getBoolean("IS_PAID"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping payroll from ResultSet", e);
            throw new DatabaseMapException("Error mapping payroll");
        }
    }
}