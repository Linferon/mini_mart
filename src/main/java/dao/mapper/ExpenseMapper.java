package dao.mapper;

import exception.DatabaseMapException;
import model.Expense;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseMapper {
    private ExpenseMapper() {
    }

    public static Expense mapRow(ResultSet rs){
        try {
            return new Expense(
                    rs.getLong("ID"),
                    rs.getLong("CATEGORY_ID"),
                    rs.getBigDecimal("TOTAL_AMOUNT"),
                    rs.getTimestamp("EXPENSE_DATE"),
                    rs.getLong("ACCOUNTANT_ID")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}