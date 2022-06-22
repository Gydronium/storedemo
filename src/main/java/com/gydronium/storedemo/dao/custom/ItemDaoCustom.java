package com.gydronium.storedemo.dao.custom;

import com.gydronium.storedemo.model.Item;

import java.time.OffsetDateTime;
import java.util.List;

public interface ItemDaoCustom {

    List<Item> getItemsUpdatedForDayBefore(OffsetDateTime dateTime);
}
