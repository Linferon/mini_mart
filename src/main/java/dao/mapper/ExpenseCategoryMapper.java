package dao.mapper;

import model.ExpenseCategory;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseCategoryMapper {
    private ExpenseCategoryMapper(){}
    
    public static ExpenseCategory mapRow(ResultSet rs) throws SQLException {
        return new ExpenseCategory(
            rs.getLong("ID"),
            rs.getString("NAME")
        );
    }
}