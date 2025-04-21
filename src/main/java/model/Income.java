package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Income {
    private Long id;
    private IncomeSource source;
    private BigDecimal totalAmount;
    private Timestamp incomeDate;
    private User accountant;

    public Income(Long id, IncomeSource source, BigDecimal totalAmount, Timestamp incomeDate, User accountant) {
        this.id = id;
        this.source = source;
        this.totalAmount = totalAmount;
        this.incomeDate = incomeDate;
        this.accountant = accountant;
    }

    public Long getId() {
        return id;
    }

    public IncomeSource getSource() {
        return source;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Timestamp getIncomeDate() {
        return incomeDate;
    }

    public User getAccountant() {
        return accountant;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSource(IncomeSource source) {
        this.source = source;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setIncomeDate(Timestamp incomeDate) {
        this.incomeDate = incomeDate;
    }

    public void setAccountant(User accountant) {
        this.accountant = accountant;
    }
}