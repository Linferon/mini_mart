package dao.impl;

import dao.Dao;
import model.MonthlyBudget;
import dao.mapper.MonthlyBudgetMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;

public class MonthlyBudgetDao extends Dao<MonthlyBudget> {
    @Override
    protected String getTableName() {
        return MONTHLY_BUDGET_TABLE;
    }

    @Override
    protected Function<ResultSet, MonthlyBudget> getMapper() {
        return MonthlyBudgetMapper::mapRow;
    }

    @Override
    public Optional<MonthlyBudget> findById(Long id) {
        String sql = "SELECT mb.*, u.NAME as DIRECTOR_NAME, u.SURNAME as DIRECTOR_SURNAME " +
                "FROM " + MONTHLY_BUDGET_TABLE + " mb " +
                "LEFT JOIN " + USER_TABLE + " u ON mb.DIRECTOR_ID = u.ID " +
                "WHERE mb.ID = ?";
        return querySingle(sql, id);
    }

    @Override
    public List<MonthlyBudget> findAll() {
        String sql = "SELECT mb.*, u.NAME as DIRECTOR_NAME, u.SURNAME as DIRECTOR_SURNAME " +
                "FROM " + MONTHLY_BUDGET_TABLE + " mb " +
                "LEFT JOIN " + USER_TABLE + " u ON mb.DIRECTOR_ID = u.ID";
        return queryList(sql);
    }

    public List<MonthlyBudget> findByDateRange(Date startDate, Date endDate) {
        String sql = "SELECT mb.*, u.NAME as DIRECTOR_NAME, u.SURNAME as DIRECTOR_SURNAME " +
                "FROM " + MONTHLY_BUDGET_TABLE + " mb " +
                "LEFT JOIN " + USER_TABLE + " u ON mb.DIRECTOR_ID = u.ID " +
                "WHERE mb.BUDGET_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Optional<MonthlyBudget> findByDate(Date date) {
        String sql = "SELECT mb.*, u.NAME as DIRECTOR_NAME, u.SURNAME as DIRECTOR_SURNAME " +
                "FROM " + MONTHLY_BUDGET_TABLE + " mb " +
                "LEFT JOIN " + USER_TABLE + " u ON mb.DIRECTOR_ID = u.ID " +
                "WHERE mb.BUDGET_DATE = ?";
        return querySingle(sql, date);
    }

    public Long save(MonthlyBudget budget) {
        if (budget.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO " + MONTHLY_BUDGET_TABLE +
                    " (BUDGET_DATE, PLANNED_INCOME, PLANNED_EXPENSES, " +
                    "ACTUAL_INCOME, ACTUAL_EXPENSES, " +
                    "CREATED_AT, UPDATED_AT, DIRECTOR_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            Long id = insert(sql,
                    Date.valueOf(budget.getBudgetDate()),
                    budget.getPlannedIncome(),
                    budget.getPlannedExpenses(),
                    budget.getActualIncome(),
                    budget.getActualExpenses(),
                    budget.getCreatedAt() != null ? budget.getCreatedAt() : now,
                    budget.getUpdatedAt() != null ? budget.getUpdatedAt() : now,
                    budget.getDirector().getId());
            if (id != null) {
                budget.setId(id);
            }
            return id;
        } else {
            boolean updated = update(budget);
            return updated ? budget.getId() : null;
        }
    }

    public boolean update(MonthlyBudget budget) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE " + MONTHLY_BUDGET_TABLE +
                " SET BUDGET_DATE = ?, PLANNED_INCOME = ?, PLANNED_EXPENSES = ?, " +
                "ACTUAL_INCOME = ?, ACTUAL_EXPENSES = ?, " +
                "UPDATED_AT = ?, DIRECTOR_ID = ? WHERE ID = ?";
        return update(sql,
                budget.getBudgetDate(),
                budget.getPlannedIncome(),
                budget.getPlannedExpenses(),
                budget.getActualIncome(),
                budget.getActualExpenses(),
                now,
                budget.getDirector().getId(),
                budget.getId());
    }
}