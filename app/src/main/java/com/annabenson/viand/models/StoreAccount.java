package com.annabenson.viand.models;

public class StoreAccount extends Account {

    private String storeName;

    public StoreAccount(String email, String password, String storeName) {
        super(email, password);
        this.storeName = storeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public String toString() {
        return "StoreAccount{" + super.toString() +
                "storeName='" + storeName + '\'' +
                '}';
    }
}
