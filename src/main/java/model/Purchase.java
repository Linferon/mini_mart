package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Purchase {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Long stockKeeperId;
    private Timestamp purchaseDate;
    private BigDecimal totalCost;

    public Purchase(Long id, Long productId, Integer quantity, Long stockKeeperId, Timestamp purchaseDate, BigDecimal totalCost) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.stockKeeperId = stockKeeperId;
        this.purchaseDate = purchaseDate;
        this.totalCost = totalCost;
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

    public Long getStockKeeperId() {
        return stockKeeperId;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
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

    public void setStockKeeperId(Long stockKeeperId) {
        this.stockKeeperId = stockKeeperId;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}
