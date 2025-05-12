package model;

import util.TableFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Purchase implements FormattableEntity {
    private Long id;
    private Product product;
    private Integer quantity;
    private User stockKeeper;
    private Timestamp purchaseDate;
    private final BigDecimal totalCost;

    private static final int ID_WIDTH = 5;
    private static final int PRODUCT_WIDTH = 30;
    private static final int QUANTITY_WIDTH = 10;
    private static final int COST_WIDTH = 15;
    private static final int KEEPER_WIDTH = 20;
    private static final int DATE_WIDTH = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Purchase(Product product, Integer quantity, BigDecimal totalCost) {
        this.product = product;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.purchaseDate = new Timestamp(System.currentTimeMillis());
    }

    public Purchase(Long id, Product product, Integer quantity, User stockKeeper, Timestamp purchaseDate, BigDecimal totalCost) {
        this(product, quantity, totalCost);
        this.id = id;
        this.stockKeeper = stockKeeper;
        this.purchaseDate = purchaseDate;
    }

    @Override
    public String toString() {
        return "Закупка: " +
                "\nid: " + id +
                "\nпродукт: " + (product != null ? product.getName() : "не указан") +
                "\nколичество: " + quantity +
                "\nкладовщик: " + (stockKeeper != null ? stockKeeper.getFullName() : "не указан") +
                "\nдата: " + (purchaseDate != null ? purchaseDate.toLocalDateTime().format(DATE_FORMATTER) : "не указана") +
                "\nстоимость: " + totalCost ;
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Продукт", PRODUCT_WIDTH) +
                TableFormatter.formatCell("Кол-во", QUANTITY_WIDTH) +
                TableFormatter.formatCell("Стоимость", COST_WIDTH) +
                TableFormatter.formatCell("Кладовщик", KEEPER_WIDTH) +
                TableFormatter.formatCell("Дата", DATE_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(product != null ? product.getName() : "-", PRODUCT_WIDTH) +
                TableFormatter.formatCell(quantity, QUANTITY_WIDTH) +
                TableFormatter.formatCell(totalCost, COST_WIDTH) +
                TableFormatter.formatCell(stockKeeper != null ? stockKeeper.getFullName() : "-", KEEPER_WIDTH) +
                TableFormatter.formatCell(getFormattedPurchaseDate(), DATE_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, PRODUCT_WIDTH, QUANTITY_WIDTH, COST_WIDTH, KEEPER_WIDTH, DATE_WIDTH);
    }

    public String getFormattedPurchaseDate() {
        return purchaseDate != null ?
                purchaseDate.toLocalDateTime().format(DATE_FORMATTER) : "-";
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

    public User getStockKeeper() {
        return stockKeeper;
    }

    public void setStockKeeper(User stockKeeper) {
        this.stockKeeper = stockKeeper;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
}