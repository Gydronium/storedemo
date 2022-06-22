package com.gydronium.storedemo.dto;

import com.gydronium.storedemo.util.ShopUnitTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class SalesItemOutDto {
    private UUID id;
    private String name;
    private ShopUnitTypeEnum type;
    private UUID parentId;
    private OffsetDateTime date;
    private Integer price;
}
