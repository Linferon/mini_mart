package model;

import util.TableFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Expense implements FormattableEntity {
    private Long id;
    private ExpenseCategory category;
    private BigDecimal totalAmount;
    private Timestamp expenseDate;
    private User accountant;

    private static final int ID_WIDTH = 5;
    private static final int CATEGORY_WIDTH = 25;
    private static final int AMOUNT_WIDTH = 15;
    private static final int DATE_WIDTH = 20;
    private static final int ACCOUNTANT_WIDTH = 25;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Expense(ExpenseCategory category, BigDecimal totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public Expense(ExpenseCategory category, BigDecimal totalAmount, Timestamp expenseDate, User accountant) {
        this(category, totalAmount);
        this.expenseDate = expenseDate;
        this.accountant = accountant;
    }

    public Expense(Long id, ExpenseCategory category, BigDecimal totalAmount, Timestamp expenseDate, User accountant) {
        this(category, totalAmount);
        this.id = id;
        this.expenseDate = expenseDate;
        this.accountant = accountant;
    }

    @Override
    public String toString() {
        return "Расход" +
                "\nid: " + id +
                "\nкатегория: " + (category != null ? category.name() : "не указана") +
                "\nсумма: " + totalAmount +
                "\nдата: " + (expenseDate != null ? expenseDate.toLocalDateTime().format(DATE_FORMATTER) : "не указана") +
                "\nбухгалтер: " + (accountant != null ? accountant.getFullName() : "не указан");
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Категория", CATEGORY_WIDTH) +
                TableFormatter.formatCell("Сумма", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Дата", DATE_WIDTH) +
                TableFormatter.formatCell("Бухгалтер", ACCOUNTANT_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(category != null ? category.name() : "-", CATEGORY_WIDTH) +
                TableFormatter.formatCell(totalAmount, AMOUNT_WIDTH) +
                TableFormatter.formatCell(getFormattedExpenseDate(), DATE_WIDTH) +
                TableFormatter.formatCell(accountant != null ? accountant.getFullName() : "-", ACCOUNTANT_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, CATEGORY_WIDTH, AMOUNT_WIDTH, DATE_WIDTH, ACCOUNTANT_WIDTH);
    }

    public String getFormattedExpenseDate() {
        return expenseDate != null ?
                expenseDate.toLocalDateTime().format(DATE_FORMATTER) : "-";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Timestamp expenseDate) {
        this.expenseDate = expenseDate;
    }

    public User getAccountant() {
        return accountant;
    }

    public void setAccountant(User accountant) {
        this.accountant = accountant;
    }
}