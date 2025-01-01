package com.DylanPerez.www.ims.application.itemtype.inventory_item;

import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;
import com.DylanPerez.www.ims.application.util.Category;
import com.DylanPerez.www.ims.presentation.util.Cart;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;

@JsonPropertyOrder({"sku", "name", "manufacturer", "category", "cost", "price", "qtyTotal", "qtyReserved", "qtyLow", "qtyReorder", "forSale", "autoRestock"})
public class InventoryItem extends Product implements InventoryItemUpdater {

    private static int fooID = 0;

    /**
     * The quantity of this <code>InventoryItem</code> currently in the
     * client's stock.
     */
    private int qtyTotal;

    /**
     * The quantity of this <code>InventoryItem</code> currently reserved
     * within online carts.
     *
     * @see Cart
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
        this(toCopy.getSku(), toCopy.getName(), toCopy.getManufacturer(), toCopy.getCategory(),
                toCopy.getCost(), toCopy.getPrice(), toCopy.qtyTotal, toCopy.qtyReserved, toCopy.qtyLow, toCopy.qtyReorder,
                toCopy.forSale, toCopy.autoRestock);
    }

    public InventoryItem(String name, String manufacturer, Category category,
                          double cost, double price) {
        this(null, name, manufacturer, category, cost, price,0, 0, 0, 0, false, false);
    }

    public InventoryItem(String name, String manufacturer, Category category,
                         double cost, double price, int qtyTotal, int qtyLow, int qtyReorder) {
        this(null, name, manufacturer, category, cost, price, qtyTotal, 0, qtyLow, qtyReorder, true, false);
    }

    @JsonCreator
    public InventoryItem(
            @JsonProperty("sku") String sku,
            @JsonProperty("name") String name,
            @JsonProperty("manufacturer") String manufacturer,
            @JsonProperty("category") Category category,
            @JsonProperty("cost") double cost,
            @JsonProperty("price") double price,
            @JsonProperty("qtyTotal") int qtyTotal,
            @JsonProperty("qtyReserved") int qtyReserved,
            @JsonProperty("qtyLow") int qtyLow,
            @JsonProperty("qtyReorder") int qtyReorder,
            @JsonProperty("forSale") boolean forSale,
            @JsonProperty("autoRestock") boolean autoRestock) {
        super(sku, name, manufacturer, category, cost, price);
        this.qtyTotal = qtyTotal;
        this.qtyReserved = 0;
        this.qtyLow = qtyLow;
        this.qtyReorder = qtyReorder;
        this.forSale = forSale;
        this.autoRestock = autoRestock;
    }

    public int write(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        TreeNode head = mapper.readTree(file);

        ArrayNode items = (ArrayNode) head.get("items");
        if(items == null) throw new RuntimeException("Missing items Json Array in " + file.getName() + ".");
        int recno = items.size();
        items.addPOJO(this);

        mapper.writeValue(file, head);
        return recno;
    }

    public void read(File file, int recno) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        TreeNode head = mapper.readTree(file);

        ArrayNode items = (ArrayNode) head.get("items");
        if(items.isMissingNode()) throw new RuntimeException("Missing items Json Array in " + file.getName() + ".");
        InventoryItem item = mapper.convertValue(items.get(recno), InventoryItem.class);

        this.setName(item.getName());
        this.setManufacturer(item.getManufacturer());
        this.setCategory(item.getCategory());
        this.setPrice(item.getPrice());
        this.setCost(item.getCost());
        this.restoreSku();

        this.setQtyTotal(item.getQtyTotal());
        this.setQtyReserved(item.getQtyReserved());
        this.setQtyLow(item.getQtyLow());
        this.setQtyReorder(item.getQtyReorder());
        this.setForSale(item.isForSale());
        this.setAutoRestock(item.hasAutoRestocks());

        if(this.hashCode() != item.hashCode()) {
            throw new RuntimeException("this.hashCode() = " + this.hashCode() + "\nitem.hashCode() = " + item.hashCode());
        }
    }

    public static InventoryItem foo() {
        fooID++;
        return new InventoryItem(null, "fooItem" + fooID, "fooManufacturer" + fooID,
                Category.values()[(int) (Math.random() * Category.values().length)],
                Math.random() * 100 + 1, Math.random() * 200 + 1,
                0, 0, 0, 0,
                false, false);
    }

//    public static Comparator<InventoryItem> getFieldComparator(String fieldName) {
//        if(fieldName == null || fieldName.isEmpty()) return null;
//
//        Comparator<InventoryItem> comparator = switch(fieldName) {
//            case "sku" -> java.util.Comparator.comparing(InventoryItem::getSku);
//            case "name" -> java.util.Comparator.comparing(InventoryItem::getName);
//            case "manufacturer" -> java.util.Comparator.comparing(InventoryItem::getManufacturer);
//            case "category" -> java.util.Comparator.comparing(InventoryItem::getCategory);
//            case "cost" -> java.util.Comparator.comparing(InventoryItem::getCost);
//            case "price" -> java.util.Comparator.comparing(InventoryItem::getPrice);
//            case "qtyTotal" ->java.util.Comparator.comparing(InventoryItem::getQtyTotal);
//            case "qtyReserved" -> java.util.Comparator.comparing(InventoryItem::getQtyReserved);
//            case "qtyLow" -> java.util.Comparator.comparing(InventoryItem::getQtyLow);
//            case "qtyReorder" -> java.util.Comparator.comparing(InventoryItem::getQtyReorder);
//            case "autoRestock" -> java.util.Comparator.comparing(InventoryItem::hasAutomaticRestocks);
//            case "forSale" -> java.util.Comparator.comparing(InventoryItem::isForSale);
//            default -> null;
//        };
//
//        return comparator;
//    }

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

    public int placeInventoryOrder() {
        return placeInventoryOrder(qtyReorder);
    }

    public int placeInventoryOrder(int quantity) {
        if(quantity < 0) return 0;

        qtyTotal += quantity;
        return quantity;
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

    @JsonGetter("autoRestock")
    public boolean hasAutoRestocks() {
        return autoRestock;
    }

    public void setAutoRestock(boolean autoRestock) {
        this.autoRestock = autoRestock;
    }

    @Override
    public String toString() {
        String[] data = {"[" + getSku() + "]", getName(), getManufacturer(), getCategory().toString(),
                "$" + getCost(), "$" + getPrice(),  Integer.toString(qtyTotal), Integer.toString(qtyReserved),
                Integer.toString(qtyLow),Integer.toString(qtyReorder), Boolean.toString(forSale), Boolean.toString(autoRestock)};
        StringBuilder sb = new StringBuilder();
        for(String cell : data)
            sb.append(String.format("%18s", cell)).append(" |");

        return sb.toString();
    }

}
