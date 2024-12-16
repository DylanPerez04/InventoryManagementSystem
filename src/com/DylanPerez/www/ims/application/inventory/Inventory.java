package com.DylanPerez.www.ims.application.inventory;

import com.DylanPerez.www.ims.application.inventory.interfaces.analytics.Analytics;
import com.DylanPerez.www.ims.application.inventory.interfaces.inventory.InventoryAccessor;
import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.proxy.InventoryItemProxy;
import com.DylanPerez.www.ims.presentation.util.Cart;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Inventory implements InventoryAccessor {

    // TODO : Update outdated documentation

    private static class SaleAnalytics implements Analytics<String, Integer> {

        private final Map<Integer, Set<String>> salesCounts; // Map<# of sales, set of SKUs>

        public SaleAnalytics(Set<String> productSkus) {
            salesCounts = new TreeMap<>();
            productSkus.forEach(sku -> salesCounts.compute(0, (k, v) -> {
                if(v == null)
                    v = new HashSet<>();
                v.add(sku);
                return v;
            }));
        }

        @Override
        public Integer getData(String key) {
            for(Integer saleCount : salesCounts.keySet())
                if(salesCounts.get(saleCount).contains(key)) return saleCount;
            return null;
        }

        @Override
        public boolean addData(String key) {
            Integer saleCount = getData(key);

            if(saleCount == null) {
                salesCounts.get(0).add(key);
                return true;
            }

            salesCounts.get(saleCount).remove(key);
            salesCounts.compute(saleCount + 1, (k, v) -> {
                if(v == null)
                    v = new HashSet<>();
                v.add(key);
                return v;
            });

            return true;
        }

        @Override
        public boolean removeData(String key) {

            Integer saleCount = getData(key);
            if(saleCount == null || saleCount - 1 < 0) return false;

            salesCounts.get(saleCount).remove(key);
            salesCounts.compute(saleCount - 1, (k, v) -> {
                if(v == null) // Shouldn't ever be true
                    v = new HashSet<>();
                v.add(key);
                return v;
            });

            return true;
        }

        @Override
        public void generate() {

        }
    }

    /**
     * A map that takes a <code>Product</code> SKU represented as a <code>String</code>
     * as a key and returns a reference to the <code>InventoryItem</code> associated
     * with the SKU.
     *
     * @see Product#getSku()
     * @see InventoryItem
     */
    private final Map<String, InventoryItem> inventory;

    /**
     * A <code>Set</code> of all <code>InventoryItem</code>s within <code>inventory</code>
     * that are sorted by the Lexicographical order of each item's <code>sku</code>.
     *
     * This <code>Set</code> is specifically declared as a means to quickly output all items
     * within <code>inventory</code>.
     */
    private NavigableSet<InventoryItem> adminView;

    private final Map<InventoryItem.Comparator, NavigableSet<InventoryItem>> inventoryMaps;

    private boolean autoRestock;

    /**
     * An Analytics object that records sales data made via this IMS object.
     *
     * @see Analytics
     */
    private Analytics<String, Integer> saleAnalytics;

    public Inventory(File inventoryData, boolean autoRestocks) throws IOException {
        inventory = new HashMap<>();

        // TODO : Check that the file passed in is .json
        ObjectMapper mapper = new ObjectMapper();
        Set<InventoryItem> jsonInventory = mapper.readValue(inventoryData,
                new TypeReference<Set<InventoryItem>>() {});
        for(InventoryItem it : jsonInventory)
            inventory.put(it.getSku(), it);
        saleAnalytics = new SaleAnalytics(inventory.keySet());

        autoRestock = autoRestocks;
        inventoryMaps = new EnumMap<>(InventoryItem.Comparator.class);

        if(autoRestocks)
            restock();

        for(InventoryItem.Comparator c : InventoryItem.Comparator.values()) {
            inventoryMaps.put(c, new TreeSet<>(c.get().thenComparing(InventoryItem::getSku)));
            inventoryMaps.get(c).addAll(inventory.values());
        }

        adminView = inventoryMaps.get(InventoryItem.Comparator.SKU);
    }

    @Override
    public InventoryItemUpdater get(String sku) {
        InventoryItem item = inventory.get(sku);
        if(item == null) return null;
        return new InventoryItemProxy(item);
    }

    @Override
    public boolean contains(String sku) {
        return inventory.containsKey(sku);
    }

    public void outputAdminView() {
        System.out.println("=== Store Inventory ==================\n");
        InventoryItem.Comparator[] data = InventoryItem.Comparator.values();
        StringBuilder sb = new StringBuilder();
        for(InventoryItem.Comparator cell : data)
            sb.append(String.format("%18s", cell.toString())).append(" |");
        sb.append('\n');
        for(InventoryItem.Comparator cell : data)
            sb.append(String.format("%18s", "").replace(' ', '-')).append(" |");

        System.out.println(sb);
        adminView.forEach(System.out::println);
        System.out.println("\n======================================");
    }

    /**
     * <p>
     *     Prints out the set of all <code>InventoryItem</code>s tracked by the
     *     client, under the natural order of the <code>String</code> representations
     *     of each item's <code>sku</code>.
     * </p>
     *
     * <p>
     *     It must be noted that <code>sku</code> has no inherent purpose as a
     *     sortable entity, and thus the order by which each <code>InventoryItem</code>
     *     is outputted has no meaning aside from the Lexicographical order of their
     *     <code>sku</code>.
     * </p>
     */
    public void updateAdminView(InventoryItem.Comparator comparator) {
        if(comparator != null)
            adminView = inventoryMaps.get(comparator);
    }

    public void updateAdminView(List<InventoryItem.Comparator> comparators) {
        if(comparators == null) throw new NullPointerException();
        if(comparators.size() == 0) return;

        if(comparators.size() > 1) {
            ArrayList<Comparator<InventoryItem>> input = new ArrayList<>();
            for(int i = 0; i < comparators.size(); i++)
                input.add(comparators.get(i).get());

            Comparator<InventoryItem> outputComparator = input.getFirst();
            for(int i = 1; i < comparators.size(); i++)
                outputComparator = outputComparator.thenComparing(input.get(i));

            var customAdminView = new TreeSet<>(outputComparator);
            customAdminView.addAll(adminView);
            adminView = customAdminView;

            return;
        }
        adminView = inventoryMaps.get(comparators.getFirst());
    }

//    /**
//     * <p>
//     *     Prints out the set of all <code>InventoryItem</code>s tracked by the
//     *     client, ordered under the specifications of each <code>Comparator</code>
//     *     passed via <code>comparators</code>.
//     * </p>
//     *
//     * <p>
//     *     The method utilizes varargs rather than a <code>List</code> to permit
//     *     the caller to either pass a single <code>Comparator</code> with multiple
//     *     <code>thenComparing</code> conditions, or pass multiple <code>Comparator</code>s
//     *     to perform the same task.
//     * </p>
//     *
//     * @param comparators The <code>Comparator</code>s that define the order by which
//     *           to print out the set of all <code>InventoryItem</code>s tracked by the client.
//     */
//    public void updateAdminView(List<Comparator<InventoryItem>> comparators) {
//
//    }

    public boolean checkOutCart(Cart cart) {
//        final boolean debug = false;
//        if(cart.isEmpty() || !cart.isWithdrawn()) return false;
//        double total = 0;
//        for(var entry : cart.getProducts().entrySet())
//            total += inventory.get(entry.getKey()).sellItem(entry.getValue()); // .sellItem(SKU string, quantity)
//
//        cart.printSalesSlip();
//        System.out.println("Total = " + total);

        return true;
    }

    public void restock() {
        inventory.forEach((k, v) -> {
            if(v.hasAutomaticRestocks())
                v.placeInventoryOrder();
        });
    }

    public boolean hasAutomaticRestocks() {
        return autoRestock;
    }

    public void setAutomaticRestocks(boolean autoRestock) {
        this.autoRestock = autoRestock;
    }
}
