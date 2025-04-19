package dao.impl;

import dao.Dao;
import model.ExpenseCategory;
import dao.mapper.ExpenseCategoryMapper;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Function;

public class ExpenseCategoryDao extends Dao<ExpenseCategory> {
    
    @Override
    protected String getTableName() {
        return "EXPENSE_CATEGORIES";
    }
    
    @Override
    protected Function<ResultSet, ExpenseCategory> getMapper() {
        return ExpenseCategoryMapper::mapRow;
    }
    
    public Optional<ExpenseCategory> findByName(String name) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE NAME = ?";
        return querySingle(sql, name);
    }
    
    public Long save(ExpenseCategory category) {
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
    
    public boolean update(ExpenseCategory category) {
        String sql = "UPDATE " + getTableName() + " SET NAME = ? WHERE ID = ?";
        return update(sql, category.getName(), category.getId());
    }
}