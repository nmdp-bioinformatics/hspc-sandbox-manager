package org.hspconsortium.sandboxmanager.model;

public enum Role {
    ADMIN(0), USER(1), READONLY(2);

    private int numVal;

    Role(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
