package dao.mapper;

import exception.DatabaseMapException;
import model.Stock;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StockMapper {
    private StockMapper() {
    }

    public static Stock mapRow(ResultSet rs) {
        try {
            return new Stock(
                    rs.getLong("PRODUCT_ID"),
                    rs.getInt("QUANTITY"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}