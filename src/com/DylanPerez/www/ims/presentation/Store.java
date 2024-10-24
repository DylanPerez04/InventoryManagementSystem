package com.DylanPerez.www.ims.presentation;

import com.DylanPerez.www.ims.application.itemtype.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.presentation.util.Cart;
import com.DylanPerez.www.ims.service.simulate.Simulatable;

import java.util.*;


public class Store implements Simulatable {

    private Map<String, InventoryItem> inventory; // HashMap<SKU string, InventoryItem>
    private Map<Cart.CartType, Cart> carts; // HashMap
    private Set<Product> aisleInventory; // TreeSet to accommodate search for both online and in store effectively

    public Store() {
        inventory = new HashMap<>();
        carts = new HashMap<>();
        aisleInventory = new TreeSet<>(Comparator.comparing(Product::getCategory)
                .thenComparing(Product::getName)
                .thenComparing(Product::getManufacturer));
    }

    @Override
    public boolean simulate(int duration) {
        int customer_chance

        for(int i = 0; i < duration; i++) {

            if(Math.random() * 100 + 1)

        }
        return true;
    }

    /*
     * manageStoreCarts();
     * checkOutCart();
     * abandonCarts();
     * listProductsByCategory();
     */

}
