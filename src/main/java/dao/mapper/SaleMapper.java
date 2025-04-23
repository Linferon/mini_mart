package dao.mapper;

import dao.impl.ProductDao;
import dao.impl.UserDao;
import exception.DatabaseMapException;
import model.Product;
import model.Sale;
import model.User;
import util.LoggerUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleMapper {
    private static final ProductDao productDao = new ProductDao();
    private static final UserDao userDao = new UserDao();

    private SaleMapper() {}

    public static Sale mapRow(ResultSet rs) {
        try {
            Long productId = rs.getLong("PRODUCT_ID");
            Long cashierId = rs.getLong("CASHIER_ID");

            Product product = productDao.findById(productId)
                    .orElse(new Product(productId, "Unknown Product", null, null, null, null, null));

            User cashier = userDao.findById(cashierId)
                    .orElse(new User(cashierId, "Unknown", "Cashier", "", "", null, null, null));

            return new Sale(
                    rs.getLong("ID"),
                    product,
                    rs.getInt("QUANTITY"),
                    cashier,
                    rs.getBigDecimal("TOTAL_AMOUNT"),
                    rs.getTimestamp("SALE_DATE")
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error mapping sale from ResultSet", e);
            throw new DatabaseMapException("Error mapping sale");
        }
    }
}