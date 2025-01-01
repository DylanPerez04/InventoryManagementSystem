package com.DylanPerez.www.ims.application.util;

import com.DylanPerez.www.ims.application.itemtype.inventory_item.InventoryItem;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class IMSUtilities {
    
    public static boolean isJson(File file) {
        if(file == null) throw new NullPointerException("Cannot discern whether a null File object is Json.");
        return file.getName().substring(file.getName().indexOf('.')).equalsIgnoreCase(".json");
    }

    public static boolean isJson(String path) {
        if(path == null) throw new NullPointerException("Cannot discern whether a null path leads to a Json file.");
        if(!path.contains(".")) throw new IllegalArgumentException("No file defined in path to check.");
        return path.substring(path.indexOf('.')).equalsIgnoreCase(".json");
    }

    public static boolean isEmpty(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(file) == null || mapper.readTree(file).size() == 0;
    }

    public static File createResource(String path) {
        final boolean debug = false;
        File file = new File(path);

        ObjectMapper mapper = new ObjectMapper();
        try {
            if(!file.exists() || mapper.readTree(file) == null) {
                file.createNewFile();

                /// If the file doesn't exist, create a Json file and generate an empty Json object
                var jsonGenerator = mapper.getFactory().createGenerator(file, JsonEncoding.UTF8);
                mapper.writeTree(jsonGenerator, mapper.createObjectNode());
                jsonGenerator.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

//    private static File deserializeResource(File file) {
//    }

    public static Map.Entry<String, Integer> writeItem(File file, String... itemValues) throws IOException {
        final boolean debug = true;
        if(!file.exists() || !isJson(file)) throw new IllegalArgumentException("File passed cannot be used for serialization. " +
                "Check that the file exists and is .json.");

        InventoryItem item = null;
        if(debug)
            item = InventoryItem.foo();
        else
            item = new InventoryItem(
                    null, itemValues[0], itemValues[1], Category.valueOf(itemValues[2].toUpperCase()),
                    Double.parseDouble(itemValues[3]), Double.parseDouble(itemValues[4]),
                    Integer.parseInt(itemValues[5]), Integer.parseInt(itemValues[6]), Integer.parseInt(itemValues[7]), Integer.parseInt(itemValues[8]),
                    Boolean.parseBoolean(itemValues[9]), Boolean.parseBoolean(itemValues[10])
            );

        return Map.entry(item.getSku(), item.write(file));
    }

    public static InventoryItem readItem(File file, int recno) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode item = (ObjectNode) mapper.readTree(file).get("items").get(recno);
        return mapper.convertValue(item, InventoryItem.class);
    }

    public static boolean clear(File file) {
        if(!file.exists() || !isJson(file)) throw new IllegalArgumentException("File passed cannot be cleared. " +
                "Check that the file exists and is .json.");

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(file, mapper.createObjectNode());
        } catch(IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
