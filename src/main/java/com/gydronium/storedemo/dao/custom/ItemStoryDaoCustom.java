package com.gydronium.storedemo.dao.custom;

import com.gydronium.storedemo.model.ItemStory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemStoryDaoCustom {
    List<ItemStory> getAllItemStories(UUID itemId, OffsetDateTime dateStart, OffsetDateTime dateEnd);
}
