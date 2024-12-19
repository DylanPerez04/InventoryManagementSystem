package com.DylanPerez.www.ims.application.itemtype.inventory_item.testing;

import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;
import com.DylanPerez.www.ims.application.util.Category;

public class InventoryItem extends Product implements InventoryItemUpdater {

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

    public InventoryItem(String name, String manufacturer, Category category, double cost, double price) {
        super(name, manufacturer, category, cost, price);
    }

    @Override
    public int getQtyTotal() {
        return 0;
    }

    @Override
    public int getQtyReserved() {
        return 0;
    }

    @Override
    public int reserveItem(int quantity) {
        return 0;
    }

    @Override
    public int releaseItem(int quantity) {
        return 0;
    }
}
