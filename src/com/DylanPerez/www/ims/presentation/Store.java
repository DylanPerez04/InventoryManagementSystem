package com.DylanPerez.www.ims.presentation;

import com.DylanPerez.www.ims.application.itemtype.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.application.util.Category;
import com.DylanPerez.www.ims.presentation.util.Cart;
import com.DylanPerez.www.ims.service.analytics.Analytics;
import com.DylanPerez.www.ims.service.analytics.CustomerAnalytics;
import com.DylanPerez.www.ims.service.analytics.SaleAnalytics;
import com.DylanPerez.www.ims.service.simulate.Simulatable;
import com.DylanPerez.www.ims.service.simulate.util.Averager;
import com.DylanPerez.www.ims.service.simulate.util.BoolSource;

import java.util.*;


public class Store implements Simulatable {

    private Map<String, InventoryItem> inventory; // HashMap<SKU string, InventoryItem>
    private Map<Cart.CartType, Map<String, Cart>> carts; // HashMap<type, HashMap<cartId, cart>>

    private Map<Category, Map<String, String>> aisleInventory; //EnumMap<Category, TreeMap<Product name, Product SKU>>

    private Analytics saleAnalytics, customerAnalytics;

    public Store(String initData, int physicalCartCount) {
        inventory = new HashMap<>();
        stockInventory(initData);
        aisleInventory = new EnumMap<>(Category.class);
        for(Category c : Category.values())
            aisleInventory.put(c, new TreeMap<>());
        stockAisleInventory();

        carts = new EnumMap<>(Cart.CartType.class);
        for(Cart.CartType t : Cart.CartType.values())
            carts.put(t, new HashMap<>());

        for(int i = 0; i < physicalCartCount; i++) {
            String cartId = "P" + i;
            carts.get(Cart.CartType.PHYSICAL).put(cartId, new Cart(cartId, Cart.CartType.PHYSICAL, this));
        }

        saleAnalytics = new SaleAnalytics(inventory.keySet());
        customerAnalytics = new CustomerAnalytics();
    }

    private void stockInventory(String initData) {
        stockInventory(initData, true);
    }

    private void stockInventory(String initData, boolean orderInventory) {
        final boolean debug = false;

        Scanner scanner = new Scanner(initData);
        while(scanner.hasNextLine()) {
            // name-manufacturer-category-price-qtylow-qtyreorder-[isForSale]
            String[] line = scanner.nextLine().split("/");
            InventoryItem item;
            // Custom deserialization
            if(line.length == 6)
                item = new InventoryItem(new Product(line[0], line[1], Category.valueOf(line[2])),
                    Double.parseDouble(line[3]), Integer.parseInt(line[4]), Integer.parseInt(line[5]));
            else
                item  = new InventoryItem(new Product(line[0], line[1], Category.valueOf(line[2])),
                        Double.parseDouble(line[3]), Integer.parseInt(line[4]), Integer.parseInt(line[5]), Boolean.parseBoolean(line[6]));

            if(orderInventory) item.placeInventoryOrder();

            inventory.put(item.getSku(), item);
        }

        if(debug) {
            listInventory();
        }

    }

    private void stockAisleInventory() {
        final boolean debug = false;
        inventory.forEach((k, v) -> {
            Category c = v.getProduct().getCategory();
            var map = aisleInventory.get(c);
            if(v.isForSale()) map.put(v.getProduct().getName(), k);
            aisleInventory.putIfAbsent(c, map);
        });

        if(debug) {
            System.out.println("Store#stockAisleInventory() : ------------------------ \n");
            aisleInventory.forEach((k, v) -> {
                if (k != Category.UNSOUGHT) {
                    System.out.println("[" + k + "] =================");
                    aisleInventory.get(k).forEach((name, sku) -> {
                        System.out.println("$" + inventory.get(sku).getSalesPrice() + " | " + inventory.get(sku).getProduct().getName() + " by " + inventory.get(sku).getProduct().getManufacturer());
                    });
                }
            });
            System.out.println("\n-----------------------------------------------------");
        }
    }

