package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Sale {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Long cashierId;
    private BigDecimal totalAmount;
    private Timestamp saleDate;

    public Sale(Long id, Long productId, Integer quantity, Long cashierId, BigDecimal totalAmount, Timestamp saleDate) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.cashierId = cashierId;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Long getCashierId() {
        return cashierId;
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

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setCashierId(Long cashierId) {
        this.cashierId = cashierId;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }
}
