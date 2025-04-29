package model;

import util.TableFormatter;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Stock implements FormattableEntity, TimestampedEntity {
    private Product product;
    private Integer quantity;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private static final int PRODUCT_ID_WIDTH = 5;
    private static final int PRODUCT_WIDTH = 30;
    private static final int QUANTITY_WIDTH = 10;
    private static final int UPDATED_WIDTH = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Stock(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }


    public Stock(Product product, Integer quantity, Timestamp createdAt, Timestamp updatedAt) {
        this(product, quantity);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Остаток на складе {" +
                "\nпродукт: " + (product != null ? product.getName() : "не указан") +
                "\nколичество: " + quantity +
                "\nпоследнее обновление: " + (updatedAt != null ? updatedAt.toLocalDateTime().format(DATE_FORMATTER) : "не указано");
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", PRODUCT_ID_WIDTH) +
                TableFormatter.formatCell("Продукт", PRODUCT_WIDTH) +
                TableFormatter.formatCell("Количество", QUANTITY_WIDTH) +
                TableFormatter.formatCell("Обновлено", UPDATED_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(product != null ? product.getId() : "-", PRODUCT_ID_WIDTH) +
                TableFormatter.formatCell(product != null ? product.getName() : "-", PRODUCT_WIDTH) +
                TableFormatter.formatCell(quantity, QUANTITY_WIDTH) +
                TableFormatter.formatCell(getFormattedUpdatedAt(), UPDATED_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(PRODUCT_ID_WIDTH, PRODUCT_WIDTH, QUANTITY_WIDTH, UPDATED_WIDTH);
    }

    public String getFormattedUpdatedAt() {
        return updatedAt != null ?
                updatedAt.toLocalDateTime().format(DATE_FORMATTER) : "-";
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