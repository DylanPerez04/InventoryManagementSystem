package com.DylanPerez.www.ims.application.itemtype.inventory_item;

import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;
import com.DylanPerez.www.ims.application.util.Category;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Supplier;

public class InventoryItem extends Product implements InventoryItemUpdater {
    
    public enum Comparator implements Supplier<java.util.Comparator<InventoryItem>> {
        SKU,
        NAME,
        MANUFACTURER,
        CATEGORY,
        COST,
        PRICE,
        QTY_TOTAL,
        QTY_RESERVED,
        QTY_LOW,
        QTY_REORDER,
        AUTO_RESTOCK,
        FOR_SALE;

        public java.util.Comparator<InventoryItem> get() {
            return switch (this) {
                case SKU -> java.util.Comparator.comparing(InventoryItem::getSku);
                case NAME -> java.util.Comparator.comparing(InventoryItem::getName);
                case MANUFACTURER -> java.util.Comparator.comparing(InventoryItem::getManufacturer);
                case CATEGORY -> java.util.Comparator.comparing(InventoryItem::getCategory);
                case COST -> java.util.Comparator.comparing(InventoryItem::getCost);
                case PRICE -> java.util.Comparator.comparing(InventoryItem::getPrice);
                case QTY_TOTAL ->java.util.Comparator.comparing(InventoryItem::getQtyTotal);
                case QTY_RESERVED -> java.util.Comparator.comparing(InventoryItem::getQtyReserved);
                case QTY_LOW -> java.util.Comparator.comparing(InventoryItem::getQtyLow);
                case QTY_REORDER -> java.util.Comparator.comparing(InventoryItem::getQtyReorder);
                case AUTO_RESTOCK -> java.util.Comparator.comparing(InventoryItem::hasAutomaticRestocks);
                case FOR_SALE -> java.util.Comparator.comparing(InventoryItem::isForSale);
            };
        }
    }

    /**
     * The quantity of this <code>InventoryItem</code> currently in the
     * client's stock.
     */
    private int qtyTotal;

    /**
     * The quantity of this <code>InventoryItem</code> currently reserved
     * within online carts.
     *
     * @see com.DylanPerez.www.ims.presentation.util.Cart
     */
    private int qtyReserved;

    /**
     * The quantity of this <code>InventoryItem</code> that indicates
     * when the client's stock of this item is in need of a reorder.
     */
    private int qtyLow;

    /**
     * The quantity of this <code>InventoryItem</code> to reorder in the
     * event that the client's stock of this item is equal to
     * or less than <code>qtyLow</code>.
     */
    private int qtyReorder;

    /**
     * An indicator to check whether a transaction with this <code>InventoryItem</code>
     * should go through; a means to quickly view items not currently
     * available to the client's customers.
     */
    private boolean forSale;

    /**
     *     A field for external calls of <code>placeInventoryOrder</code> to check
     *     to determine whether they are permitted to call the method.
     *     There are no formal checks to differentiate whether the caller
     *     of <code>placeInventoryOrder</code> is doing so autonomously,
     *     or manually.
     */
    private boolean autoRestock;

    public InventoryItem(InventoryItem toCopy) {
        this(toCopy.getName(), toCopy.getManufacturer(), toCopy.getCategory(),
                toCopy.getCost(), toCopy.getPrice(), toCopy.qtyTotal, toCopy.qtyLow, toCopy.qtyReorder,
                toCopy.forSale, toCopy.autoRestock);
    }

    public InventoryItem(String name, String manufacturer, Category category,
                          double cost, double price) {
        this(name, manufacturer, category, cost, price, 0, 0, 0, false, false);
    }

    public InventoryItem(String name, String manufacturer, Category category,
                         double cost, double price, int qtyTotal, int qtyLow, int qtyReorder) {
        this(name, manufacturer, category, cost, price, qtyTotal, qtyLow, qtyReorder, true, false);
    }

