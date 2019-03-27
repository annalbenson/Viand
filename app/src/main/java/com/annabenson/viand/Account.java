package com.annabenson.viand;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Account implements Serializable {

    private String email; // login credential
    private String password; // login credential
    private String phoneNumber;

    private ArrayList<Order> orderHistory;

    public Account(String email, String password, String phoneNumber, ArrayList<Order> orderHistory) {
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.orderHistory = orderHistory;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ArrayList<Order> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(ArrayList<Order> orderHistory) {
        this.orderHistory = orderHistory;
    }

    @Override
    public String toString() {
        return "Account{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", orderHistory=" + orderHistory +
                '}';
    }
}
