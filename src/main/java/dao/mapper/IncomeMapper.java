package dao.mapper;

import model.Income;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IncomeMapper {
    private IncomeMapper(){}
    
    public static Income mapRow(ResultSet rs) throws SQLException {
        return new Income(
            rs.getLong("ID"),
            rs.getLong("SOURCE_ID"),
            rs.getBigDecimal("TOTAL_AMOUNT"),
            rs.getTimestamp("INCOME_DATE"),
            rs.getLong("ACCOUNTANT_ID")
        );
    }
}