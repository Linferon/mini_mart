package dao.impl;

import dao.Dao;
import model.Purchase;
import dao.mapper.PurchaseMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

public class PurchaseDao extends Dao<Purchase> {

    @Override
    protected String getTableName() {
        return "PURCHASES";
    }

    @Override
    protected Function<ResultSet, Purchase> getMapper() {
        return PurchaseMapper::mapRow;
    }

    public List<Purchase> findByProduct(Long productId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE PRODUCT_ID = ?";
        return queryList(sql, productId);
    }

    public List<Purchase> findByStockKeeper(Long stockKeeperId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE STOCK_KEEPER_ID = ?";
        return queryList(sql, stockKeeperId);
    }

    public List<Purchase> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE PURCHASE_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Purchase purchase) {
        if (purchase.getId() == null) {
            String sql = "INSERT INTO " + getTableName() +
                    " (PRODUCT_ID, QUANTITY, STOCK_KEEPER_ID, PURCHASE_DATE, TOTAL_COST) " +
                    "VALUES (?, ?, ?, ?, ?)";
            Long id = insert(sql,
                    purchase.getProduct().getId(),
                    purchase.getQuantity(),
                    purchase.getStockKeeper().getId(),
                    purchase.getPurchaseDate(),
                    purchase.getTotalCost());
            if (id != null) {
                purchase.setId(id);
            }
            return id;
        } else {
            boolean updated = update(purchase);
            return updated ? purchase.getId() : null;
        }
    }

    public boolean update(Purchase purchase) {
        String sql = "UPDATE " + getTableName() +
                " SET PRODUCT_ID = ?, QUANTITY = ?, STOCK_KEEPER_ID = ?, " +
                "PURCHASE_DATE = ?, TOTAL_COST = ? WHERE ID = ?";
        return update(sql,
                purchase.getProduct().getId(),
                purchase.getQuantity(),
                purchase.getStockKeeper().getId(),
                purchase.getPurchaseDate(),
                purchase.getTotalCost(),
                purchase.getId());
    }
}