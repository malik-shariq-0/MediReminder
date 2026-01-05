package com.example.medireminder.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Medicine {
    public long id;
    public String name;
    public String time;

    // Constructor
    public Medicine(long id, String name, String time) {
        this.id = id;
        this.name = name;
        this.time = time;
    }

    // Convert Medicine → JSONObject
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("name", name);
            obj.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    // Convert JSONObject → Medicine
    public static Medicine fromJSON(JSONObject obj) {
        try {
            long id = obj.getLong("id");
            String name = obj.getString("name");
            String time = obj.getString("time");
            return new Medicine(id, name, time);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}