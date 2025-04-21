package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Payroll {
    private Long id;
    private User employee;
    private User accountant;
    private Float hoursWorked;
    private BigDecimal hourlyRate;
    private BigDecimal totalAmount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate paymentDate;
    private Boolean isPaid;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Payroll(Long id, User employee, User accountant, Float hoursWorked, BigDecimal hourlyRate, BigDecimal totalAmount, LocalDate periodStart, LocalDate periodEnd, LocalDate paymentDate, Boolean isPaid, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.employee = employee;
        this.accountant = accountant;
        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
        this.totalAmount = totalAmount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.paymentDate = paymentDate;
        this.isPaid = isPaid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public User getEmployee() {
        return employee;
    }

    public User getAccountant() {
        return accountant;
    }

    public Float getHoursWorked() {
        return hoursWorked;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public Boolean isPaid() {
        return isPaid;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public void setAccountant(User accountant) {
        this.accountant = accountant;
    }

    public void setHoursWorked(Float hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}