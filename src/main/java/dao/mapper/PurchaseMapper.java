package dao.mapper;

import exception.DatabaseMapException;
import model.Product;
import model.Purchase;
import model.User;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static util.LoggerUtil.error;

public class PurchaseMapper {
    private PurchaseMapper() {
    }

    public static Purchase mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            Long productId = rs.getLong("PRODUCT_ID");
            Integer quantity = rs.getInt("QUANTITY");
            Long stockKeeperId = rs.getLong("STOCK_KEEPER_ID");
            Timestamp purchaseDate = rs.getTimestamp("PURCHASE_DATE");
            BigDecimal totalCost = rs.getBigDecimal("TOTAL_COST");

            String productName  = rs.getString("PRODUCT_NAME");
            String stockKeeperName = rs.getString("STOCK_KEEPER_NAME");
            String stockKeeperSurname = null;

            try {
                stockKeeperSurname = rs.getString("STOCK_KEEPER_SURNAME");
            } catch (SQLException ignored) {}

            Product product = new Product(productId, productName);

            User stockKeeper = new User(
                    stockKeeperId,
                    stockKeeperName,
                    stockKeeperSurname
            );

            return new Purchase(id, product, quantity, stockKeeper, purchaseDate, totalCost);
        } catch (SQLException e) {
            error("Error mapping purchase from ResultSet", e);
            throw new DatabaseMapException("Error mapping purchase");
        }
    }
}