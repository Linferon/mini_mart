package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Payroll {
    private Long id;
    private Long employeeId;
    private Long accountantId;
    private Float hoursWorked;
    private BigDecimal hourlyRate;
    private BigDecimal totalAmount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate paymentDate;
    private Boolean isPaid;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Payroll(Long id, Long employeeId, Long accountantId, Float hoursWorked, BigDecimal hourlyRate, BigDecimal totalAmount, LocalDate periodStart, LocalDate periodEnd, LocalDate paymentDate, Boolean isPaid, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.accountantId = accountantId;
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

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getAccountantId() {
        return accountantId;
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

    public Boolean getPaid() {
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

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setAccountantId(Long accountantId) {
        this.accountantId = accountantId;
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
