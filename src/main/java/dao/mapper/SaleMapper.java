package dao.mapper;

import exception.DatabaseMapException;
import model.Sale;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleMapper {
    private SaleMapper() {
    }

    public static Sale mapRow(ResultSet rs) {
        try {
            return new Sale(
                    rs.getLong("ID"),
                    rs.getLong("PRODUCT_ID"),
                    rs.getInt("QUANTITY"),
                    rs.getLong("CASHIER_ID"),
                    rs.getBigDecimal("TOTAL_AMOUNT"),
                    rs.getTimestamp("SALE_DATE")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}