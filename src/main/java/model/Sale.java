package model;

import util.TableFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Sale implements FormattableEntity {
    private Long id;
    private Product product;
    private Integer quantity;
    private User cashier;
    private BigDecimal totalAmount;
    private Timestamp saleDate;

    private static final int ID_WIDTH = 5;
    private static final int PRODUCT_WIDTH = 30;
    private static final int QUANTITY_WIDTH = 10;
    private static final int AMOUNT_WIDTH = 15;
    private static final int CASHIER_WIDTH = 20;
    private static final int DATE_WIDTH = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Sale(Long id, Product product, Integer quantity, User cashier, BigDecimal totalAmount, Timestamp saleDate) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.cashier = cashier;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }

    @Override
    public String toString() {
        return "Продажа" +
                "\nid: " + id +
                "\nпродукт: " + (product != null ? product.getName() : "не указан") +
                "\nколичество:" + quantity +
                "\nкассир:" + (cashier != null ? cashier.getFullName() : "не указан") +
                "\nсумма: " + totalAmount +
                "\nдата: " + (saleDate != null ? saleDate.toLocalDateTime().format(DATE_FORMATTER) : "не указана");
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Продукт", PRODUCT_WIDTH) +
                TableFormatter.formatCell("Кол-во", QUANTITY_WIDTH) +
                TableFormatter.formatCell("Сумма", AMOUNT_WIDTH) +
                TableFormatter.formatCell("Кассир", CASHIER_WIDTH) +
                TableFormatter.formatCell("Дата", DATE_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(product != null ? product.getName() : "-", PRODUCT_WIDTH) +
                TableFormatter.formatCell(quantity, QUANTITY_WIDTH) +
                TableFormatter.formatCell(totalAmount, AMOUNT_WIDTH) +
                TableFormatter.formatCell(cashier != null ? cashier.getFullName() : "-", CASHIER_WIDTH) +
                TableFormatter.formatCell(getFormattedSaleDate(), DATE_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, PRODUCT_WIDTH, QUANTITY_WIDTH, AMOUNT_WIDTH, CASHIER_WIDTH, DATE_WIDTH);
    }

    public BigDecimal getUnitPrice() {
        if (totalAmount != null && quantity != null && quantity > 0) {
            return totalAmount.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public String getFormattedSaleDate() {
        return saleDate != null ?
                saleDate.toLocalDateTime().format(DATE_FORMATTER) : "-";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public User getCashier() {
        return cashier;
    }

    public void setCashier(User cashier) {
        this.cashier = cashier;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }
}