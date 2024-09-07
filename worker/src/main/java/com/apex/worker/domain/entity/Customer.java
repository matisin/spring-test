package com.apex.worker.domain.entity;

public class Customer {
    private String customerId;
    private String name;
    private String email;
    private String address;
    private boolean active;

    // Constructor
    public Customer(String customerId, String name, String email, String address, boolean active) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.active = active;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
