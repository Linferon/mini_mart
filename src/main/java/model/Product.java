package model;

import util.TableFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product implements FormattableEntity {
    private Long id;
    private String name;
    private ProductCategory category;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private static final int ID_WIDTH = 5;
    private static final int NAME_WIDTH = 30;
    private static final int CATEGORY_WIDTH = 20;
    private static final int PRICE_WIDTH = 15;
    public Product(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Product(Long id, String name, ProductCategory category) {
        this(id, name);
        this.category = category;
    }

    public Product(Long id, String name, ProductCategory category, BigDecimal buyPrice, BigDecimal sellPrice, Timestamp createdAt, Timestamp updatedAt) {
        this(id, name, category);
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Продукт" +
                "\nid:" + id +
                "\nназвание: " + name +
                "\nкатегория: " + (category != null ? category.getName() : "не указана") +
                "\nцена закупки: " + buyPrice +
                "\nцена продажи: " + sellPrice;
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Название", NAME_WIDTH) +
                TableFormatter.formatCell("Категория", CATEGORY_WIDTH) +
                TableFormatter.formatCell("Цена закупки", PRICE_WIDTH) +
                TableFormatter.formatCell("Цена продажи", PRICE_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(name, NAME_WIDTH) +
                TableFormatter.formatCell(category.getName(), CATEGORY_WIDTH) +
                TableFormatter.formatCell(buyPrice, PRICE_WIDTH) +
                TableFormatter.formatCell(sellPrice, PRICE_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, NAME_WIDTH, CATEGORY_WIDTH, PRICE_WIDTH, PRICE_WIDTH);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
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