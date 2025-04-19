package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Income {
    private Long id;
    private Long sourceId;
    private BigDecimal totalAmount;
    private Timestamp incomeDate;
    private Long accountantId;

    public Income(Long id, Long sourceId, BigDecimal totalAmount, Timestamp incomeDate, Long accountantId) {
        this.id = id;
        this.sourceId = sourceId;
        this.totalAmount = totalAmount;
        this.incomeDate = incomeDate;
        this.accountantId = accountantId;
    }

    public Long getId() {
        return id;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Timestamp getIncomeDate() {
        return incomeDate;
    }

    public Long getAccountantId() {
        return accountantId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setIncomeDate(Timestamp incomeDate) {
        this.incomeDate = incomeDate;
    }

    public void setAccountantId(Long accountantId) {
        this.accountantId = accountantId;
    }
}
