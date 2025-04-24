package model;

import util.TableFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Income implements FormattableEntity {
    private Long id;
    private IncomeSource source;
    private BigDecimal totalAmount;
    private Timestamp incomeDate;
    private User accountant;

    private static final int ID_WIDTH = 5;
    private static final int SOURCE_WIDTH = 25;
    private static final int AMOUNT_WIDTH = 15;
    private static final int DATE_WIDTH = 20;
    private static final int ACCOUNTANT_WIDTH = 25;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Income(Long id, IncomeSource source, BigDecimal totalAmount, Timestamp incomeDate, User accountant) {
        this.id = id;
        this.source = source;
        this.totalAmount = totalAmount;
        this.incomeDate = incomeDate;
        this.accountant = accountant;
    }

    @Override
    public String toString() {
        return "Доход" +
                "\nid: " + id +
                "\nисточник: " + (source != null ? source.getName() : "не указан") +
                "\nсумма: " + totalAmount +
                "\nдата: " + (incomeDate != null ? incomeDate.toLocalDateTime().format(DATE_FORMATTER) : "не указана") +
                "\nбухгалтер: " + (accountant != null ? accountant.getFullName() : "не указан");
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Источник", SOURCE_WIDTH) +
                TableFormatter.formatCell("Сумма", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Дата", DATE_WIDTH) +
                TableFormatter.formatCell("Бухгалтер", ACCOUNTANT_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(source != null ? source.getName() : "-", SOURCE_WIDTH) +
                TableFormatter.formatCell(totalAmount, AMOUNT_WIDTH) +
                TableFormatter.formatCell(getFormattedIncomeDate(), DATE_WIDTH) +
                TableFormatter.formatCell(accountant != null ? accountant.getFullName() : "-", ACCOUNTANT_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, SOURCE_WIDTH, AMOUNT_WIDTH, DATE_WIDTH, ACCOUNTANT_WIDTH);
    }

    public String getFormattedIncomeDate() {
        return incomeDate != null ?
                incomeDate.toLocalDateTime().format(DATE_FORMATTER) : "-";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IncomeSource getSource() {
        return source;
    }

    public void setSource(IncomeSource source) {
        this.source = source;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getIncomeDate() {
        return incomeDate;
    }

    public void setIncomeDate(Timestamp incomeDate) {
        this.incomeDate = incomeDate;
    }

    public User getAccountant() {
        return accountant;
    }

    public void setAccountant(User accountant) {
        this.accountant = accountant;
    }
}