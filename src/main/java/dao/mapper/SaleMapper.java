package dao.mapper;

import exception.DatabaseMapException;
import model.*;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static util.LoggerUtil.error;

public class SaleMapper {
    private SaleMapper() {
    }

    public static Sale mapRow(ResultSet rs) {
        try {
            Long id = rs.getLong("ID");
            Long productId = rs.getLong("PRODUCT_ID");
            Integer quantity = rs.getInt("QUANTITY");
            Long cashierId = rs.getLong("CASHIER_ID");
            BigDecimal totalAmount = rs.getBigDecimal("TOTAL_AMOUNT");
            Timestamp saleDate = rs.getTimestamp("SALE_DATE");

            String productName =  rs.getString("PRODUCT_NAME");
            String productCategoryName = rs.getString("PRODUCT_CATEGORY_NAME");
            String cashierName = rs.getString("CASHIER_NAME");
            String cashierSurname = null;

            try {
                cashierSurname = rs.getString("CASHIER_SURNAME");
            } catch (SQLException ignored) {}

            ProductCategory productCategory = new ProductCategory(null,productCategoryName);
            Product product = new Product(productId, productName, productCategory);

            User cashier = new User(cashierId, cashierName, cashierSurname);

            return new Sale(id, product, quantity, cashier, totalAmount, saleDate);
        } catch (SQLException e) {
            error("Error mapping expense from ResultSet", e);
            throw new DatabaseMapException("Error mapping expense");
        }
    }
}