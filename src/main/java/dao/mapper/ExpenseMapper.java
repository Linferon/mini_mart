package dao.mapper;

import dao.impl.ExpenseCategoryDao;
import dao.impl.UserDao;
import exception.DatabaseMapException;
import model.Expense;
import model.ExpenseCategory;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseMapper {
    private static final ExpenseCategoryDao categoryDao = new ExpenseCategoryDao();
    private static final UserDao userDao = new UserDao();

    private ExpenseMapper() {}

    public static Expense mapRow(ResultSet rs) {
        try {
            Long categoryId = rs.getLong("CATEGORY_ID");
            Long accountantId = rs.getLong("ACCOUNTANT_ID");

            ExpenseCategory category = categoryDao.findById(categoryId)
                    .orElse(new ExpenseCategory(categoryId, "Unknown Category"));

            User accountant = userDao.findById(accountantId)
                    .orElse(new User(accountantId, "Unknown", "User", "", "", null,  null, null));

            return new Expense(
                    rs.getLong("ID"),
                    category,
                    rs.getBigDecimal("TOTAL_AMOUNT"),
                    rs.getTimestamp("EXPENSE_DATE"),
                    accountant
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping expense from ResultSet", e);
            throw new DatabaseMapException("Error mapping expense");
        }
    }
}