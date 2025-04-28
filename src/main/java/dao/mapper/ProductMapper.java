package dao.mapper;

import exception.DatabaseMapException;
import model.Product;
import model.ProductCategory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static util.LoggerUtil.error;

public class ProductMapper {
    private ProductMapper() {}

    public static Product mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            String name = rs.getString("NAME");
            Long categoryId = rs.getLong("CATEGORY_ID");
            BigDecimal buyPrice = rs.getBigDecimal("BUY_PRICE");
            BigDecimal sellPrice = rs.getBigDecimal("SELL_PRICE");
            Timestamp createdAt = rs.getTimestamp("CREATED_AT");
            Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");

            String categoryName = rs.getString("CATEGORY_NAME");

            ProductCategory category = new ProductCategory(categoryId, categoryName);

            return new Product(id, name, category, buyPrice, sellPrice, createdAt, updatedAt);
        } catch (SQLException e) {
            error("Error mapping product from ResultSet", e);
            throw new DatabaseMapException("Error mapping product");
        }
    }
}