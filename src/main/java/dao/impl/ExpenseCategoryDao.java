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
}