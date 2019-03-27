package com.annabenson.viand;

import java.util.ArrayList;

public class Order {

    private ArrayList<Item> items;
    private int id;

    public Order(ArrayList<Item> items, int id) {
        setItems(items);
        setId(id);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Order{" +
                "items=" + items +
                ", id=" + id +
                '}';
    }
}
