package com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces;


import com.DylanPerez.www.ims.application.util.Category;

/**
 * <p>
 *     <code>InventoryItemAccessor</code> is an interface utilized to define the behavior
 *     of all <code>InventoryItem</code> proxy objects, which exist in an effort to
 *     eliminate direct access to all of an <code>InventoryItem</code>'s public methods.
 * </p>
 *
 * <p>
 *     This disconnect of direct access is necessary to prevent objects outside the
 *     <code>IMS</code> class from mutating an <code>InventoryItem</code> via
 *     its setter methods.
 * </p>
 *
 * <p>
 *     What prompted the necessity of this implementation was the requirement for
 *     <code>Cart</code> objects to be able to reserve and release <code>InventoryItem</code>s
 *     using the public <code>reserveItem</code> and <code>releaseItem</code> methods, respectively,
 *     while simultaneously not having access to other public, operations-critical methods.
 * </p>
 *
 *  @author Dylan Perez-Hernandez
 *  @version 1.0
 *  @since 21.0.3
 */
public interface InventoryItemAccessor {

    String getSku();

    String getName();

    String getManufacturer();

    Category getCategory();

    double getPrice();

    int getQtyTotal();

    int getQtyReserved();

}
