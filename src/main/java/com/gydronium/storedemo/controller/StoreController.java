package com.gydronium.storedemo.controller;

import com.gydronium.storedemo.dto.IAmTeapotDto;
import com.gydronium.storedemo.dto.ImportsDto;
import com.gydronium.storedemo.dto.ItemOutDto;
import com.gydronium.storedemo.dto.SalesOutDto;
import com.gydronium.storedemo.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;

    /**
     * Эндпоинт, который предназначен для новых и обновлениия старых товаров и категорий
     * @param imports
     */
    @PostMapping("/imports")
    @ResponseStatus(HttpStatus.OK)
    public void saveItems(@Valid @RequestBody ImportsDto imports) {
        storeService.saveImports(imports);
    }

    /**
     * Эндпоинт для получения статистики по товару или категории
     * @param id
     * @return
     */
    @GetMapping("/nodes/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ItemOutDto getItems(@PathVariable UUID id) {
        return storeService.getItems(id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteItem(@PathVariable UUID id) {
        storeService.deleteItem(id);
    }

    @GetMapping("/sales")
    public SalesOutDto getSales(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime date) {
        System.out.println("date is " + date.toString());
        return storeService.getSales(date);
    }

    @GetMapping("nodes/{id}/statistic")
    public SalesOutDto getStatisticForItem(@PathVariable UUID id,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<OffsetDateTime> dateStart,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<OffsetDateTime>  dateEnd) {
        return storeService.getStatisticForItem(id, dateStart, dateEnd);
    }

    @GetMapping("/i-am-a-teapot")
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public IAmTeapotDto getTeapot(@RequestParam Optional<String> name) {
        return name.map(IAmTeapotDto::new).orElseGet(IAmTeapotDto::new);
    }
}
