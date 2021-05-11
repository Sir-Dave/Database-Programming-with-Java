package com.sirdave.database.Models;

import java.time.LocalDate;

public class Inventory {
    private int productId;
    private String name;
    private int quantity;
    private LocalDate date;

    public Inventory(int productId, String name, int quantity) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
    }

    public Inventory(int productId, String name, int quantity, LocalDate date) {
        this.productId = productId;
        this.date = date;
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
