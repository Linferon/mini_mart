package dao.mapper;

import model.Sale;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleMapper {
    private SaleMapper(){}

    public static Sale mapRow(ResultSet rs) throws SQLException {
        return new Sale(
            rs.getLong("ID"),
            rs.getLong("PRODUCT_ID"),
            rs.getInt("QUANTITY"),
            rs.getLong("CASHIER_ID"),
            rs.getBigDecimal("TOTAL_AMOUNT"),
            rs.getTimestamp("SALE_DATE")
        );
    }
}