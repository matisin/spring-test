package com.apex.worker.domain.entity;

import java.util.ArrayList;

public class Order {
    private String orderId;
    private String customerId;
    private String customerName;
    private ArrayList<Product> products;

    // Constructor
    public Order(String orderId, String customerId, String customerName, ArrayList<Product> products) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.products = products;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }
}
