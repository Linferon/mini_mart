package dao.impl;

import dao.Dao;
import model.Product;
import dao.mapper.ProductMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;

public class ProductDao extends Dao<Product> {

    @Override
    protected String getTableName() {
        return PRODUCT_TABLE;
    }

    @Override
    protected Function<ResultSet, Product> getMapper() {
        return ProductMapper::mapRow;
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.*, c.NAME as CATEGORY_NAME " +
                "FROM " + PRODUCT_TABLE + " p " +
                "LEFT JOIN " + PRODUCT_CATEGORY_TABLE +" c ON p.CATEGORY_ID = c.ID " +
                "WHERE p.ID = ?";
        return querySingle(sql, id);
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT p.*, c.NAME as CATEGORY_NAME " +
                "FROM " + PRODUCT_TABLE + " p " +
                "LEFT JOIN " + PRODUCT_CATEGORY_TABLE + " c ON p.CATEGORY_ID = c.ID";
        return queryList(sql);
    }

    public Long save(Product product) {
        if (product.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO " + PRODUCT_TABLE +
                    " (NAME, CATEGORY_ID, BUY_PRICE, SELL_PRICE, CREATED_AT, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            Long id = insert(sql,
                    product.getName(),
                    product.getCategory().id(),
                    product.getBuyPrice(),
                    product.getSellPrice(),
                    product.getCreatedAt() != null ? product.getCreatedAt() : now,
                    product.getUpdatedAt() != null ? product.getUpdatedAt() : now);
            if (id != null) {
                product.setId(id);
            }
            return id;
        } else {
            boolean updated = update(product);
            return updated ? product.getId() : null;
        }
    }

    public boolean update(Product product) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE " + PRODUCT_TABLE +
                " SET NAME = ?, CATEGORY_ID = ?, BUY_PRICE = ?, " +
                "SELL_PRICE = ?, UPDATED_AT = ? WHERE ID = ?";
        return update(sql,
                product.getName(),
                product.getCategory().id(),
                product.getBuyPrice(),
                product.getSellPrice(),
                now,
                product.getId());
    }
}