package dao.impl;

import dao.Dao;
import model.ProductCategory;
import dao.mapper.ProductCategoryMapper;

import java.sql.ResultSet;
import java.util.function.Function;

import static dao.DbConstants.PRODUCT_CATEGORY_TABLE;
public class ProductCategoryDao extends Dao<ProductCategory> {
    
    @Override
    protected String getTableName() {
        return PRODUCT_CATEGORY_TABLE;
    }
    
    @Override
    protected Function<ResultSet, ProductCategory> getMapper() {
        return ProductCategoryMapper::mapRow;
    }
}