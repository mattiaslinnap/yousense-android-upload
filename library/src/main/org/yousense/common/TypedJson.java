package org.yousense.common;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Helpers for loading JSON files without being able to describe the data type beforehand.
 */
public class TypedJson {

    /**
     * Loads a JSON object (also called a hash or a key-value map) from a file. The types of values are chosen based on string keys.
     * Types in the second argument must be a Gson TypeToken<? extends T> instances.
     * T is the base type (can be Object).
     *
     * Never throws exceptions or returns null - returns as much as was possible to load.
     */
    public static <T> Map<String, T> fromJsonObjectTypedByKeys(String fullJsonString, Map<String, Type> types) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        HashMap<String, T> loaded = new HashMap<String, T>();
        try {
            JsonElement fullJson = parser.parse(fullJsonString);
            if (fullJson.isJsonObject()) {
                for (Map.Entry<String, JsonElement> element : fullJson.getAsJsonObject().entrySet()) {
                    Type type = types.get(element.getKey());
                    if (type != null) {
                        try {
                            loaded.put(element.getKey(), (T)gson.fromJson(element.getValue(), type));
                        } catch (JsonParseException e) {
                            // Failed to parse this element with the correct type.
                        } catch (ClassCastException e) {
                            // Failed to cast this element to base type.
                        }
                    }
                }
            }
        // Failed to parse or read outer JSON map, or other unknown parse error.
        } catch (JsonParseException e) {
        } catch (ClassCastException e) {
        } catch (IllegalStateException e) {
        }
        return loaded;
    }
}
