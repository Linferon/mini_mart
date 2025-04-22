package dao.impl;

import dao.Dao;
import model.IncomeSource;
import dao.mapper.IncomeSourceMapper;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Function;

public class IncomeSourceDao extends Dao<IncomeSource> {
    
    @Override
    protected String getTableName() {
        return "INCOME_SOURCES";
    }
    
    @Override
    protected Function<ResultSet, IncomeSource> getMapper() {
        return IncomeSourceMapper::mapRow;
    }
    
    public Optional<IncomeSource> findByName(String name) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE NAME = ?";
        return querySingle(sql, name);
    }
}