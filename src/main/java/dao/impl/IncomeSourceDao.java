package dao.impl;

import dao.Dao;
import model.IncomeSource;
import dao.mapper.IncomeSourceMapper;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Function;
import static dao.DbConstants.INCOME_SOURCE_TABLE;

public class IncomeSourceDao extends Dao<IncomeSource> {
    
    @Override
    protected String getTableName() {
        return INCOME_SOURCE_TABLE;
    }
    
    @Override
    protected Function<ResultSet, IncomeSource> getMapper() {
        return IncomeSourceMapper::mapRow;
    }
    
    public Optional<IncomeSource> findByName(String name) {
        String sql = "SELECT * FROM " + INCOME_SOURCE_TABLE + " WHERE NAME = ?";
        return querySingle(sql, name);
    }
}