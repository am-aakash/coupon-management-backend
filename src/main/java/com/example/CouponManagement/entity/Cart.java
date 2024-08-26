package com.example.CouponManagement.entity;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items = new ArrayList<>();
    private double totalPrice;
    private double totalDiscount;
    private double finalPrice;

    // Constructors
    public Cart() {}

    public Cart(List<CartItem> items) {
        this.items = items;
        this.totalPrice = 0.0;
        this.totalDiscount = 0.0;
        this.finalPrice = 0.0;
    }

    // Getters and Setters
    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
