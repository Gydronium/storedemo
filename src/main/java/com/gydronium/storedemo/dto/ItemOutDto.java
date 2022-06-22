package com.gydronium.storedemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ItemOutDto extends SalesItemOutDto {
    private Set<ItemOutDto> children;

    public ItemOutDto(SalesItemOutDto itemDto) {
        setId(itemDto.getId());
        setPrice(itemDto.getPrice());
        setParentId(itemDto.getParentId());
        setDate(itemDto.getDate());
        setName(itemDto.getName());
        setType(itemDto.getType());
    }
}
