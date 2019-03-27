package com.annabenson.viand;

import java.util.ArrayList;

public class ShopperAccount extends Account {

    private String firstName;
    private String lastName;

    public ShopperAccount(String email, String password, String phoneNumber, ArrayList<Order> orderHistory, String firstName, String lastName) {
        super(email, password, phoneNumber, orderHistory);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "ShopperAccount{" + super.toString() +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
