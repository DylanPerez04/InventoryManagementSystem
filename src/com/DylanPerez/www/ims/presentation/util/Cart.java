package com.DylanPerez.www.ims.presentation.util;

import com.DylanPerez.www.ims.application.itemtype.InventoryItem;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Cart {

    public enum CartType {
        PHYSICAL,
        VIRTUAL;
    }

    private String id;
    private Map<String, Integer> products; // SKU string, quantity
    private LocalDate date;
    private CartType type;

    public Cart(String id, CartType type) {
        this.id = id;
        this.type = type;
        date = LocalDate.now();
        products = new HashMap<>();
    }

    // TODO: Update InventoryItem#qtyReserved when adding and removing items from cart

    public boolean addItem(String productSku, int quantity) {
        if(quantity <= 0) return false;
        products.merge(productSku, quantity, (k, v) -> v + quantity);
        return true;
    }

    public boolean removeItem(String productSku, int quantity) {
        if(quantity <= 0 || !products.containsKey(productSku)) return false;
        Integer actualQty = products.get(productSku);
        
        if(actualQty - quantity <= 0) {
            products.remove(productSku);
        } else {
            products.compute(productSku, (k, v) -> v - quantity);
        }
        
        return true;
    }

    public String getId() {
        return id;
    }

    public Map<String, Integer> getProducts() {
        return products;
    }

    public LocalDate getDate() {
        return date;
    }

    public CartType getType() {
        return type;
    }

    public void printSalesSlip(TreeSet<InventoryItem> inventory) {
        System.out.println("-----------------  Sales Slip -----------------");
        System.out.println("------------------------------------------------");
    }

}
