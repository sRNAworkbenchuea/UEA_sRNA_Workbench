/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;

/**
 *
 * @author Chris Applegate
 */
public class JsonUtils {

    public static JsonObject parseJsonFile(File jsonFile) throws IOException {
        InputStream stream = org.apache.commons.io.FileUtils.openInputStream(jsonFile);
        JsonParser parser = Json.createParser(stream);
        JsonObjectBuilder obj = Json.createObjectBuilder();
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            if (event == JsonParser.Event.START_OBJECT) {
                obj = parseJsonObject(parser);
            }
        }
        parser.close();
        stream.close();
        return obj.build();
    }

    private static JsonArrayBuilder parseJsonArray(JsonParser parser) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        JsonParser.Event event = parser.next();
        while (event != JsonParser.Event.END_ARRAY && parser.hasNext()) {
            if (event == JsonParser.Event.START_OBJECT) {
                JsonObjectBuilder jsonObjectBuilder = parseJsonObject(parser);
                jsonArrayBuilder.add(jsonObjectBuilder);
            }
            event = parser.next();
        }
        return jsonArrayBuilder;
    }

    private static JsonObjectBuilder parseJsonObject(JsonParser parser) {
        // create a new json object
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        JsonParser.Event event = parser.next();
        String key = "";
        while (event != JsonParser.Event.END_OBJECT && parser.hasNext()) {
            if (event == JsonParser.Event.KEY_NAME) {
                key = parser.getString();
            } else if (event == JsonParser.Event.START_ARRAY) {
                JsonArrayBuilder jsonArrayBuilder = parseJsonArray(parser);
                JsonArray jsonArray = jsonArrayBuilder.build();
                jsonObjectBuilder.add(key, jsonArray);
            } else if (event == JsonParser.Event.VALUE_STRING) {
                jsonObjectBuilder.add(key, parser.getString());
            } else if (event == JsonParser.Event.VALUE_NUMBER) {
                jsonObjectBuilder.add(key, parser.getBigDecimal());
            } else if (event == JsonParser.Event.VALUE_TRUE) {
                jsonObjectBuilder.add(key, true);
            } else if (event == JsonParser.Event.VALUE_FALSE) {
                jsonObjectBuilder.add(key, false);
            }
            event = parser.next();
        }
        return jsonObjectBuilder;
    }

    public static JsonObjectBuilder buildJsonObject(Map<String, Object> map) {

        JsonObjectBuilder record = Json.createObjectBuilder();
        for (String key : map.keySet()) {

            record.add(key, map.get(key).toString());
        }
        return record;
       
    }

    public static JsonArrayBuilder buildJsonArray(List<Map<String, Object>> maps) {
        JsonArrayBuilder recordArray = Json.createArrayBuilder();
        for (Map<String, Object> map : maps) {
            JsonObjectBuilder jsonObjectBuilder = buildJsonObject(map);
            recordArray.add(jsonObjectBuilder);
        }
        return recordArray;
    }
}
