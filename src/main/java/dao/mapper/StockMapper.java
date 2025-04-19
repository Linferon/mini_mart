package dao.mapper;

import model.Stock;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StockMapper {
    private StockMapper(){}

    public static Stock mapRow(ResultSet rs) throws SQLException {
        return new Stock(
            rs.getLong("PRODUCT_ID"),
            rs.getInt("QUANTITY"),
            rs.getTimestamp("CREATED_AT"),
            rs.getTimestamp("UPDATED_AT")
        );
    }
}