package com.example.CouponManagement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItem {
	
	@JsonProperty("product_id")
    private Long productId;
	
    private int quantity;
    private double price;
    private double totalDiscount;

    // Constructors
    public CartItem() {}

    public CartItem(Long productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.totalDiscount = 0.0;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }
}