    @JsonCreator
    public InventoryItem(
            @JsonProperty("name") String name,
            @JsonProperty("manufacturer") String manufacturer,
            @JsonProperty("category") Category category,
            @JsonProperty("cost") double cost,
            @JsonProperty("price") double price,
            @JsonProperty("qtyTotal") int qtyTotal,
            @JsonProperty("qtyLow") int qtyLow,
            @JsonProperty("qtyReorder") int qtyReorder,
            @JsonProperty("forSale") boolean forSale,
            @JsonProperty("autoRestock") boolean autoRestock) {
        super(name, manufacturer, category, cost, price);
        this.qtyTotal = qtyTotal;
        this.qtyReserved = 0;
        this.qtyLow = qtyLow;
        this.qtyReorder = qtyReorder;
        this.forSale = forSale;
        this.autoRestock = autoRestock;
    }

    @Override
    public int reserveItem(int quantity) {
        if(quantity <= 0 || !isForSale()) return 0;
        /// Defines the quantity of this InventoryItem not yet reserved
        int unitsAvailable = qtyTotal - qtyReserved;

        if(unitsAvailable > 0) {
            if(quantity >= unitsAvailable) {
                qtyReserved = qtyTotal;
                return unitsAvailable;
            }

            qtyReserved += quantity;
            return quantity;
        }
        return 0;
    }

    @Override
    public int releaseItem(int quantity) {
        if(quantity <= 0) return 0;
        int priorQty = qtyReserved;

        if(quantity >= qtyReserved) {
            qtyReserved = 0;
            return priorQty;
        }

        qtyReserved -= quantity;
        return quantity;
    }


    public double sellItem(int quantity) {
        int qtySold = 0;
        if(quantity <= 0 || qtyReserved == 0) return -1;

        if(qtyReserved - quantity <= 0) {
            qtySold = qtyReserved;
            qtyTotal -= qtyReserved;
            qtyReserved = 0;
        } else {
            qtySold = quantity;
            qtyReserved -= quantity;
            qtyTotal -= quantity;
        }
        return qtySold * getPrice();
    }

    public boolean placeInventoryOrder() {
        if(qtyTotal > qtyLow) return false;

        qtyTotal += qtyReorder;
        return true;
    }

    public boolean setPrice(double price) {
        return super.setPrice(price);
    }

    public boolean setCost(double cost) {
        return super.setCost(cost);
    }

    @Override
    public int getQtyTotal() {
        return qtyTotal;
    }

    public void setQtyTotal(int qtyTotal) {
        this.qtyTotal = qtyTotal;
    }

    @Override
    public int getQtyReserved() {
        return qtyReserved;
    }

    public void setQtyReserved(int qtyReserved) {
        this.qtyReserved = qtyReserved;
    }

    public int getQtyReorder() {
        return qtyReorder;
    }

    public boolean setQtyReorder(int qtyReorder) {
        if(qtyReorder < 0) return false;
        this.qtyReorder = qtyReorder;
        return true;
    }

    public int getQtyLow() {
        return qtyLow;
    }

    public boolean setQtyLow(int qtyLow) {
        if(qtyLow < 0) return false;
        this.qtyLow = qtyLow;
        return true;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public boolean hasAutomaticRestocks() {
        return autoRestock;
    }

    public void setAutomaticallyRestock(boolean autoRestock) {
        this.autoRestock = autoRestock;
    }

    @Override
    public String toString() {
        String[] data = {"[" + getSku() + "]", getName(), getManufacturer(), getCategory().toString(),
                "$" + getCost(), "$" + getPrice(),  Integer.toString(qtyTotal), Integer.toString(qtyReserved),
                Integer.toString(qtyLow),Integer.toString(qtyReorder), Boolean.toString(autoRestock), Boolean.toString(forSale)};
        StringBuilder sb = new StringBuilder();
        for(String cell : data)
            sb.append(String.format("%18s", cell)).append(" |");

        return sb.toString();
    }

}
