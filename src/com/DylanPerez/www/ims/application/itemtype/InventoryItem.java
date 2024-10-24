package com.DylanPerez.www.ims.application.itemtype;

public class InventoryItem {

    private final Product product;
    private double salesPrice;

    private int qtyTotal;
    private int qtyReserved;
    private int qtyReorder;
    private int qtyLow;

    public InventoryItem(Product product, double salesPrice) {
        this(product, salesPrice, 0, 0);
    }

    public InventoryItem(Product product, double salesPrice, int qtyLow, int qtyReorder) {
        this(product, salesPrice, 0, qtyLow, qtyReorder);
    }

    public InventoryItem(Product product, double salesPrice, int qtyTotal, int qtyLow, int qtyReorder) {
        this.product = product;
        this.salesPrice = salesPrice;
        this.qtyTotal = qtyTotal;
        this.qtyReserved = 0;
        this.qtyLow = qtyLow;
        this.qtyReorder = qtyReorder;
    }

    public String getSku() {
        return product.getSku();
    }

    public double getSalesPrice() {
        return salesPrice;
    }

    public boolean setSalesPrice(double salesPrice) {
        if(salesPrice < 0) return false;
        this.salesPrice = salesPrice;
        return true;
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

    public boolean reserveItem(int quantity) {
        if(quantity <= 0) return false;

        if(qtyTotal - qtyReserved > 0) {
            if(quantity >= qtyTotal - qtyReserved) {
                qtyReserved = qtyTotal;
            } else {
                qtyReserved += quantity;
            }
            return true;
        }
        return false;
    }

    public boolean releaseItem(int quantity) {
        if(quantity <= 0 || qtyReserved == 0) return false;

        if(qtyReserved - quantity <= 0) {
            qtyReserved = 0;
        } else {
            qtyReserved -= quantity;
        }

        return true;
    }


    public boolean sellItem(int quantity) {
        if(quantity <= 0 || qtyReserved == 0) return false;

        if(qtyReserved - quantity <= 0) {
            qtyTotal -= qtyReserved;
            qtyReserved = 0;
        } else {
            qtyReserved -= quantity;
            qtyTotal -= quantity;
        }
        return true;
    }

    public boolean placeInventoryOrder() {
        if(qtyTotal > qtyReorder) return false;

        qtyTotal += qtyReorder;
        return true;
    }

}
