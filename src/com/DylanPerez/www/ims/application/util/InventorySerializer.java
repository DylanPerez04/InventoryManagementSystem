package com.DylanPerez.www.ims.application.util;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;

public abstract class InventorySerializer {

    public static boolean isJson(File file) {
        return file.getName().substring(file.getName().indexOf('.')).equalsIgnoreCase(".json");
    }

    public static boolean clear(File file) {
        if(!isJson(file)) return false;

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode node = mapper.createArrayNode();
        try {
            node.serialize(mapper.getFactory().createGenerator(file, JsonEncoding.UTF8), mapper.getSerializerProviderInstance());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
