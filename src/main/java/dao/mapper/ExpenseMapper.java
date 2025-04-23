package dao.mapper;

import exception.DatabaseMapException;
import model.Expense;
import model.ExpenseCategory;
import model.User;
import util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ExpenseMapper {
    private ExpenseMapper() {}

    public static Expense mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            Long categoryId = rs.getLong("CATEGORY_ID");
            BigDecimal totalAmount = rs.getBigDecimal("TOTAL_AMOUNT");
            Timestamp expenseDate = rs.getTimestamp("EXPENSE_DATE");
            Long accountantId = rs.getLong("ACCOUNTANT_ID");

            String  categoryName = rs.getString("CATEGORY_NAME");
            String accountantName = rs.getString("ACCOUNTANT_NAME");
            String accountantSurname = null;

            try {
                accountantSurname = rs.getString("ACCOUNTANT_SURNAME");
            } catch (SQLException ignored) {}

            ExpenseCategory category = new ExpenseCategory(categoryId, categoryName);

            User accountant = new User(
                    accountantId,
                    accountantName,
                    accountantSurname
            );

            return new Expense(id, category, totalAmount, expenseDate, accountant);
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping expense from ResultSet", e);
            throw new DatabaseMapException("Error mapping expense");
        }
    }
}