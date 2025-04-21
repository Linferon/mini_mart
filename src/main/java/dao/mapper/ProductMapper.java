package dao.mapper;

import dao.impl.ProductCategoryDao;
import exception.DatabaseMapException;
import model.Product;
import model.ProductCategory;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMapper {
    private static final ProductCategoryDao categoryDao = new ProductCategoryDao();

    private ProductMapper() {
    }

    public static Product mapRow(ResultSet rs) {
        try {
            Long categoryId = rs.getLong("CATEGORY_ID");

            ProductCategory category = categoryDao.findById(categoryId)
                    .orElse(new ProductCategory(categoryId, "Unknown Category"));

            return new Product(
                    rs.getLong("ID"),
                    rs.getString("NAME"),
                    category,
                    rs.getBigDecimal("BUY_PRICE"),
                    rs.getBigDecimal("SELL_PRICE"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping product from ResultSet", e);
            throw new DatabaseMapException("Error mapping product");
        }
    }
}