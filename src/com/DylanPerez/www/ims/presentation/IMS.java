package com.DylanPerez.www.ims.presentation;

import com.DylanPerez.www.ims.application.inventory.Inventory;
import com.DylanPerez.www.ims.application.inventory.proxy.InventoryProxy;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemAccessor;
import com.DylanPerez.www.ims.presentation.util.Cart;
import com.DylanPerez.www.ims.service.simulate.Simulatable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * IMS is the base class for all Inventory Management System queries used by a client to facilitate
 * inventory stock and sales analytics for all in-store and online transactions with their business.
 * An IMS object encapsulates the means by which a client can add, remove, and navigate through their
 * available--and unavailable--selection of products. In addition, the IMS object directly
 * handles client-to-customer transactions which interact with the stock of the client's available
 * selection, allowing the IMS object to provide analytical data regarding the client's sales.
 *
 * @author Dylan Perez-Hernandez
 * @version 1.0
 * @since 21.0.3
 */
public class IMS implements Simulatable, Runnable {

    // TODO : Make inventory final and have a method to update/merge Inventory with new database (.json)
    private final Inventory inventory; // HashMap<SKU string, InventoryItem>

    /**
     * A field that offers a means of providing a unique identifier to all
     * outgoing carts.
     */
    private static int cartID = 0;

    /**
     * A map that offers a means of quickly retrieving a reference to a
     * <code>Cart</code> that has been checked out.
     *
     * @see Cart
     */
    private Map<String, Cart> carts; // HashMap<type, HashMap<cartId, cart>>


    public IMS(File database, boolean autoRestock) throws IOException {
        inventory = new Inventory(database, autoRestock);
        carts = new HashMap<>();
    }

    private InventoryItemAccessor search(String sku) {
        return inventory.get(sku);
    }

    private void outputAdminView(InventoryItem.Comparator comparator) {
        inventory.updateAdminView(comparator);
    }

    private void outputAdminView(List<InventoryItem.Comparator> comparator) {
        inventory.updateAdminView(comparator);
    }

    private void restockInventory() {
        inventory.restock();
    }
    
    private boolean checkOutCart(Cart cart) {
        return inventory.checkOutCart(cart);
    }

    private Cart withdrawCart() {
        String onlineCartId = "C" + String.format("%3d", cartID++).replace(' ', '0');
        carts.put(onlineCartId, new Cart(onlineCartId, new InventoryProxy(inventory)));
        return carts.get(onlineCartId);
    }

    private boolean depositCart(Cart cart) {
        if(cart.isEmpty()) {
            carts.get(cart.getId()).setWithdrawn(false);
            return true;
        }
        return false;
    }

    public void abandonCarts() {

    }

    // TODO: Finish simulation

    @Override
    public boolean simulate(int duration, double p) {
//
//        Averager averager = new Averager();
//        BoolSource arrivalSource = new BoolSource(p),
//                typeSource = new BoolSource(.183), // https://www.statista.com/statistics/1113287/sales-share-of-target-us-by-channel/
//                physicalLeaveSource = new BoolSource(.8), // https://linkretail.com/why-are-80-of-visitors-to-a-store-not-buying-anything/
//                virtualLeaveSource = new BoolSource(.7); // https://optinmo  nster.com/online-shopping-statistics/#Abandoned
//
//        Map<Integer, Integer> customers = new HashMap<>(); // timestamp, customer id
//
//        int customerID = 1;
//
//        for(int currentSecond = 1; currentSecond < duration; currentSecond++) {
//
//            if(arrivalSource.query()) {
//                customers.put(currentSecond, customerID++);
//                Cart.CartType cartType = (typeSource.query() ? Cart.CartType.VIRTUAL : Cart.CartType.PHYSICAL);
//
//                /* TODO: Have customer go shopping, and have a BoolSource for probability of them leaving
//                    (this will be different depending on if they are an "physical" or "virtual" shopper)
//                */
//                carts.forEach((k, v) -> {
//                    if(k == Cart.CartType.PHYSICAL) {
//
//                        /*
//                         We will have an 80% chance for a customer to add something
//                         to their carts, and a 20% chance they remove something from their
//                         carts (made-up statistic, just for simulation)
//                         */
//                        if(Math.random() < .8) {
//                        } else {
//                        }
//
//                    } else if(k == Cart.CartType.VIRTUAL) {
//
//                    }
//                });
//
//            }
//
//        }

        return true;
    }


    private void customizeOutput(Scanner scanner) {
        final String[] options = {
                "Customize Inventory Output",
                "Search For Item",
                "Update Inventory",
                "View Analytics",
                "Exit"
        };

        String buffer;
        List<InventoryItem.Comparator> comparators = new ArrayList<>();
        boolean askedInitial = false;

        while(true) { ///< Bad practice but IntelliJ compiler advised this over redundant boolean variable
            if(!askedInitial)
                System.out.print("[?] Sort by (enter Exit to go back) : ");
            else
                System.out.print("[?] And by (enter Exit to go back) : ");
            buffer = scanner.nextLine();

            if(buffer.equalsIgnoreCase("exit")) {
                inventory.updateAdminView(comparators);
                break;
            }

            try {
                comparators.add(InventoryItem.Comparator.valueOf(buffer.toUpperCase()));
            } catch(IllegalArgumentException e) {
                System.out.println("[!] Unknown InventoryItem field: " + buffer);
            }
            askedInitial = true;
        }
    }

    private void searchForItem(Scanner scanner) {

    }

    private void updateInventory(Scanner scanner) {

    }

    private void viewAnalytics(Scanner scanner) {

    }

    @Override
    public void run() {
        final String[] options = {
                "Customize Inventory Output",
                "Search For Item",
                "Update Inventory",
                "View Analytics",
                "Exit"
        };

        Scanner scanner = new Scanner(System.in);
        String buffer;
        boolean endProgram = false;

        while(!endProgram) {
            inventory.outputAdminView();
            System.out.println("-------------------------");
            for(int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }
            System.out.println("-------------------------");

            System.out.print("> ");
            buffer = scanner.nextLine();

            switch (Integer.parseInt(buffer)) {
                case 1 -> customizeOutput(scanner);
                case 2 -> searchForItem(scanner);
                case 3 -> updateInventory(scanner);
                case 4 -> viewAnalytics(scanner);
                default -> endProgram = true;
            }
        }
    }

    /*
     * manageStoreCarts();
     */

}