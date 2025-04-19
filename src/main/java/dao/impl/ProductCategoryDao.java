package dao.impl;

import dao.Dao;
import model.ProductCategory;
import dao.mapper.ProductCategoryMapper;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Function;

public class ProductCategoryDao extends Dao<ProductCategory> {
    
    @Override
    protected String getTableName() {
        return "PRODUCT_CATEGORIES";
    }
    
    @Override
    protected Function<ResultSet, ProductCategory> getMapper() {
        return ProductCategoryMapper::mapRow;
    }
    
    public Optional<ProductCategory> findByName(String name) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE NAME = ?";
        return querySingle(sql, name);
    }
    
    public Long save(ProductCategory category) {
        if (category.getId() == null) {
            String sql = "INSERT INTO " + getTableName() + " (NAME) VALUES (?)";
            Long id = insert(sql, category.getName());
            if (id != null) {
                category.setId(id);
            }
            return id;
        } else {
            boolean updated = update(category);
            return updated ? category.getId() : null;
        }
    }
    
    public boolean update(ProductCategory category) {
        String sql = "UPDATE " + getTableName() + " SET NAME = ? WHERE ID = ?";
        return update(sql, category.getName(), category.getId());
    }
}