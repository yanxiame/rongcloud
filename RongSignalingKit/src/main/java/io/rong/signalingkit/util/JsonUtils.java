package io.rong.signalingkit.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    public static Map<String, Object> jsonStrToMap(String jsonStr) {
        Map<String, Object> params = null;
        try {
            params = jsonToMap(new JSONObject(jsonStr));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params;
    }

    public static Map<String, Object> jsonToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<String, Object>();
        Object value;
        String key;
        for (Iterator<?> it = jsonObject.keys(); it.hasNext(); ) {
            key = (String) it.next();
            if (jsonObject.isNull(key)) {
                map.put(key, null);
            } else {
                try {
                    value = jsonObject.get(key);
                    if (value instanceof JSONArray) {
                        value = toList((JSONArray) value);
                    } else if (value instanceof JSONObject) {
                        value = jsonToMap((JSONObject) value);
                    }
                    map.put(key, value);
                } catch (JSONException e) {

                }
            }
        }
        return map;
    }

    private static List toList(JSONArray array) {
        List list = new ArrayList();
        Object value;
        for (int i = 0; i < array.length(); i++) {
            try {
                value = array.get(i);
                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = jsonToMap((JSONObject) value);
                }
                list.add(value);
            } catch (JSONException e) {

            }
        }
        return list;
    }

    public static String mapToString(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }
}