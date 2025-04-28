package dao.mapper;

import exception.DatabaseMapException;
import model.ProductCategory;

import java.sql.ResultSet;
import java.sql.SQLException;

import static util.LoggerUtil.error;

public class ProductCategoryMapper {
    private ProductCategoryMapper() {}

    public static ProductCategory mapRow(ResultSet rs) {
        try {
            return new ProductCategory(
                    rs.getLong("ID"),
                    rs.getString("NAME")
            );
        } catch (SQLException e) {
            error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}