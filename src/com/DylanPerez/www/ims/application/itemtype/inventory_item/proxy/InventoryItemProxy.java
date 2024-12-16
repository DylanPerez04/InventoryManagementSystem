package com.DylanPerez.www.ims.application.itemtype.inventory_item.proxy;

import com.DylanPerez.www.ims.application.itemtype.inventory_item.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;
import com.DylanPerez.www.ims.application.util.Category;

public class InventoryItemProxy implements InventoryItemUpdater {

    private final InventoryItemUpdater inventoryItem;

    public InventoryItemProxy(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    @Override
    public int reserveItem(int quantity) {
        return inventoryItem.reserveItem(quantity);
    }

    @Override
    public int releaseItem(int quantity) {
        return inventoryItem.releaseItem(quantity);
    }

    @Override
    public String getSku() {
        return inventoryItem.getSku();
    }

    @Override
    public String getName() {
        return inventoryItem.getName();
    }

    @Override
    public String getManufacturer() {
        return inventoryItem.getManufacturer();
    }

    @Override
    public Category getCategory() {
        return inventoryItem.getCategory();
    }

    @Override
    public double getPrice() {
        return inventoryItem.getPrice();
    }

    @Override
    public int getQtyTotal() {
        return inventoryItem.getQtyTotal();
    }

    @Override
    public int getQtyReserved() {
        return inventoryItem.getQtyReserved();
    }
}
