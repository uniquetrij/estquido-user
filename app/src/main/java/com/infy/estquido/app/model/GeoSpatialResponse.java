package com.infy.estquido.app.model;

import java.util.ArrayList;

public class GeoSpatialResponse {

    Status StatusObject;
    Request RequestObject;


    ArrayList < Object > hits = new ArrayList < Object > ();
    private float total_hits;
    private float max_score;
    private float took;
    private String facets = null;


    // Getter Methods

    public Status getStatus() {
        return StatusObject;
    }

    public Request getRequest() {
        return RequestObject;
    }

    public float getTotal_hits() {
        return total_hits;
    }

    public float getMax_score() {
        return max_score;
    }

    public float getTook() {
        return took;
    }

    public String getFacets() {
        return facets;
    }


    public ArrayList<Object> getHits() {
        return hits;
    }

    // Setter Methods

    public void setHits(ArrayList<Object> hits) {
        this.hits = hits;
    }

    public void setStatus(Status statusObject) {
        this.StatusObject = statusObject;
    }

    public void setRequest(Request requestObject) {
        this.RequestObject = requestObject;
    }

    public void setTotal_hits(float total_hits) {
        this.total_hits = total_hits;
    }

    public void setMax_score(float max_score) {
        this.max_score = max_score;
    }

    public void setTook(float took) {
        this.took = took;
    }

    public void setFacets(String facets) {
        this.facets = facets;
    }
}

class Request {
    Query QueryObject;
    private float size;
    private float from;
    private String highlight = null;
    private String fields = null;
    private String facets = null;
    private boolean explain;
    ArrayList < Object > sort = new ArrayList < Object > ();
    private boolean includeLocations;


    // Getter Methods

    public Query getQuery() {
        return QueryObject;
    }

    public float getSize() {
        return size;
    }

    public float getFrom() {
        return from;
    }

    public String getHighlight() {
        return highlight;
    }

    public String getFields() {
        return fields;
    }

    public String getFacets() {
        return facets;
    }

    public boolean getExplain() {
        return explain;
    }

    public boolean getIncludeLocations() {
        return includeLocations;
    }

    // Setter Methods

    public void setQuery(Query queryObject) {
        this.QueryObject = queryObject;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setFrom(float from) {
        this.from = from;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public void setFacets(String facets) {
        this.facets = facets;
    }

    public void setExplain(boolean explain) {
        this.explain = explain;
    }

    public void setIncludeLocations(boolean includeLocations) {
        this.includeLocations = includeLocations;
    }
}

class Status {
    private float total;
    private float failed;
    private float successful;

    // Getter Methods

    public float getTotal() {
        return total;
    }

    public float getFailed() {
        return failed;
    }

    public float getSuccessful() {
        return successful;
    }

    // Setter Methods

    public void setTotal(float total) {
        this.total = total;
    }

    public void setFailed(float failed) {
        this.failed = failed;
    }

    public void setSuccessful(float successful) {
        this.successful = successful;
    }

}
