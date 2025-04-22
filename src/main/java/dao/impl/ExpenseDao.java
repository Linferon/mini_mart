package dao.impl;

import dao.Dao;
import model.Expense;
import dao.mapper.ExpenseMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public class ExpenseDao extends Dao<Expense> {
    
    @Override
    protected String getTableName() {
        return "EXPENSES";
    }
    
    @Override
    protected Function<ResultSet, Expense> getMapper() {
        return ExpenseMapper::mapRow;
    }

    public List<Expense> findByCategory(Long categoryId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE CATEGORY_ID = ?";
        return queryList(sql, categoryId);
    }

    public List<Expense> findByAccountant(Long accountantId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ACCOUNTANT_ID = ?";
        return queryList(sql, accountantId);
    }

    public List<Expense> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE EXPENSE_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Expense expense) {
        if (expense.getId() == null) {
            String sql = "INSERT INTO " + getTableName() +
                    " (CATEGORY_ID, TOTAL_AMOUNT, EXPENSE_DATE, ACCOUNTANT_ID) " +
                    "VALUES (?, ?, ?, ?)";
            Long id = insert(sql,
                    expense.getCategory().getId(),
                    expense.getTotalAmount(),
                    expense.getExpenseDate() != null ? expense.getExpenseDate() : Timestamp.from(Instant.now()),
                    expense.getAccountant().getId());
            if (id != null) {
                expense.setId(id);
            }
            return id;
        } else {
            boolean updated = update(expense);
            return updated ? expense.getId() : null;
        }
    }

    public boolean update(Expense expense) {
        String sql = "UPDATE " + getTableName() +
                " SET CATEGORY_ID = ?, TOTAL_AMOUNT = ?, " +
                "EXPENSE_DATE = ?, ACCOUNTANT_ID = ? WHERE ID = ?";
        return update(sql,
                expense.getCategory().getId(),
                expense.getTotalAmount(),
                expense.getExpenseDate(),
                expense.getAccountant().getId(),
                expense.getId());
    }
}