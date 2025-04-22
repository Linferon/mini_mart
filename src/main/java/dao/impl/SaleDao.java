package dao.impl;

import dao.Dao;
import model.Sale;
import dao.mapper.SaleMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

public class SaleDao extends Dao<Sale> {

    @Override
    protected String getTableName() {
        return "SALES";
    }

    @Override
    protected Function<ResultSet, Sale> getMapper() {
        return SaleMapper::mapRow;
    }

    public List<Sale> findByProduct(Long productId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE PRODUCT_ID = ?";
        return queryList(sql, productId);
    }

    public List<Sale> findByCashier(Long cashierId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE CASHIER_ID = ?";
        return queryList(sql, cashierId);
    }

    public List<Sale> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE SALE_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Sale sale) {
        if (sale.getId() == null) {
            String sql = "INSERT INTO " + getTableName() +
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
        String sql = "UPDATE " + getTableName() +
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