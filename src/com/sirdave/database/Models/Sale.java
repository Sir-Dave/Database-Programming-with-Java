package com.sirdave.database.Models;

import java.time.LocalDate;
import java.util.List;

public class Sale {
    private int id;
    private List<Product> products;
    private double amount;
    private double discount;
    private LocalDate date;
    private Customer customer;

    public Sale(int id, List<Product> products, double amount, double discount, LocalDate date, Customer customer) {
        this.id = id;
        this.products = products;
        this.amount = amount;
        this.discount = discount;
        this.date = date;
        this.customer = customer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
