package dao.impl;

import dao.Dao;
import model.Product;
import dao.mapper.ProductMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

public class ProductDao extends Dao<Product> {
    
    @Override
    protected String getTableName() {
        return "PRODUCTS";
    }
    
    @Override
    protected Function<ResultSet, Product> getMapper() {
        return ProductMapper::mapRow;
    }
    
    public List<Product> findByCategory(Long categoryId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE CATEGORY_ID = ?";
        return queryList(sql, categoryId);
    }
    
    public List<Product> findByName(String name) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE NAME LIKE ?";
        return queryList(sql, "%" + name + "%");
    }
    
    public Long save(Product product) {
        if (product.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            String sql = "INSERT INTO " + getTableName() + 
                         " (NAME, CATEGORY_ID, BUY_PRICE, SELL_PRICE, CREATED_AT, UPDATED_AT) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            Long id = insert(sql, 
                         product.getName(),
                         product.getCategoryId(),
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
        
        String sql = "UPDATE " + getTableName() + 
                     " SET NAME = ?, CATEGORY_ID = ?, BUY_PRICE = ?, " +
                     "SELL_PRICE = ?, UPDATED_AT = ? WHERE ID = ?";
        return update(sql,
                 product.getName(),
                 product.getCategoryId(),
                 product.getBuyPrice(),
                 product.getSellPrice(),
                 now,
                 product.getId());
    }
    
    public List<Product> findAllWithCategoryDetails() {
        String sql = "SELECT p.*, c.NAME as CATEGORY_NAME " +
                     "FROM " + getTableName() + " p " +
                     "JOIN PRODUCT_CATEGORIES c ON p.CATEGORY_ID = c.ID";
        return queryList(sql);
    }
}