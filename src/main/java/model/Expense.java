package model;

import java.math.BigDecimal;
import java.sql.Timestamp;


public class Expense {
    private Long id;
    private Long categoryId;
    private BigDecimal totalAmount;
    private Timestamp expenseDate;
    private Long accountantId;

    public Expense(Long id,Long categoryId, BigDecimal totalAmount, Timestamp expenseDate, Long accountantId) {
        this.id = id;
        this.categoryId = categoryId;
        this.totalAmount = totalAmount;
        this.expenseDate = expenseDate;
        this.accountantId = accountantId;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Timestamp getExpenseDate() {
        return expenseDate;
    }

    public Long getAccountantId() {
        return accountantId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setExpenseDate(Timestamp expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setAccountantId(Long accountantId) {
        this.accountantId = accountantId;
    }
}
