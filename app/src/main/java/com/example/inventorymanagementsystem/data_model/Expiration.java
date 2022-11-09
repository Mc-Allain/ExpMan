package com.example.inventorymanagementsystem.data_model;

public class Expiration {

    private int id;
    private int productId;
    private long date;
    private String tag;

    public Expiration() {
    }

    public Expiration(int id, int productId, long date, String tag) {
        this.id = id;
        this.productId = productId;
        this.date = date;
        this.tag = tag;
    }

    public Expiration(int productId, long date, String tag) {
        this.productId = productId;
        this.date = date;
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public long getDate() {
        return date;
    }

    public String getTag() {
        return tag;
    }
}
