package dao.mapper;

import model.Expense;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseMapper {
    private ExpenseMapper(){}
    
    public static Expense mapRow(ResultSet rs) throws SQLException {
        return new Expense(
            rs.getLong("ID"),
            rs.getLong("CATEGORY_ID"),
            rs.getBigDecimal("TOTAL_AMOUNT"),
            rs.getTimestamp("EXPENSE_DATE"),
            rs.getLong("ACCOUNTANT_ID")
        );
    }
}