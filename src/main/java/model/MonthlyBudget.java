package model;

import util.TableFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MonthlyBudget implements FormattableEntity {
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

    private static final int ID_WIDTH = 5;
    private static final int DATE_WIDTH = 12;
    private static final int AMOUNT_WIDTH = 15;
    private static final int DIRECTOR_WIDTH = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MonthlyBudget(Long id, LocalDate budgetDate, BigDecimal plannedIncome, BigDecimal plannedExpenses,
                         BigDecimal actualIncome, BigDecimal actualExpenses, BigDecimal netResult,
                         Timestamp createdAt, Timestamp updatedAt, User director) {
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

    @Override
    public String toString() {
        return "Бюджет" +
                "\nid: " + id +
                "\nдата: " + (budgetDate != null ? budgetDate.format(DATE_FORMATTER) : "не указана") +
                "\nплановый доход: " + plannedIncome +
                "\nплановые расходы: " + plannedExpenses +
                "\nфактический доход: " + actualIncome +
                "\nфактические расходы: " + actualExpenses +
                "\nчистый результат: " + netResult +
                "\nдиректор: " + (director != null ? director.getFullName() : "не указан");
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Дата", DATE_WIDTH) +
                TableFormatter.formatCell("План доход", AMOUNT_WIDTH) +
                TableFormatter.formatCell("План расход", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Факт доход", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Факт расход", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Результат", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Директор", DIRECTOR_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(getFormattedBudgetDate(), DATE_WIDTH) +
                TableFormatter.formatCell(plannedIncome, AMOUNT_WIDTH) +
                TableFormatter.formatCell(plannedExpenses, AMOUNT_WIDTH) +
                TableFormatter.formatCell(actualIncome, AMOUNT_WIDTH) +
                TableFormatter.formatCell(actualExpenses, AMOUNT_WIDTH) +
                TableFormatter.formatCell(netResult, AMOUNT_WIDTH) +
                TableFormatter.formatCell(director != null ? director.getFullName() : "-", DIRECTOR_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, DATE_WIDTH, AMOUNT_WIDTH, AMOUNT_WIDTH, AMOUNT_WIDTH, AMOUNT_WIDTH, AMOUNT_WIDTH, DIRECTOR_WIDTH);
    }

    public String getFormattedBudgetDate() {
        return budgetDate != null ? budgetDate.format(DATE_FORMATTER) : "-";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getBudgetDate() {
        return budgetDate;
    }

    public void setBudgetDate(LocalDate budgetDate) {
        this.budgetDate = budgetDate;
    }

    public BigDecimal getPlannedIncome() {
        return plannedIncome;
    }

    public void setPlannedIncome(BigDecimal plannedIncome) {
        this.plannedIncome = plannedIncome;
    }

    public BigDecimal getPlannedExpenses() {
        return plannedExpenses;
    }

    public void setPlannedExpenses(BigDecimal plannedExpenses) {
        this.plannedExpenses = plannedExpenses;
    }

    public BigDecimal getActualIncome() {
        return actualIncome;
    }

    public void setActualIncome(BigDecimal actualIncome) {
        this.actualIncome = actualIncome;
    }

    public BigDecimal getActualExpenses() {
        return actualExpenses;
    }

    public void setActualExpenses(BigDecimal actualExpenses) {
        this.actualExpenses = actualExpenses;
    }

    public BigDecimal getNetResult() {
        return netResult;
    }

    public void setNetResult(BigDecimal netResult) {
        this.netResult = netResult;
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

    public User getDirector() {
        return director;
    }

    public void setDirector(User director) {
        this.director = director;
    }
}