package com.gydronium.storedemo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class IAmTeapotDto {
    private String message;
    private Integer code;

    public IAmTeapotDto() {
        this("teapot");
    }

    public IAmTeapotDto(String name) {
        this.message = String.format("I am a %s!", name);
        this.code = HttpStatus.I_AM_A_TEAPOT.value();
    }
}
