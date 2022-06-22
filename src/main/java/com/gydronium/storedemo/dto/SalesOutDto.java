package com.gydronium.storedemo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SalesOutDto {
    private List<SalesItemOutDto> items;
}
