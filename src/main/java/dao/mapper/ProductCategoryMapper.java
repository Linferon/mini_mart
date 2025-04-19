package dao.mapper;

import model.ProductCategory;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductCategoryMapper {
    private ProductCategoryMapper(){}

    public static ProductCategory mapRow(ResultSet rs) throws SQLException {
        return new ProductCategory(
            rs.getLong("ID"),
            rs.getString("NAME")
        );
    }
}