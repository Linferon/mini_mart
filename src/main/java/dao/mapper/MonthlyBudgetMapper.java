package dao.mapper;

import dao.impl.UserDao;
import exception.DatabaseMapException;
import model.MonthlyBudget;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MonthlyBudgetMapper {
    private static final UserDao userDao = new UserDao();

    private MonthlyBudgetMapper() {
    }

    public static MonthlyBudget mapRow(ResultSet rs) {
        try {
            Long directorId = rs.getLong("DIRECTOR_ID");

            User director = userDao.findById(directorId)
                    .orElse(new User(directorId, "Unknown", "Director", "", "", null, null, null));

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
                    director
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping monthly budget from ResultSet", e);
            throw new DatabaseMapException("Error mapping monthly budget");
        }
    }
}