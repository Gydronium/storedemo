package com.gydronium.storedemo.exception;

public class StoreException extends RuntimeException {

    public StoreException() {
        super("Validation Failed");
    }
}
