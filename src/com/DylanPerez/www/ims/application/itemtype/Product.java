package com.DylanPerez.www.ims.application.itemtype;

// TODO: Have products be pulled and listed from a database (file) to maintain changes.

import com.DylanPerez.www.ims.application.util.Category;

import java.util.Map;

public class Product implements Comparable<Product> {

    private static final char[] invalid_chars = {'0', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=', '\\', '/', '.', ',', '`', '~', '|', '?', '>', '<', ':', ';', '}', '{', '[', ']', '\'', '\"'};

    /**
     * 7-10 characters long. SKU used specifically for sale analytics.
     */
    private String sku;

    private String name;
    private String manufacturer;
    private Category category;

    public Product(String name, String manufacturer, Category category) {
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;

        this.sku = generateSku();
    }

    public Product(String sku, String name, String manufacturer, Category category) {
        this.sku = sku;
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;
    }

    private String generateSku() {

        StringBuilder sku = new StringBuilder();
        sku.append(String.format("%02X", category.ordinal()).replaceAll("0", "Z"));

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
     * Updates the sku of the product.
     * #param newSku must be between 7-10 characters long, and contain no special characters,
     * or zeroes, all to maintain compatibility with third-party software.
     *
     * @param newSku A 7-10 character long sku.
     * @returns Whether the sku has been successfully updated.
     */
    public boolean updateSku(String newSku, Map<String, InventoryItem> inventory) {
        if(newSku.length() < 7 || newSku.length() > 10) return false;

        for(char c : invalid_chars)
            if(newSku.contains(String.valueOf(c))) return false;

        if(inventory.get(newSku) == null) return false;

        this.sku = newSku;
        return true;
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

    @Override
    public String toString() {
        return getName() + " | " + getManufacturer() + " | " + getCategory();
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) return true;
        return this.sku.equals(((Product) obj).sku);
    }

    @Override
    public int compareTo(Product o) {
        if(name.equals(o.getName())) {
            if(manufacturer.equals(o.getManufacturer())) {
                if(category == o.getCategory()) return 0;
                return Integer.compare(category.ordinal(), o.getCategory().ordinal());
            }
            return manufacturer.compareTo(o.getManufacturer());
        }
        return name.toUpperCase().compareTo(o.getName().toUpperCase());
    }
}