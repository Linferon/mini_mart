package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Expense {
    private Long id;
    private ExpenseCategory category;
    private BigDecimal totalAmount;
    private Timestamp expenseDate;
    private User accountant;

    public Expense(Long id, ExpenseCategory category, BigDecimal totalAmount, Timestamp expenseDate, User accountant) {
        this.id = id;
        this.category = category;
        this.totalAmount = totalAmount;
        this.expenseDate = expenseDate;
        this.accountant = accountant;
    }

    public Long getId() {
        return id;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Timestamp getExpenseDate() {
        return expenseDate;
    }

    public User getAccountant() {
        return accountant;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setExpenseDate(Timestamp expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setAccountant(User accountant) {
        this.accountant = accountant;
    }
}