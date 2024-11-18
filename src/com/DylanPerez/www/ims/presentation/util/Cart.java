package com.DylanPerez.www.ims.presentation.util;

import com.DylanPerez.www.ims.application.itemtype.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.presentation.Store;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Comparable<Cart> {

    public enum CartType {
        PHYSICAL,
        VIRTUAL
    }

    private String id;
    private Map<String, Integer> products; // SKU string, quantity
    private LocalDate date;
    private CartType type;

    private Store store;
    private boolean withdrawn;

    public Cart(String id, CartType type, Store store) {
        this.id = id;
        this.type = type;
        this.store = store;
        date = LocalDate.now();
        products = new HashMap<>();
        withdrawn = false;
    }

    public boolean addItem(String sku, int quantity) {
        final boolean debug = true;

        if(quantity <= 0) return false;
        InventoryItem item = store.searchItem(sku);
        if(item != null && item.isForSale()) {
            products.merge(sku, quantity, (k, v) -> v + quantity);
            item.reserveItem(quantity);

            if(debug) {
                System.out.println("Cart#addItem() :");
                store.listInventory();
            }

            return true;
        }
        return false;
    }

    public boolean removeItem(String sku, int quantity) {
        final boolean debug = true;
        if(quantity <= 0 || !products.containsKey(sku)) return false;
        Integer actualQty = products.get(sku);
        InventoryItem item = store.searchItem(sku);
        
        if(actualQty - quantity <= 0) {
            products.remove(sku);
            item.releaseItem(actualQty);
        } else {
            products.compute(sku, (k, v) -> v - quantity);
            item.releaseItem(quantity);
        }

        if(debug) {
            System.out.println("Cart#removeItem() :");
            store.listInventory();
        }
        
        return true;
    }

    public boolean isEmpty() {
        return products.isEmpty();
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

    public boolean isWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(boolean withdrawn) {
        this.withdrawn = withdrawn;
    }

    public void printSalesSlip() {
        System.out.println("-----------------  Sales Slip -----------------");
        products.forEach((k, v) -> {
            InventoryItem item = store.searchItem(k);
            Product p = item.getProduct();
            System.out.println("* [" + v + "] " + p.getName() + " by " + p.getManufacturer() + " | " + v + " x " + item.getSalesPrice() + "(" + v * item.getSalesPrice() + ")");
        });
        System.out.println("------------------------------------------------");
        products.clear();
    }

    @Override
    public int compareTo(Cart o) {
        return id.compareTo(o.id);
    }
}