    public boolean checkOutCart(Cart cart) {
        final boolean debug = false;
        if(cart.isEmpty() || !cart.isWithdrawn()) return false;
        double total = 0;
        for(var entry : cart.getProducts().entrySet())
            total += searchItem(entry.getKey()).sellItem(entry.getValue());
        cart.printSalesSlip();
        System.out.println("Total = " + total);

        if(debug) {
            System.out.println("Store#checkOutCart() :");
            listInventory();
        }

        return true;
    }


    public InventoryItem searchItem(String sku) {
        return inventory.get(sku);
    }

    public String searchProduct(String name) {
        for (Category category : aisleInventory.keySet()) {
            var map = aisleInventory.get(category);
            String sku = map.get(name);
            if (sku != null) return sku;
        }
        return null;
    }

    public void abandonCarts() {
        carts.forEach((cartType, cartMap) -> {
            cartMap.forEach((cartId, cart) -> {

            });
        });
    }

    public Cart withdrawCart(Cart.CartType cartType) {
        if(cartType == Cart.CartType.PHYSICAL) {
            var storeCarts = carts.get(cartType).values();
            for(Cart c : storeCarts)
                if(!c.isWithdrawn()) {
                    c.setWithdrawn(true);
                    return c;
                }
            return null;
        } // else cartType == Cart.CartType.VIRTUAL
        String virtualCartId = "V" + carts.get(cartType).size();
        carts.get(cartType).put(virtualCartId, new Cart(virtualCartId, cartType, this));
        return carts.get(cartType).get(virtualCartId);
    }

    public boolean depositCart(Cart cart) {
        if(cart.isEmpty()) {
            carts.get(cart.getType()).get(cart.getId()).setWithdrawn(false);
            return true;
        }
        return false;
    }

    public void listInventory() {
        System.out.println("Current Store Inventory ------------------------ \n");
        inventory.forEach((k, v) -> {
            System.out.println(v);
        });
        System.out.println("\n-----------------------------------------------------");
    }

    public void listProductsByCategory() {
        listProductsByCategory(Cart.CartType.PHYSICAL);
    }

    public void listProductsByCategory(Cart.CartType type) {

        if(type.equals(Cart.CartType.PHYSICAL)) {
            System.out.println("---------- Aisles -----------------------------\n");
            aisleInventory.forEach((k, v) -> {
                if (k != Category.UNSOUGHT) {
                    System.out.println("[" + k + "] =================");
                    aisleInventory.get(k).forEach((name, sku) ->
                        System.out.println("$" + inventory.get(sku).getSalesPrice() + " | " + inventory.get(sku).getProduct().getName() + " by " + inventory.get(sku).getProduct().getManufacturer())
                    );
                }
            });
            System.out.println("\n-----------------------------------------------");
            return;
        }

        List<InventoryItem> items = new ArrayList<>(inventory.values());
        items.sort(Comparator.comparing(InventoryItem::getProduct));
        System.out.println("---------- Current Selection -----------------------------\n");
        items.forEach(item -> {
            if(item.isForSale()) System.out.println("$" + item.getSalesPrice() + " | " + item.getProduct().getName() + " by " + item.getProduct().getManufacturer());
        });
        System.out.println("\n-----------------------------------------------");
    }

    // TODO: Finish simulation

    @Override
    public boolean simulate(int duration, double p) {

        Averager averager = new Averager();
        BoolSource arrivalSource = new BoolSource(p),
                typeSource = new BoolSource(.183), // https://www.statista.com/statistics/1113287/sales-share-of-target-us-by-channel/
                physicalLeaveSource = new BoolSource(.8), // https://linkretail.com/why-are-80-of-visitors-to-a-store-not-buying-anything/
                virtualLeaveSource = new BoolSource(.7); // https://optinmo  nster.com/online-shopping-statistics/#Abandoned

        Map<Integer, Integer> customers = new HashMap<>(); // timestamp, customer id

        int customerID = 1;

        for(int currentSecond = 1; currentSecond < duration; currentSecond++) {

            if(arrivalSource.query()) {
                customers.put(currentSecond, customerID++);
                Cart.CartType cartType = (typeSource.query() ? Cart.CartType.VIRTUAL : Cart.CartType.PHYSICAL);

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
     */

}
