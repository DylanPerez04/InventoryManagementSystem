package com.DylanPerez.www.ims.presentation.util;

import com.DylanPerez.www.ims.application.inventory.interfaces.inventory.InventoryAccessor;
import com.DylanPerez.www.ims.application.inventory.proxy.InventoryProxy;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemAccessor;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Comparable<Cart> {

    private final String id;
    private final Map<String, Integer> products; // SKU string, quantity
    private final LocalDate date;

    private double total;

    /**
     * A reference to an <code>InventoryAccessor</code> associated with
     * the active <code>inventory</code> being handled by an <code>IMS</code>
     * object, allowing real-time interaction with the available stock managed by the
     * client.
     */
    private final InventoryAccessor liveInventory;

    private boolean withdrawn;

    public Cart(String id,  InventoryProxy liveInventory) {
        this.id = id;
        this.products = new HashMap<>();
        this.date = LocalDate.now();
        this.total = 0;
        this.liveInventory = liveInventory;
        this.withdrawn = true;
    }

    public int addItem(String sku, int quantity) {
        final boolean debug = true;

        if(quantity <= 0) return 0;
        InventoryItemUpdater item = liveInventory.get(sku);
        if(item != null) {
            int qtyReserved = item.reserveItem(quantity);
            products.merge(sku, qtyReserved, (k, v) -> v + qtyReserved);
            total += (products.get(sku) * item.getPrice());
            return qtyReserved;
        }
        throw new IllegalArgumentException("Unable to add unknown sku(" + sku + ") to Cart(id = " + id +")");
    }

    public int removeItem(String sku, int quantity) {
        final boolean debug = true;
        if(quantity <= 0) return 0;
        if(!products.containsKey(sku)) throw new IllegalArgumentException("No sku(" + sku + ") in Cart(id = " + id +")");

        InventoryItemUpdater item = liveInventory.get(sku);

        /// Should not happen; checks if there has somehow been an excess quantity of item releases by other carts.
        if(item.getQtyReserved() < products.get(sku))
            throw new RuntimeException("Quantity reserved for item(" + item + ") is less than what " +
                    "has been reserved by Cart(" + id + ").");

        int toRemove;
        if(quantity >= products.get(sku)) { ///< If attempting to remove more of item than in cart
            toRemove = products.get(sku);
            products.remove(sku);
        } else {
            toRemove = quantity;
            /// Will always be present (we literally check for it at the start of the method)
            products.computeIfPresent(sku, (k, v) -> v - toRemove);
        }
        total -= (toRemove * item.getPrice());
        return item.releaseItem(toRemove);
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

    public boolean isWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(boolean withdrawn) {
        this.withdrawn = withdrawn;
    }

    public void printSalesSlip() {
        System.out.println("-----------------  Sales Slip -----------------");
        products.forEach((k, v) -> {
            InventoryItemAccessor item = liveInventory.get(k);
            System.out.println("* [" + v + "] " + item.getName() + " by " + item.getManufacturer() + " | " + v + " x " + item.getPrice() + "(" + v * item.getPrice() + ")");
        });
        System.out.println("------------------------------------------------");
        products.clear();
    }

    @Override
    public int compareTo(Cart o) {
        return id.compareTo(o.id);
    }
}
