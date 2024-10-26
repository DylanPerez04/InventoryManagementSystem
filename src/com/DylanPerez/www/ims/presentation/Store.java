package com.DylanPerez.www.ims.presentation;

import com.DylanPerez.www.ims.application.itemtype.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.application.util.Category;
import com.DylanPerez.www.ims.presentation.util.Cart;
import com.DylanPerez.www.ims.service.simulate.Simulatable;
import com.DylanPerez.www.ims.service.simulate.util.Averager;
import com.DylanPerez.www.ims.service.simulate.util.BoolSource;

import java.util.*;


public class Store implements Simulatable {

    private Map<String, InventoryItem> inventory; // HashMap<SKU string, InventoryItem>
    private Map<Cart.CartType, Cart> carts; // HashMap
    private Set<Product> aisleInventory; // TreeSet to accommodate search for both online and in store effectively

    public Store(String initData) {
        inventory = new HashMap<>();
        carts = new HashMap<>();
        aisleInventory = new TreeSet<>(Comparator.comparing(Product::getCategory)
                .thenComparing(Product::getManufacturer)
                .thenComparing(Product::getName));
        stockInventory(initData);
    }

    private void stockInventory(String initData) {
        final boolean debug = true;

        Scanner scanner = new Scanner(initData);
        while(scanner.hasNextLine()) {
            // sku-name-manufacturer-category-price-qtylow-qtyreorder
            String[] line = scanner.nextLine().split("-");
            inventory.put(line[0], new InventoryItem(
                    new Product(line[0], line[1], line[2], Category.valueOf(line[3])),
                    Double.parseDouble(line[4]), Integer.parseInt(line[5]), Integer.parseInt(line[6])));
        }

        if(debug) {
            System.out.println("Store#stockInventory() : ------------------------ \n");
            inventory.forEach((k, v) -> {
                System.out.println("[" + k + "] " + v);
            });
            System.out.println("\n-----------------------------------------------------");
        }

    }

    // TODO: Finish simulation

    @Override
    public boolean simulate(int duration, double p) {

        Averager averager = new Averager();
        BoolSource arrivalSource = new BoolSource(p),
                typeSource = new BoolSource(.183), // https://www.statista.com/statistics/1113287/sales-share-of-target-us-by-channel/
                physicalLeaveSource = new BoolSource(.8), // https://linkretail.com/why-are-80-of-visitors-to-a-store-not-buying-anything/
                virtualLeaveSource = new BoolSource(.7); // https://optinmonster.com/online-shopping-statistics/#Abandoned

        Map<Integer, Integer> customers = new HashMap<>(); // timestamp, customer id

        int customerID = 1;

        for(int currentSecond = 1; currentSecond < duration; currentSecond++) {

            if(arrivalSource.query()) {
                customers.put(currentSecond, customerID++);
                Cart.CartType cartType = (typeSource.query() ? Cart.CartType.VIRTUAL : Cart.CartType.PHYSICAL);
                carts.put(cartType, new Cart(Integer.toString(customerID), cartType));
                /* TODO: Have customer go shopping, and have a BoolSource for probability of them leaving
                    (this will be different depending on if they are an "physical" or "virtual" shopper)
                */
                carts.forEach((k, v) -> {
                    if(k == Cart.CartType.PHYSICAL) {

                        /*
                         We will have an 80% chance for a customer to add something
                         to their carts, and a 20% chance they remove something from their
                         carts (made-up statistic, just for simulation)
                         */
                        if(Math.random() < .8) {
                        } else {
                        }

                    } else if(k == Cart.CartType.VIRTUAL) {

                    }
                });

            }

        }

        return true;
    }

    /*
     * manageStoreCarts();
     * checkOutCart();
     * abandonCarts();
     * listProductsByCategory();
     */

}
