package dao.mapper;

import model.Purchase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PurchaseMapper {
    private PurchaseMapper(){}

    public static Purchase mapRow(ResultSet rs) throws SQLException {
        return new Purchase(
                rs.getLong("ID"),
                rs.getLong("PRODUCT_ID"),
                rs.getInt("QUANTITY"),
                rs.getLong("STOCK_KEEPER_ID"),
                rs.getTimestamp("PURCHASE_DATE"),
                rs.getBigDecimal("TOTAL_COST")
        );
    }
}