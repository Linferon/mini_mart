package dao.impl;

import dao.Dao;
import model.Purchase;
import dao.mapper.PurchaseMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;

public class PurchaseDao extends Dao<Purchase> {

    @Override
    protected String getTableName() {
        return PURCHASE_TABLE;
    }

    @Override
    protected Function<ResultSet, Purchase> getMapper() {
        return PurchaseMapper::mapRow;
    }

    @Override
    public Optional<Purchase> findById(Long id) {
        String sql = "SELECT pur.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "u.NAME as STOCK_KEEPER_NAME, u.SURNAME as STOCK_KEEPER_SURNAME " +
                "FROM " + PURCHASE_TABLE + " pur " +
                "LEFT JOIN " + PRODUCT_TABLE + " p ON pur.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON pur.STOCK_KEEPER_ID = u.ID " +
                "WHERE pur.ID = ?";
        return querySingle(sql, id);
    }

    @Override
    public List<Purchase> findAll() {
        String sql = "SELECT pur.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "u.NAME as STOCK_KEEPER_NAME, u.SURNAME as STOCK_KEEPER_SURNAME " +
                "FROM " + PURCHASE_TABLE + " pur " +
                "LEFT JOIN " + PRODUCT_TABLE + " p ON pur.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON pur.STOCK_KEEPER_ID = u.ID";
        return queryList(sql);
    }

    public List<Purchase> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT pur.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "u.NAME as STOCK_KEEPER_NAME, u.SURNAME as STOCK_KEEPER_SURNAME " +
                "FROM " + PURCHASE_TABLE + " pur " +
                "LEFT JOIN " + PRODUCT_TABLE + " p ON pur.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON pur.STOCK_KEEPER_ID = u.ID " +
                "WHERE pur.PURCHASE_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Purchase purchase) {
        if (purchase.getId() == null) {
            String sql = "INSERT INTO " + PURCHASE_TABLE +
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
        String sql = "UPDATE " + PURCHASE_TABLE +
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