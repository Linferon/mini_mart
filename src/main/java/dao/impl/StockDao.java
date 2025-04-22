package dao.impl;

import dao.Dao;
import model.Stock;
import dao.mapper.StockMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.function.Function;

public class StockDao extends Dao<Stock> {

    @Override
    protected String getTableName() {
        return "STOCK";
    }

    @Override
    protected Function<ResultSet, Stock> getMapper() {
        return StockMapper::mapRow;
    }

    @Override
    public Optional<Stock> findById(Long id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE PRODUCT_ID = ?";
        return querySingle(sql, id);
    }

    public Optional<Stock> findByProductId(Long productId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE PRODUCT_ID = ?";
        return querySingle(sql, productId);
    }

    public Long save(Stock stock) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Optional<Stock> existingStock = findByProductId(stock.getProduct().getId());

        if (existingStock.isEmpty()) {
            String sql = "INSERT INTO " + getTableName() +
                    " (PRODUCT_ID, QUANTITY, CREATED_AT, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?)";
            insert(sql,
                    stock.getProduct().getId(),
                    stock.getQuantity(),
                    stock.getCreatedAt() != null ? stock.getCreatedAt() : now,
                    stock.getUpdatedAt() != null ? stock.getUpdatedAt() : now);

            return stock.getProduct().getId();
        } else {
            boolean updated = update(stock);
            return updated ? stock.getProduct().getId() : null;
        }
    }

    public boolean update(Stock stock) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE " + getTableName() +
                " SET QUANTITY = ?, UPDATED_AT = ? WHERE PRODUCT_ID = ?";
        return update(sql,
                stock.getQuantity(),
                now,
                stock.getProduct().getId());
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE PRODUCT_ID = ?";
        return delete(sql, id);
    }

    public boolean deleteByProductId(Long productId) {
        String sql = "DELETE FROM " + getTableName() + " WHERE PRODUCT_ID = ?";
        return delete(sql, productId);
    }
}