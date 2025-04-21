package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public class MonthlyBudget {
    private Long id;
    private LocalDate budgetDate;
    private BigDecimal plannedIncome;
    private BigDecimal plannedExpenses;
    private BigDecimal actualIncome;
    private BigDecimal actualExpenses;
    private BigDecimal netResult;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private User director;

    public MonthlyBudget(Long id, LocalDate budgetDate, BigDecimal plannedIncome, BigDecimal plannedExpenses, BigDecimal actualIncome, BigDecimal actualExpenses, BigDecimal netResult, Timestamp createdAt, Timestamp updatedAt, User director) {
        this.id = id;
        this.budgetDate = budgetDate;
        this.plannedIncome = plannedIncome;
        this.plannedExpenses = plannedExpenses;
        this.actualIncome = actualIncome;
        this.actualExpenses = actualExpenses;
        this.netResult = netResult;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.director = director;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getBudgetDate() {
        return budgetDate;
    }

    public BigDecimal getPlannedIncome() {
        return plannedIncome;
    }

    public BigDecimal getPlannedExpenses() {
        return plannedExpenses;
    }

    public BigDecimal getActualIncome() {
        return actualIncome;
    }

    public BigDecimal getActualExpenses() {
        return actualExpenses;
    }

    public BigDecimal getNetResult() {
        return netResult;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public User getDirector() {
        return director;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBudgetDate(LocalDate budgetDate) {
        this.budgetDate = budgetDate;
    }

    public void setPlannedIncome(BigDecimal plannedIncome) {
        this.plannedIncome = plannedIncome;
    }

    public void setPlannedExpenses(BigDecimal plannedExpenses) {
        this.plannedExpenses = plannedExpenses;
    }

    public void setActualIncome(BigDecimal actualIncome) {
        this.actualIncome = actualIncome;
    }

    public void setActualExpenses(BigDecimal actualExpenses) {
        this.actualExpenses = actualExpenses;
    }

    public void setNetResult(BigDecimal netResult) {
        this.netResult = netResult;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDirector(User director) {
        this.director = director;
    }
}