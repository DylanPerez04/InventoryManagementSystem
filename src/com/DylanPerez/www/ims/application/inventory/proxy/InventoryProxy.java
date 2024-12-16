package com.DylanPerez.www.ims.application.inventory.proxy;

import com.DylanPerez.www.ims.application.inventory.Inventory;
import com.DylanPerez.www.ims.application.inventory.interfaces.inventory.InventoryAccessor;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;

public class InventoryProxy implements InventoryAccessor {

    private final InventoryAccessor inventory;

    public InventoryProxy(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * If present, retrieves a reference to an <code>InventoryItemAccessor</code> view
     * of the <code>InventoryItem</code> within this <code>Inventory</code>
     * that has an equal <code>sku</code>; null otherwise.
     * <p>
     * The <code>InventoryItemAccessor</code> is backed by the <code>InventoryItem</code>,
     * so changes made using the interface methods are reflected on the actual <code>InventoryItem</code> associated
     * with the <code>sku</code>.
     *
     * @param sku The <code>sku</code> of the <code>InventoryItem</code> to retrieve.
     * @return A proxy of the <code>InventoryItem</code> found; if there is no non-null
     * value mapped to the key <code>sku</code>, returns null.
     */
    @Override
    public InventoryItemUpdater get(String sku) {
        return inventory.get(sku);
    }

    @Override
    public boolean contains(String sku) {
        return inventory.contains(sku);
    }
}
