package dao.mapper;

import exception.DatabaseMapException;
import model.Product;
import model.Stock;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class StockMapper {
    private StockMapper() {}

    public static Stock mapRow(ResultSet rs) {
        try {
            Long productId = rs.getLong("PRODUCT_ID");
            Integer quantity = rs.getInt("QUANTITY");
            Timestamp createdAt = rs.getTimestamp("CREATED_AT");
            Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");

            String productName  = rs.getString("PRODUCT_NAME");


            Product product = new Product(productId, productName);

            return new Stock(
                    product,
                    quantity,
                    createdAt,
                    updatedAt
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping stock from ResultSet", e);
            throw new DatabaseMapException("Error mapping stock");
        }
    }
}