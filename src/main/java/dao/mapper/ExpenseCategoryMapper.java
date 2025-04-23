package dao.mapper;

import exception.DatabaseMapException;
import model.ExpenseCategory;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseCategoryMapper {
    private ExpenseCategoryMapper() {}

    public static ExpenseCategory mapRow(ResultSet rs) {
        try {
            return new ExpenseCategory(
                    rs.getLong("ID"),
                    rs.getString("NAME")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}