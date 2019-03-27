package com.annabenson.viand;

import java.util.ArrayList;

public class StoreAccount extends Account {

    private String storeName;

    public StoreAccount(String email, String password, String phoneNumber, ArrayList<Order> orderHistory, String storeName) {
        super(email, password, phoneNumber, orderHistory);
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
