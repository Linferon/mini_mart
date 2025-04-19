package dao.mapper;

import exception.DatabaseMapException;
import model.Product;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMapper {
    private ProductMapper() {
    }

    public static Product mapRow(ResultSet rs) {
        try {
            return new Product(
                    rs.getLong("ID"),
                    rs.getString("NAME"),
                    rs.getLong("CATEGORY_ID"),
                    rs.getBigDecimal("BUY_PRICE"),
                    rs.getBigDecimal("SELL_PRICE"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}