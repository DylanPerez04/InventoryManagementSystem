package com.DylanPerez.www.ims.application.itemtype;

// TODO: Have products be pulled and listed from a database (file) to maintain changes.

import com.DylanPerez.www.ims.application.itemtype.inventory_item.InventoryItem;
import com.DylanPerez.www.ims.application.util.Category;

import java.util.Map;

public abstract class Product {

    // TODO : Add setter methods; check if there is already a product with a particular sku; update the way I make SKU

    private static final char[] invalid_chars = {'0', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=', '\\', '/', '.', ',', '`', '~', '|', '?', '>', '<', ':', ';', '}', '{', '[', ']', '\'', '\"'};

    /**
     * 7-10 characters long. SKU used specifically for sale analytics.
     */
    private String sku;

    private String name;
    private String manufacturer;
    private Category category;

    /**
     * The price the <code>Product</code> was bought for by the client from the <code>Product</code>'s
     * wholesaler.
     */
    private double cost;

    /**
     * The price the <code>Product</code>> is selling for on the client's business' listing.
     */
    private double price;

    public Product(String name, String manufacturer, Category category, double cost, double price) {
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;

        this.cost = cost;
        this.price = price;

        this.sku = generateSku();
    }

    private String generateSku() {

        StringBuilder sku = new StringBuilder();
        sku.append(String.format("%02X", category.ordinal() + 1).replaceAll("0", "Z"));

        String noSpaceMan = manufacturer.replaceAll(" ", "");
        if(noSpaceMan.length() >= 3) {
            sku.append(noSpaceMan.substring(0, 3).toUpperCase());
        } else {
            sku.append(noSpaceMan.toUpperCase());
            for(int i = 0; i < 3 - noSpaceMan.length(); i++)
                sku.append("Z");
        }

        String noSpaceName = name.replaceAll(" ", "");

        if(noSpaceName.length() >= 5) {
            sku.append(noSpaceName.substring(0, 3).toUpperCase());
            sku.append(noSpaceName.substring(noSpaceName.length() - 2).toUpperCase());
        } else {
            sku.append(noSpaceName.toUpperCase());
            for(int i = 0; i < 5 - noSpaceName.length(); i++)
                sku.append("Z");
        }
        return sku.toString();
    }

    /**
     * Updates the <code>sku</code>> of this <code>Product</code>.
     * <code>newSku</code> must be between 7-10 characters long, and contain no special characters,
     * or zeroes, all to maintain compatibility with third-party software.
     *
     * @param newSku A 7-10 character long sku.
     * @return Whether the sku has been successfully updated.
     */
    public boolean updateSku(String newSku, Map<String, InventoryItem> inventory) {
        if(newSku.length() < 7 || newSku.length() > 10) return false;

        for(char c : invalid_chars)
            if(newSku.contains(String.valueOf(c))) return false;

        if(inventory.get(newSku) == null) return false;

        this.sku = newSku;
        return true;
    }

    public void restoreSku() {
        this.sku = generateSku();
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Category getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    protected boolean setPrice(double price) {
        if(price < 0) return false;
        this.price = price;
        return true;
    }

    protected double getCost() {
        return cost;
    }

    protected boolean setCost(double cost) {
        if(cost < 0) return false;
        this.cost = cost;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) return true;
        return this.sku.equals(((Product) obj).sku);
    }
}