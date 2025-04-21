package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Sale {
    private Long id;
    private Product product;
    private Integer quantity;
    private User cashier;
    private BigDecimal totalAmount;
    private Timestamp saleDate;

    public Sale(Long id, Product product, Integer quantity, User cashier, BigDecimal totalAmount, Timestamp saleDate) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.cashier = cashier;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public User getCashier() {
        return cashier;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setCashier(User cashier) {
        this.cashier = cashier;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }
}