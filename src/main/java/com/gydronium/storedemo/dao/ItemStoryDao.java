package com.gydronium.storedemo.dao;

import com.gydronium.storedemo.dao.custom.ItemStoryDaoCustom;
import com.gydronium.storedemo.model.Item;
import com.gydronium.storedemo.model.ItemStory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ItemStoryDao extends JpaRepository<ItemStory, UUID>, ItemStoryDaoCustom {
    Optional<ItemStory> findFirstByItemOrderByDateUpdatedDesc(Item item);
}
