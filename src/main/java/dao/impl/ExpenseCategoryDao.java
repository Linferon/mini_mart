package dao.impl;

import dao.Dao;
import model.ExpenseCategory;
import dao.mapper.ExpenseCategoryMapper;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.EXPENSE_CATEGORY_TABLE;

public class ExpenseCategoryDao extends Dao<ExpenseCategory> {

    @Override
    protected String getTableName() {
        return EXPENSE_CATEGORY_TABLE;
    }

    @Override
    protected Function<ResultSet, ExpenseCategory> getMapper() {
        return ExpenseCategoryMapper::mapRow;
    }

    public Optional<ExpenseCategory> findByName(String name) {
        String sql = "SELECT * FROM " + EXPENSE_CATEGORY_TABLE + " WHERE NAME = ?";
        return querySingle(sql, name);
    }
}