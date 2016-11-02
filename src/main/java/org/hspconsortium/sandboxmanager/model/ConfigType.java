package org.hspconsortium.sandboxmanager.model;

public enum ConfigType {
    FHIR_QUERY(0);

    private int numVal;

    ConfigType(int numVal) {
        this.numVal = numVal;
    }

    public static ConfigType fromInt(int num) {
        for (ConfigType configType : values() ){
            if (configType.numVal == num) return configType;
        }
        return null;
    }

    public int getNumVal() {
        return numVal;
    }
}
