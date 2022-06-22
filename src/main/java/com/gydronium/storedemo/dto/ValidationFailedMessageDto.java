package com.gydronium.storedemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationFailedMessageDto {
    private Integer code;
    private String message;
}
