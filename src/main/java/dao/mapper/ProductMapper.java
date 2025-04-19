package dao.mapper;

import model.Product;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMapper {
    private ProductMapper(){}

    public static Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getLong("ID"),
            rs.getString("NAME"),
            rs.getLong("CATEGORY_ID"),
            rs.getBigDecimal("BUY_PRICE"),
            rs.getBigDecimal("SELL_PRICE"),
            rs.getTimestamp("CREATED_AT"),
            rs.getTimestamp("UPDATED_AT")
        );
    }
}