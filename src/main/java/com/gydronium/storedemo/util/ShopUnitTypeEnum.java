package com.gydronium.storedemo.util;

public enum ShopUnitTypeEnum {
    OFFER("OFFER"),
    CATEGORY("CATEGORY");

    private ShopUnitTypeEnum(String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }
}
