package dao.impl;

import dao.Dao;
import model.Sale;
import dao.mapper.SaleMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;

public class SaleDao extends Dao<Sale> {

    @Override
    protected String getTableName() {
        return SALE_TABLE;
    }

    @Override
    protected Function<ResultSet, Sale> getMapper() {
        return SaleMapper::mapRow;
    }

    @Override
    public Optional<Sale> findById(Long id) {
        String sql = "SELECT s.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "pc.NAME as PRODUCT_CATEGORY_NAME, " +
                "u.NAME as CASHIER_NAME, u.SURNAME as CASHIER_SURNAME " +
                "FROM " + SALE_TABLE + " s " +
                "LEFT JOIN " + PRODUCT_TABLE +" p ON s.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + PRODUCT_CATEGORY_TABLE + " pc ON p.CATEGORY_ID = pc.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON s.CASHIER_ID = u.ID " +
                "WHERE s.ID = ?";
        return querySingle(sql, id);
    }

    @Override
    public List<Sale> findAll() {
        String sql = "SELECT s.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "pc.NAME as PRODUCT_CATEGORY_NAME, " +
                "u.NAME as CASHIER_NAME, u.SURNAME as CASHIER_SURNAME " +
                "FROM " + SALE_TABLE + " s " +
                "LEFT JOIN " + PRODUCT_TABLE +" p ON s.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + PRODUCT_CATEGORY_TABLE + " pc ON p.CATEGORY_ID = pc.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON s.CASHIER_ID = u.ID";
        return queryList(sql);
    }

    public List<Sale> findByProduct(Long productId) {
        String sql = "SELECT s.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "pc.NAME as PRODUCT_CATEGORY_NAME, " +
                "u.NAME as CASHIER_NAME, u.SURNAME as CASHIER_SURNAME " +
                "FROM " + SALE_TABLE + " s " +
                "LEFT JOIN " + PRODUCT_TABLE +" p ON s.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + PRODUCT_CATEGORY_TABLE + " pc ON p.CATEGORY_ID = pc.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON s.CASHIER_ID = u.ID " +
                "WHERE s.PRODUCT_ID = ?";
        return queryList(sql, productId);
    }

    public List<Sale> findByCashier(Long cashierId) {
        String sql = "SELECT s.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "pc.NAME as PRODUCT_CATEGORY_NAME, " +
                "u.NAME as CASHIER_NAME, u.SURNAME as CASHIER_SURNAME " +
                "FROM " + SALE_TABLE + " s " +
                "LEFT JOIN " + PRODUCT_TABLE +" p ON s.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + PRODUCT_CATEGORY_TABLE + " pc ON p.CATEGORY_ID = pc.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON s.CASHIER_ID = u.ID " +
                "WHERE s.CASHIER_ID = ?";
        return queryList(sql, cashierId);
    }

    public List<Sale> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT s.*, " +
                "p.NAME as PRODUCT_NAME, " +
                "pc.NAME as PRODUCT_CATEGORY_NAME, " +
                "u.NAME as CASHIER_NAME, u.SURNAME as CASHIER_SURNAME " +
                "FROM " + SALE_TABLE + " s " +
                "LEFT JOIN " + PRODUCT_TABLE +" p ON s.PRODUCT_ID = p.ID " +
                "LEFT JOIN " + PRODUCT_CATEGORY_TABLE + " pc ON p.CATEGORY_ID = pc.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON s.CASHIER_ID = u.ID " +
                "WHERE s.SALE_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Sale sale) {
        if (sale.getId() == null) {
            String sql = "INSERT INTO " + SALE_TABLE +
                    " (PRODUCT_ID, QUANTITY, CASHIER_ID, TOTAL_AMOUNT, SALE_DATE) " +
                    "VALUES (?, ?, ?, ?, ?)";
            Long id = insert(sql,
                    sale.getProduct().getId(),
                    sale.getQuantity(),
                    sale.getCashier().getId(),
                    sale.getTotalAmount(),
                    sale.getSaleDate());
            if (id != null) {
                sale.setId(id);
            }
            return id;
        } else {
            boolean updated = update(sale);
            return updated ? sale.getId() : null;
        }
    }

    public boolean update(Sale sale) {
        String sql = "UPDATE " + SALE_TABLE +
                " SET PRODUCT_ID = ?, QUANTITY = ?, CASHIER_ID = ?, " +
                "TOTAL_AMOUNT = ?, SALE_DATE = ? WHERE ID = ?";
        return update(sql,
                sale.getProduct().getId(),
                sale.getQuantity(),
                sale.getCashier().getId(),
                sale.getTotalAmount(),
                sale.getSaleDate(),
                sale.getId());
    }
}