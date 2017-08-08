package org.hspconsortium.sandboxmanager.model;

public enum DataSet {
    NA("na"), NONE("none"), DEFAULT("default");

    private String value;

    DataSet(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

};