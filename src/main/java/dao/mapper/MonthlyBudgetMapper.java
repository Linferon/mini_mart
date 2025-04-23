package dao.mapper;

import exception.DatabaseMapException;
import model.MonthlyBudget;
import model.User;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class MonthlyBudgetMapper {
    private MonthlyBudgetMapper() {
    }

    public static MonthlyBudget mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            LocalDate budgetDate = rs.getDate("BUDGET_DATE") != null ? rs.getDate("BUDGET_DATE").toLocalDate() : null;
            BigDecimal plannedIncome = rs.getBigDecimal("PLANNED_INCOME");
            BigDecimal plannedExpenses = rs.getBigDecimal("PLANNED_EXPENSES");
            BigDecimal actualIncome = rs.getBigDecimal("ACTUAL_INCOME");
            BigDecimal actualExpenses = rs.getBigDecimal("ACTUAL_EXPENSES");
            BigDecimal netResult = rs.getBigDecimal("NET_RESULT");
            Timestamp createdAt = rs.getTimestamp("CREATED_AT");
            Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");
            Long directorId = rs.getLong("DIRECTOR_ID");

            String directorName = rs.getString("DIRECTOR_NAME");
            String directorSurname = null;

            try {
                directorSurname = rs.getString("DIRECTOR_SURNAME");
            } catch (SQLException ignored) {
            }

            User director = new User(
                    directorId,
                    directorName,
                    directorSurname);

            return new MonthlyBudget(
                    id,
                    budgetDate,
                    plannedIncome,
                    plannedExpenses,
                    actualIncome,
                    actualExpenses,
                    netResult,
                    createdAt,
                    updatedAt,
                    director
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping monthly budget from ResultSet", e);
            throw new DatabaseMapException("Error mapping monthly budget");
        }
    }
}