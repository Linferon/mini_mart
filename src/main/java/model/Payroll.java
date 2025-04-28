package model;

import util.TableFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Payroll implements FormattableEntity {
    private Long id;
    private final User employee;
    private User accountant;
    private final Float hoursWorked;
    private final BigDecimal hourlyRate;
    private BigDecimal totalAmount;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final LocalDate paymentDate;
    private final Boolean isPaid;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private static final int ID_WIDTH = 5;
    private static final int EMPLOYEE_WIDTH = 25;
    private static final int HOURS_WIDTH = 10;
    private static final int RATE_WIDTH = 10;
    private static final int AMOUNT_WIDTH = 15;
    private static final int PERIOD_WIDTH = 25;
    private static final int STATUS_WIDTH = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Payroll(Long id, User employee, User accountant, Float hoursWorked, BigDecimal hourlyRate, BigDecimal totalAmount,
                   LocalDate periodStart, LocalDate periodEnd, LocalDate paymentDate, Boolean isPaid,
                   Timestamp createdAt, Timestamp updatedAt) {
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

    @Override
    public String toString() {
        return "Зарплата" +
                "\nid: " + id +
                "\nсотрудник: " + (employee != null ? employee.getFullName() : "не указан") +
                "\nчасов отработано: " + hoursWorked +
                "\nставка: " + hourlyRate +
                "\nсумма: " + totalAmount +
                "\nпериод: " + formatPeriod() +
                "\nстатус: " + (Boolean.TRUE.equals(isPaid) ? "Выплачена" : "Не выплачена") +
                (paymentDate != null ? "\nдата выплаты: " + paymentDate.format(DATE_FORMATTER) : "");
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Сотрудник", EMPLOYEE_WIDTH) +
                TableFormatter.formatCell("Часы", HOURS_WIDTH) +
                TableFormatter.formatCell("Ставка", RATE_WIDTH) +
                TableFormatter.formatCell("Сумма", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Период", PERIOD_WIDTH) +
                TableFormatter.formatCell("Статус", STATUS_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(employee != null ? employee.getFullName() : "-", EMPLOYEE_WIDTH) +
                TableFormatter.formatCell(hoursWorked, HOURS_WIDTH) +
                TableFormatter.formatCell(hourlyRate, RATE_WIDTH) +
                TableFormatter.formatCell(totalAmount, AMOUNT_WIDTH) +
                TableFormatter.formatCell(formatPeriod(), PERIOD_WIDTH) +
                TableFormatter.formatCell(formatStatus(), STATUS_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, EMPLOYEE_WIDTH, HOURS_WIDTH, RATE_WIDTH, AMOUNT_WIDTH, PERIOD_WIDTH, STATUS_WIDTH);
    }

    public String formatPeriod() {
        if (periodStart == null || periodEnd == null) {
            return "-";
        }
        return periodStart.format(DATE_FORMATTER) + " - " + periodEnd.format(DATE_FORMATTER);
    }

    public String formatStatus() {
        return Boolean.TRUE.equals(isPaid) ? "Выплачена" : "Не выплачена";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEmployee() {
        return employee;
    }

    public User getAccountant() {
        return accountant;
    }

    public void setAccountant(User accountant) {
        this.accountant = accountant;
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

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}