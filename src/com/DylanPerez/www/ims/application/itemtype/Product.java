package com.DylanPerez.www.ims.application.itemtype;

// TODO: Have products be pulled and listed from a database (file) to maintain changes.

import com.DylanPerez.www.ims.application.util.Category;

public class Product {

    /**
     * 7-10 characters long. SKU used specifically for sale analytics.
     */
    private String sku;

    private String name;
    private String manufacturer;
    private Category category;

    public Product(String sku, String name, String manufacturer, Category category) {
        this.sku = sku;
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;
    }

    /**
     * Updates the sku of the product.
     * #param newSku must be between 7-10 characters long, and contain no special characters,
     * or zeroes, all to maintain compatibility with third-party software.
     *
     * @param newSku A 7-10 character long sku.
     * @returns Whether the sku has been successfully updated.
     */
    public boolean updateSku(String newSku) {
        if(newSku.length() < 7 || newSku.length() > 10) return false;

        final char[] invalid_chars = {'0', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=', '\\', '/', '.', ',', '`', '~', '|', '?', '>', '<', ':', ';', '}', '{', '[', ']', '\'', '\"'};

        for(char c : invalid_chars)
            if(newSku.contains(String.valueOf(c))) return false;

        // TODO: check if there is already a product with the same sku (need file/database)

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
    public boolean equals(Object obj) {
        if(super.equals(obj)) return true;
        return this.sku.equals(((Product) obj).sku);
    }
}