package com.example.inventorymanagementsystem.data_model;

import java.util.ArrayList;
import java.util.List;

public class Product {

    private int id;
    private String name;
    private List<Expiration> expirations;

    public Product() {
    }

    public Product(int id, String name, List<Expiration> expirations) {
        this.id = id;
        this.name = name;
        this.expirations = expirations;
    }

    public Product(String name, List<Expiration> expirations) {
        this.name = name;
        this.expirations = expirations;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Expiration> getExpirations() {
        return expirations;
    }
}
