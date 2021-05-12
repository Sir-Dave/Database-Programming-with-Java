package com.sirdave.database.Models;

import java.time.LocalDate;

public class Order {
    private String name;
    private int quantity;
    private LocalDate date;

    public Order(String name, int quantity, LocalDate date){
        this.name = name;
        this.quantity = quantity;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
}
