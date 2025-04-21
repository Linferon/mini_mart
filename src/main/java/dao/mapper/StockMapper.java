package dao.mapper;

import dao.impl.ProductDao;
import exception.DatabaseMapException;
import model.Product;
import model.Stock;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StockMapper {
    private static final ProductDao productDao = new ProductDao();

    private StockMapper() {
    }

    public static Stock mapRow(ResultSet rs) {
        try {
            Long productId = rs.getLong("PRODUCT_ID");

            Product product = productDao.findById(productId)
                    .orElse(new Product(productId, "Unknown Product", null, null, null, null, null));

            return new Stock(
                    product,
                    rs.getInt("QUANTITY"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping stock from ResultSet", e);
            throw new DatabaseMapException("Error mapping stock");
        }
    }
}