package com.gydronium.storedemo.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class ImportsDto {

    @NotNull
    @Valid
    private List<ItemDto> items;
    @NotNull
    private OffsetDateTime updateDate;
}
