package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Purchase {
    private Long id;
    private Product product;
    private Integer quantity;
    private User stockKeeper;
    private Timestamp purchaseDate;
    private BigDecimal totalCost;

    public Purchase(Long id, Product product, Integer quantity, User stockKeeper, Timestamp purchaseDate, BigDecimal totalCost) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.stockKeeper = stockKeeper;
        this.purchaseDate = purchaseDate;
        this.totalCost = totalCost;
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

    public User getStockKeeper() {
        return stockKeeper;
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

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setStockKeeper(User stockKeeper) {
        this.stockKeeper = stockKeeper;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}