package dao.mapper;

import model.Payroll;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PayrollMapper {
    private PayrollMapper(){}
    
    public static Payroll mapRow(ResultSet rs) throws SQLException {
        return new Payroll(
            rs.getLong("ID"),
            rs.getLong("EMPLOYEE_ID"),
            rs.getLong("ACCOUNTANT_ID"),
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
    }
}