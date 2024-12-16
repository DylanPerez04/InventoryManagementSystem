package com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces;

public interface InventoryItemUpdater extends InventoryItemAccessor {

    /**
     * Returns the actual quantity of this <code>InventoryItem</code> reserved, and not
     * <code>quantity</code> if <code>quantity</code> exceeds the
     * available units of this <code>InventoryItem</code>.
     *
     * @param quantity The number of this InventoryItem to attempt to reserve
     * @return The actual quantity reserved of this InventoryItem
     */
    int reserveItem(int quantity);

    /**
     * Returns the actual quantity of this <code>InventoryItem</code> released, and not
     * <code>quantity</code> if <code>quantity</code> exceeds the
     * total reserved units of this <code>InventoryItem</code>.
     *
     * @param quantity The number of this InventoryItem to attempt to release
     * @return The actual quantity released of this InventoryItem
     */
    int releaseItem(int quantity);

}
