package dao.impl;

import dao.Dao;
import model.ProductCategory;
import dao.mapper.ProductCategoryMapper;

import java.sql.ResultSet;
import java.util.List;
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
    
    public List<ProductCategory> findByName(String name) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE NAME like ?%";
        return queryList(sql, name);
    }
}