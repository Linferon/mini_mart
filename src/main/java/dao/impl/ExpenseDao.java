package dao.impl;

import dao.Dao;
import model.Expense;
import dao.mapper.ExpenseMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dao.DbConstants.*;
public class ExpenseDao extends Dao<Expense> {

    @Override
    protected String getTableName() {
        return EXPENSE_TABLE;
    }

    @Override
    protected Function<ResultSet, Expense> getMapper() {
        return ExpenseMapper::mapRow;
    }

    @Override
    public Optional<Expense> findById(Long id) {
        String sql = "SELECT e.*, " +
                "c.NAME as CATEGORY_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + EXPENSE_TABLE + " e " +
                "LEFT JOIN " + EXPENSE_CATEGORY_TABLE + " c ON e.CATEGORY_ID = c.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON e.ACCOUNTANT_ID = u.ID " +
                "WHERE e.ID = ?";
        return querySingle(sql, id);
    }

    public Optional<Expense> findByTotalAmountAndDate(BigDecimal totalAmount, Timestamp date) {
        String sql = "SELECT e.*, " +
                "c.NAME as CATEGORY_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + EXPENSE_TABLE + " e " +
                "LEFT JOIN " + EXPENSE_CATEGORY_TABLE + " c ON e.CATEGORY_ID = c.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON e.ACCOUNTANT_ID = u.ID " +
                "WHERE e.TOTAL_AMOUNT = ? AND e.EXPENSE_DATE = ?";
        return querySingle(sql, totalAmount, date);
    }

    @Override
    public List<Expense> findAll() {
        String sql = "SELECT e.*, " +
                "c.NAME as CATEGORY_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + EXPENSE_TABLE + " e " +
                "LEFT JOIN " + EXPENSE_CATEGORY_TABLE + " c ON e.CATEGORY_ID = c.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON e.ACCOUNTANT_ID = u.ID";
        return queryList(sql);
    }

    public List<Expense> findByCategory(Long categoryId) {
        String sql = "SELECT e.*, " +
                "c.NAME as CATEGORY_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + EXPENSE_TABLE + " e " +
                "LEFT JOIN " + EXPENSE_CATEGORY_TABLE +" c ON e.CATEGORY_ID = c.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON e.ACCOUNTANT_ID = u.ID " +
                "WHERE e.CATEGORY_ID = ?";
        return queryList(sql, categoryId);
    }

    public List<Expense> findByAccountant(Long accountantId) {
        String sql = "SELECT e.*, " +
                "c.NAME as CATEGORY_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + EXPENSE_TABLE + " e " +
                "LEFT JOIN " + EXPENSE_CATEGORY_TABLE + " c ON e.CATEGORY_ID = c.ID " +
                "LEFT JOIN " + USER_TABLE + " u ON e.ACCOUNTANT_ID = u.ID " +
                "WHERE e.ACCOUNTANT_ID = ?";
        return queryList(sql, accountantId);
    }

    public List<Expense> findByDateRange(Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT e.*, " +
                "c.NAME as CATEGORY_NAME, " +
                "u.NAME as ACCOUNTANT_NAME, u.SURNAME as ACCOUNTANT_SURNAME " +
                "FROM " + EXPENSE_TABLE + " e " +
                "LEFT JOIN " + EXPENSE_CATEGORY_TABLE + " c ON e.CATEGORY_ID = c.ID " +
                "LEFT JOIN USERS u ON e.ACCOUNTANT_ID = u.ID " +
                "WHERE e.EXPENSE_DATE BETWEEN ? AND ?";
        return queryList(sql, startDate, endDate);
    }

    public Long save(Expense expense) {
        if (expense.getId() == null) {
            String sql = "INSERT INTO " + EXPENSE_TABLE +
                    " (CATEGORY_ID, TOTAL_AMOUNT, EXPENSE_DATE, ACCOUNTANT_ID) " +
                    "VALUES (?, ?, ?, ?)";
            Long id = insert(sql,
                    expense.getCategory().id(),
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
        String sql = "UPDATE " + EXPENSE_TABLE +
                " SET CATEGORY_ID = ?, TOTAL_AMOUNT = ?, " +
                "EXPENSE_DATE = ?, ACCOUNTANT_ID = ? WHERE ID = ?";
        return update(sql,
                expense.getCategory().id(),
                expense.getTotalAmount(),
                expense.getExpenseDate(),
                expense.getAccountant().getId(),
                expense.getId());
    }
}