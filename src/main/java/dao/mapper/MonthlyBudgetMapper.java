package dao.mapper;

import exception.DatabaseMapException;
import model.MonthlyBudget;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MonthlyBudgetMapper {
    private MonthlyBudgetMapper() {
    }

    public static MonthlyBudget mapRow(ResultSet rs){
        try {
            return new MonthlyBudget(
                    rs.getLong("ID"),
                    rs.getDate("BUDGET_DATE").toLocalDate(),
                    rs.getBigDecimal("PLANNED_INCOME"),
                    rs.getBigDecimal("PLANNED_EXPENSES"),
                    rs.getBigDecimal("ACTUAL_INCOME"),
                    rs.getBigDecimal("ACTUAL_EXPENSES"),
                    rs.getBigDecimal("NET_RESULT"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT"),
                    rs.getLong("DIRECTOR_ID")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}