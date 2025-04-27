package dao.impl;

import dao.Dao;
import model.Income;
import dao.mapper.IncomeMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;

public class IncomeDao extends Dao<Income> {
    @Override
    protected String getTableName() {
        return INCOME_TABLE;
    }

    @Override
    protected Function<ResultSet, Income> getMapper() {
        return IncomeMapper::mapRow;
    }

    @Override
    public Optional<Income> findById(Long id) {
        String sql = "SELECT i.*, " +
                "s.NAME as SOURCE_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + INCOME_TABLE + " i " +
                "LEFT JOIN " + INCOME_SOURCE_TABLE + " s ON i.SOURCE_ID = s.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON i.ACCOUNTANT_ID = u.ID " +
                "WHERE i.ID = ?";
        return querySingle(sql, id);
    }

    @Override
    public List<Income> findAll() {
        String sql = "SELECT i.*, " +
                "s.NAME as SOURCE_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + INCOME_TABLE + " i " +
                "LEFT JOIN " + INCOME_SOURCE_TABLE + " s ON i.SOURCE_ID = s.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON i.ACCOUNTANT_ID = u.ID";
        return queryList(sql);
    }

    public List<Income> findBySource(Long sourceId) {
        String sql = "SELECT i.*, " +
                "s.NAME as SOURCE_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + INCOME_TABLE + " i " +
                "LEFT JOIN " + INCOME_SOURCE_TABLE + " s ON i.SOURCE_ID = s.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON i.ACCOUNTANT_ID = u.ID " +
                "WHERE i.SOURCE_ID = ?";
        return queryList(sql, sourceId);
    }

    public List<Income> findByAccountant(Long accountantId) {
        String sql = "SELECT i.*, " +
                "s.NAME as SOURCE_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + INCOME_TABLE + " i " +
                "LEFT JOIN " + INCOME_SOURCE_TABLE + " s ON i.SOURCE_ID = s.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON i.ACCOUNTANT_ID = u.ID " +
                "WHERE i.ACCOUNTANT_ID = ?";
        return queryList(sql, accountantId);
    }

    public List<Income> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT i.*, " +
                "s.NAME as SOURCE_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + INCOME_TABLE + " i " +
                "LEFT JOIN " + INCOME_SOURCE_TABLE + " s ON i.SOURCE_ID = s.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON i.ACCOUNTANT_ID = u.ID " +
                "WHERE i.INCOME_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Income income) {
        if (income.getId() == null) {
            String sql = "INSERT INTO " + INCOME_TABLE +
                    " (SOURCE_ID, TOTAL_AMOUNT, INCOME_DATE, ACCOUNTANT_ID) " +
                    "VALUES (?, ?, ?, ?)";
            Long id = insert(sql,
                    income.getSource().id(),
                    income.getTotalAmount(),
                    income.getIncomeDate(),
                    income.getAccountant().getId());
            if (id != null) {
                income.setId(id);
            }
            return id;
        } else {
            boolean updated = update(income);
            return updated ? income.getId() : null;
        }
    }

    public boolean update(Income income) {
        String sql = "UPDATE " + INCOME_TABLE +
                " SET SOURCE_ID = ?, TOTAL_AMOUNT = ?, " +
                "INCOME_DATE = ?, ACCOUNTANT_ID = ? WHERE ID = ?";
        return update(sql,
                income.getSource().id(),
                income.getTotalAmount(),
                income.getIncomeDate(),
                income.getAccountant().getId(),
                income.getId());
    }
}