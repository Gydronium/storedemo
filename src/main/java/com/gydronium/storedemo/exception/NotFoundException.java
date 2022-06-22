package com.gydronium.storedemo.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException() {
        super("Item not found");
    }
}
