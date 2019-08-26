package com.infy.estquido.app.model;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Query {
    Location location;
    private String distance;
    private String field;


    // Getter Methods

    public String getDistance() {
        return distance;
    }

    public String getField() {
        return field;
    }

    // Setter Methods

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @NonNull
    @Override
    public String toString() {

        Map<String, String> map = new HashMap<>();

        map.put("location", location.toString());
        map.put("distance", distance);
        map.put("field", field);

        return map.toString();
    }
}

