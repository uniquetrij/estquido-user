package com.infy.estquido.app.model;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class GeoSpatialRequest {
    private int from;
    private int size;
    Query queryObject;

    // Getter Methods

    public float getFrom() {
        return from;
    }

    public float getSize() {
        return size;
    }

    public Query getQuery() {
        return queryObject;
    }

    // Setter Methods

    public void setFrom(int from) {
        this.from = from;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setQuery(Query queryObject) {
        this.queryObject = queryObject;
    }

    public Map<String, String> toMap() {

        Gson gson =new Gson();

        Map<String, String> map = new HashMap<>();

        map.put("from", String.valueOf(from));
        map.put("size", String.valueOf(size));

        Map<String,String> queryMap=new HashMap<>();
        queryMap.put("distance",queryObject.getDistance());
        queryMap.put("field",queryObject.getField());

        Map<String,String> loc=new HashMap<>();
        loc.put("lat",queryObject.getLocation().getLat() + "");
        loc.put("lon",queryObject.getLocation().getLon() + "");

        queryMap.put("location",gson.toJson(loc));

        map.put("query", gson.toJson(queryMap));

        return map;
    }
}

