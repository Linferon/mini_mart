package dao.mapper;

import dao.impl.ProductDao;
import dao.impl.UserDao;
import exception.DatabaseMapException;
import model.Product;
import model.Purchase;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PurchaseMapper {
    private static final ProductDao productDao = new ProductDao();
    private static final UserDao userDao = new UserDao();

    private PurchaseMapper() {
    }

    public static Purchase mapRow(ResultSet rs) {
        try {
            Long productId = rs.getLong("PRODUCT_ID");
            Long stockKeeperId = rs.getLong("STOCK_KEEPER_ID");

            Product product = productDao.findById(productId)
                    .orElse(new Product(productId, "Unknown Product", null, null, null, null, null));

            User stockKeeper = userDao.findById(stockKeeperId)
                    .orElse(new User(stockKeeperId, "Unknown", "StockKeeper", "", "", null, null, null));

            return new Purchase(
                    rs.getLong("ID"),
                    product,
                    rs.getInt("QUANTITY"),
                    stockKeeper,
                    rs.getTimestamp("PURCHASE_DATE"),
                    rs.getBigDecimal("TOTAL_COST")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping purchase from ResultSet", e);
            throw new DatabaseMapException("Error mapping purchase");
        }
    }
}