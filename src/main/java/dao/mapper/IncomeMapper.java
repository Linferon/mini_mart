package dao.mapper;

import exception.DatabaseMapException;
import model.Income;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IncomeMapper {
    private IncomeMapper() {
    }

    public static Income mapRow(ResultSet rs){
        try {
            return new Income(
                    rs.getLong("ID"),
                    rs.getLong("SOURCE_ID"),
                    rs.getBigDecimal("TOTAL_AMOUNT"),
                    rs.getTimestamp("INCOME_DATE"),
                    rs.getLong("ACCOUNTANT_ID")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}