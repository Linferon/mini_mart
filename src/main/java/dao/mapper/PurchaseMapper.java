package dao.mapper;

import exception.DatabaseMapException;
import model.Purchase;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PurchaseMapper {
    private PurchaseMapper() {
    }

    public static Purchase mapRow(ResultSet rs) {
        try {
            return new Purchase(
                    rs.getLong("ID"),
                    rs.getLong("PRODUCT_ID"),
                    rs.getInt("QUANTITY"),
                    rs.getLong("STOCK_KEEPER_ID"),
                    rs.getTimestamp("PURCHASE_DATE"),
                    rs.getBigDecimal("TOTAL_COST")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping role from ResultSet", e);
            throw new DatabaseMapException("Error mapping role");
        }
    }
}