package com.DylanPerez.www.ims.application.inventory;

import com.DylanPerez.www.ims.application.inventory.interfaces.analytics.Analytics;
import com.DylanPerez.www.ims.application.inventory.interfaces.inventory.InventoryAccessor;
import com.DylanPerez.www.ims.application.itemtype.Product;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.InventoryItem;
import com.DylanPerez.www.ims.application.itemtype.inventory_item.interfaces.InventoryItemUpdater;
import com.DylanPerez.www.ims.application.util.IMSUtilities;
import com.DylanPerez.www.ims.presentation.util.Cart;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    private final Map<String, Integer> inventory; // Map<sku, Json array index>

    private Map<String, Integer> fieldMap;

    private final Map<String, NavigableMap<Object, Set<Integer>>> inventoryMaps; ///< Map<field name, NMap<field value, Set<Json array indices>>

    /**
     * A <code>Set</code> of all <code>InventoryItem</code>s within <code>inventory</code>
     * that are sorted by the Lexicographical order of each item's <code>sku</code>.
     *
     * This <code>Set</code> is specifically declared as a means to quickly output all items
     * within <code>inventory</code>.
     */
    private Set<InventoryItem> adminView;

    /**
     * An Analytics object that records sales data made via this IMS object.
     *
     * @see Analytics
     */
    private Analytics<String, Integer> saleAnalytics;

    private final File database;

    /// Utilized to deserialize inventory.json into a valid and usable Inventory object
    @JsonCreator
    private Inventory(
            @JsonProperty("itemFieldNames") List<String> itemFieldNames,
            @JsonProperty("size") int size,
            @JsonProperty("inventoryJsonPath") String inventoryJsonPath,
            @JsonProperty("items") List<InventoryItem> items
    ) {
        final boolean debug = true;

        if(debug) System.out.println("Called Inventory @JsonCreator!");

        this.database = new File(inventoryJsonPath);

        this.fieldMap = new LinkedHashMap<>();
        for(int i = 0; i < itemFieldNames.size(); i++)
            fieldMap.put(itemFieldNames.get(i), i);

       if(debug) System.out.println("itemFieldNames from Json = " + getItemFieldNames());


        this.inventory = new HashMap<>();

        this.inventoryMaps = new LinkedHashMap<>();
        initInventoryMaps(this.inventoryMaps);

        ObjectMapper mapper = new ObjectMapper();
        for(int i = 0; i < items.size(); i++) {
            String itemSku = items.get(i).getSku();
            inventory.put(itemSku, i);
            try {
                addToInventoryMaps(mapper, i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        updateAdminView(null);
    }

    public Inventory(String jsonPath) {
        final boolean debug = true;
        if(!IMSUtilities.isJson(jsonPath)) throw new IllegalArgumentException("Cannot pass non-Json file path to constructor.");

        if(debug) System.out.println("Calling Inventory(String jsonPath) constructor!");

        this.database = IMSUtilities.createResource(jsonPath);

        this.fieldMap = new LinkedHashMap<>();
        initItemFieldNames(fieldMap);
        this.inventory = new HashMap<>();
        this.inventoryMaps = new LinkedHashMap<>();
        initInventoryMaps(this.inventoryMaps);

        this.adminView = null;

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            /*
                If IMS could not properly deserialize inventory.json, rather than
                write over what was previously there, we will create a backup .json
                file in the default src/resources directory prior to writing over it with a
                default serialization of a new Inventory instance.

                This is to prevent a potentially massive loss of data in the event an unintended
                change is made to the primary inventory.json object.
             */
            if(!IMSUtilities.isEmpty(database)) {
                String backupId;
                File file = new File(database.getPath());
                while(file.exists()) {
                    backupId = "backup_" + Math.abs(new Random().nextLong());
                    file = new File("src/resources/backups/" + backupId + ".json");
                }
                boolean fileCreated = file.createNewFile();
                if(!fileCreated) throw new RuntimeException("Unable to read existing inventory.json; could not create backup file.");
                mapper.writeValue(file, mapper.readTree(database));
            }

            mapper.writeValue(database, this);

            ObjectNode node = (ObjectNode) mapper.readTree(database);
            node.putArray("items");
            mapper.writeValue(database, node);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateAdminView(null);

//        JsonNode head = mapper.readTree(database);

//        List<JsonNode> inventoryItemNodes = mapper.readValue(database,
//                new TypeReference<LinkedList<JsonNode>>() {});
//
//        /// Defined first for output purposes
//        inventoryMaps.put("sku", new TreeMap<>());

        /*
            For every json object defined in the json file contained within inventoryData,
            we convert it into an InventoryItem object, and for every field, if absent,
            we create a TreeMap to sort the entirety of this Inventory's InventoryItems
            by that particular field, where the key will be each InventoryItem's
            field value, and--to allow different InventoryItems with the same field value--,
            the value will be a set of all InventoryItems with an equivalent key.
         */
//        for(int i = 0; i < inventoryItemNodes.size(); i++) {
//            JsonNode object = inventoryItemNodes.get(i);
//
//            InventoryItem objectAsInventoryItem = jsonMapper.convertValue(object, InventoryItem.class);
//            if(debug) System.out.println("Reading item: " + objectAsInventoryItem.getName());
//
//            if(false) restock(objectAsInventoryItem); ///< For debugging and testing purposes only
//
//            Iterator<String> objectFields = object.fieldNames();
//            while(objectFields.hasNext()) {
//                String fieldName = objectFields.next();
//
//                inventoryMaps.computeIfAbsent(fieldName, fName -> new TreeMap<>()); ///< Can be done outside the loop to prevent unnecessary method call
//                Object fieldValue = switch(object.get(fieldName).getNodeType()) {
//                    case NUMBER -> object.get(fieldName).asInt();
//                    case BOOLEAN -> object.get(fieldName).asBoolean();
//                    default -> object.get(fieldName).asText();
//                };
//
//                inventoryMaps.get(fieldName).compute(fieldValue, (k, v) -> {
//                    if(v == null)
//                        v = new HashSet<>();
//
//                    v.add(objectAsInventoryItem);
//                    return v;
//                });
//            }
//        }
//
//        /// Uses single pre-existing TreeMap with name of permanent field to manually populate sku TreeMap
//        inventoryMaps.get("name").forEach((fValue, itemSet) -> {
//            itemSet.forEach((inventoryItem -> {
//
//                if(inventoryMaps.get("sku").containsKey(inventoryItem.getSku()))
//                    throw new RuntimeException("InventoryItem with duplicate sku!\n " +
//                            "Current - " + inventoryMaps.get("sku").get(inventoryItem.getSku()).toString() + "\n Duplicate - " + inventoryItem);
//
//                inventoryMaps.get("sku").compute(inventoryItem.getSku(), (itemSku, skuItemSet) -> {
//                    if(skuItemSet == null)
//                        skuItemSet = new HashSet<>();
//                    skuItemSet.add(inventoryItem);
//                    return skuItemSet;
//                });
//            }));
//        });
//
//        updateAdminView("sku");
    }

    @JsonGetter("inventoryJsonPath")
    public String getJsonPath() {
        return database.getPath();
    }

    @JsonGetter("size")
    public int size() {
        return inventory.size();
    }

    @JsonGetter("itemFieldNames")
    public List<String> getItemFieldNames() {
        return List.copyOf(fieldMap.keySet());
    }

    public void addItem(String... itemValues) {
        try {
            // key - InventoryItem SKU
            // value - Record number
            var itemRecordEntry = IMSUtilities.writeItem(database, itemValues);
            inventory.put(itemRecordEntry.getKey(), itemRecordEntry.getValue());
            addToInventoryMaps(new ObjectMapper(), itemRecordEntry.getValue());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InventoryItemUpdater get(String sku) {
//        InventoryItem item = inventory.get(sku);
//        if(item == null) return null;
//        return new InventoryItemProxy(item);
        return null;
    }

    @Override
    public boolean contains(String sku) {
        return inventory.containsKey(sku);
    }

    public void outputAdminView() {
        System.out.println("=== Store Inventory ==================\n");
        StringBuilder sb = new StringBuilder();
        for(var cell : inventoryMaps.keySet())
            sb.append(String.format("%18s", cell)).append(" |");
        sb.append('\n');
        for(var cell : inventoryMaps.keySet())
            sb.append(String.format("%18s", "").replace(' ', '-')).append(" |");

        System.out.println(sb);
        if(adminView != null) adminView.forEach(System.out::println);
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
    public void updateAdminView(String fieldName) {
        if(fieldName == null || fieldName.isEmpty()) fieldName = "sku";

        adminView = new LinkedHashSet<>();
            inventoryMaps.get(fieldName).values().forEach(recnos -> {
                    recnos.forEach(recno -> {
                        try {
                            adminView.add(IMSUtilities.readItem(database, recno));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            });
    }

//    public void updateAdminView(List<InventoryItem.Comparator> comparators) {
//        if(comparators == null) throw new NullPointerException();
//        if(comparators.size() == 0) return;
//
//        if(comparators.size() > 1) {
//            ArrayList<Comparator<InventoryItem>> input = new ArrayList<>();
//            for(int i = 0; i < comparators.size(); i++)
//                input.add(comparators.get(i).get());
//
//            Comparator<InventoryItem> outputComparator = input.getFirst();
//            for(int i = 1; i < comparators.size(); i++)
//                outputComparator = outputComparator.thenComparing(input.get(i));
//
//            var customAdminView = new TreeSet<>(outputComparator);
//            customAdminView.addAll(adminView);
//            adminView = customAdminView;
//
//            return;
//        }
//        adminView = inventoryMaps.get(comparators.getFirst());
//    }

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
//        inventory.forEach((k, v) -> {
//                restock(v);
//        });
    }

//    private void restock(InventoryItem item) {
//        final boolean debug = true;
//        if(debug) System.out.println("Inventory.restock() : Restocking " + item.getName() + "...");
//        item.placeInventoryOrder();
//
//        /// Update inventory.json
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//
//            ArrayNode inventoryNode = mapper.createArrayNode().addAll(
//                    Collections.unmodifiableCollection(mapper.readValue(this.inventoryData, new TypeReference<LinkedHashSet<JsonNode>>() {}))
//            );
//
//            ObjectNode itemNode = (ObjectNode) inventoryNode.get(inventoryRecnos.get(item.getSku()));
//
//            if(itemNode == null) throw new NullPointerException("Unable to restock InventoryItem with unknown sku(" + item + ")");
//            itemNode.replace("qtyTotal", mapper.valueToTree(item.getQtyTotal()));
//            System.out.println(inventoryNode);
//
//            JsonGenerator inventoryGenerator = mapper.getFactory().createGenerator(this.inventoryData, JsonEncoding.UTF8);
//            inventoryGenerator.useDefaultPrettyPrinter();
//            inventoryNode.serialize(inventoryGenerator,  mapper.getSerializerProviderInstance());
//            inventoryGenerator.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void addToInventoryMaps(ObjectMapper mapper, int recno) throws IOException {
        final boolean debug = true;
        ArrayNode jsonArray = (ArrayNode) mapper.readTree(database).get("items");
        ObjectNode jsonItem = (ObjectNode) jsonArray.get(recno);

        Iterator<String> fieldNames = jsonItem.fieldNames();
        while(fieldNames.hasNext()) {
            String itemFieldName = fieldNames.next();

            if(debug) System.out.println("addToInventoryMaps() : itemFieldName = " + itemFieldName);

            inventoryMaps.get(itemFieldName).compute(mapper.convertValue(jsonItem.get(itemFieldName), new TypeReference<>() {}), (k, v) -> {
                if(v == null)
                    v = new HashSet<>();

                if(debug) System.out.println("addToInventoryMaps() : itemFieldValue = " + k);

                v.add(recno);
                return v;
            });
        }
    }

    private static Map<String, Integer> initItemFieldNames(Map<String, Integer> fieldMap) {
        if(fieldMap == null) return null;
        /*
            Source: https://stackoverflow.com/questions/45834654/how-to-get-the-list-of-properties-of-a-class-as-jackson-views-it/45839099#45839099
            Used to retrieve the Json Properties of the InventoryItem class to create a map and
            Json ArrayNode of them for easy accessing and output.
         */
        ObjectMapper mapper = new ObjectMapper();
        // Construct a Jackson JavaType for your class
        JavaType javaType = mapper.getTypeFactory().constructType(InventoryItem.class);
        // Introspect the given type
        BeanDescription beanDescription = mapper.getSerializationConfig().introspect(javaType);
        // Find properties
        List<BeanPropertyDefinition> properties = beanDescription.findProperties();

        for(int i = 0; i < properties.size(); i++)
            fieldMap.put(properties.get(i).getName(), i);
        return fieldMap;
    }

    private void initInventoryMaps(Map<String, NavigableMap<Object, Set<Integer>>> inventoryMaps) {
        final boolean debug = true;
        if(this.fieldMap.isEmpty()) {
            if(debug) System.out.println("initInventoryMaps() : Calling initItemFieldNames()!");
            initItemFieldNames(this.fieldMap);
        }

        fieldMap.forEach((fieldName, fieldIndex) -> inventoryMaps.put(fieldName, new TreeMap<>()));
    }

}
