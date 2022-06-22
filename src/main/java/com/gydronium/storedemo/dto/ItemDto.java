package com.gydronium.storedemo.dto;

import com.gydronium.storedemo.util.ShopUnitTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@ToString
public class ItemDto {

    @NotNull
    private UUID id;
    @NotNull
    private String name;
    private UUID parentId;
    private Integer price;
    @NotNull
    private ShopUnitTypeEnum type;
}
