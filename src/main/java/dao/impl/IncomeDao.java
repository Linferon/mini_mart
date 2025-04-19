package dao.impl;

import dao.Dao;
import model.Income;
import dao.mapper.IncomeMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

public class IncomeDao extends Dao<Income> {

    @Override
    protected String getTableName() {
        return "INCOMES";
    }

    @Override
    protected Function<ResultSet, Income> getMapper() {
        return IncomeMapper::mapRow;
    }

    public List<Income> findBySource(Long sourceId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE SOURCE_ID = ?";
        return queryList(sql, sourceId);
    }

    public List<Income> findByAccountant(Long accountantId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ACCOUNTANT_ID = ?";
        return queryList(sql, accountantId);
    }

    public List<Income> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE INCOME_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Income income) {
        if (income.getId() == null) {
            String sql = "INSERT INTO " + getTableName() +
                    " (SOURCE_ID, TOTAL_AMOUNT, INCOME_DATE, ACCOUNTANT_ID) " +
                    "VALUES (?, ?, ?, ?)";
            Long id = insert(sql,
                    income.getSourceId(),
                    income.getTotalAmount(),
                    income.getIncomeDate(),
                    income.getAccountantId());
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
        String sql = "UPDATE " + getTableName() +
                " SET SOURCE_ID = ?, TOTAL_AMOUNT = ?, " +
                "INCOME_DATE = ?, ACCOUNTANT_ID = ? WHERE ID = ?";
        return update(sql,
                income.getSourceId(),
                income.getTotalAmount(),
                income.getIncomeDate(),
                income.getAccountantId(),
                income.getId());
    }
}