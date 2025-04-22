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

public class MonthlyBudgetDao extends Dao<MonthlyBudget> {

    @Override
    protected String getTableName() {
        return "MONTHLY_BUDGETS";
    }

    @Override
    protected Function<ResultSet, MonthlyBudget> getMapper() {
        return MonthlyBudgetMapper::mapRow;
    }

    public List<MonthlyBudget> findByDirector(Long directorId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE DIRECTOR_ID = ?";
        return queryList(sql, directorId);
    }

    public List<MonthlyBudget> findByDateRange(Date startDate, Date endDate) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE BUDGET_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Optional<MonthlyBudget> findByDate(Date date) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE BUDGET_DATE = ?";
        return querySingle(sql, date);
    }

    public Long save(MonthlyBudget budget) {
        if (budget.getId() == null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());

            String sql = "INSERT INTO " + getTableName() +
                    " (BUDGET_DATE, PLANNED_INCOME, PLANNED_EXPENSES, " +
                    "ACTUAL_INCOME, ACTUAL_EXPENSES, NET_RESULT, " +
                    "CREATED_AT, UPDATED_AT, DIRECTOR_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Long id = insert(sql,
                    budget.getBudgetDate(),
                    budget.getPlannedIncome(),
                    budget.getPlannedExpenses(),
                    budget.getActualIncome(),
                    budget.getActualExpenses(),
                    budget.getNetResult(),
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

        String sql = "UPDATE " + getTableName() +
                " SET BUDGET_DATE = ?, PLANNED_INCOME = ?, PLANNED_EXPENSES = ?, " +
                "ACTUAL_INCOME = ?, ACTUAL_EXPENSES = ?, NET_RESULT = ?, " +
                "UPDATED_AT = ?, DIRECTOR_ID = ? WHERE ID = ?";
        return update(sql,
                budget.getBudgetDate(),
                budget.getPlannedIncome(),
                budget.getPlannedExpenses(),
                budget.getActualIncome(),
                budget.getActualExpenses(),
                budget.getNetResult(),
                now,
                budget.getDirector().getId(),
                budget.getId());
    }
}