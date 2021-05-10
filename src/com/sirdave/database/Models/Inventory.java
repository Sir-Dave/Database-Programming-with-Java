package com.sirdave.database.Models;

import java.time.LocalDate;
import java.util.List;

public class Inventory {
    private int id;
    private List<Product> products;
    private LocalDate date;

    public Inventory(int id, List<Product> products, LocalDate date) {
        this.id = id;
        this.products = products;
        this.date = date;
    }

    public List<Product> getProducts() {
        return products;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
