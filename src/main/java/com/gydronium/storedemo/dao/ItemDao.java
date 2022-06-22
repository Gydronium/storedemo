package com.gydronium.storedemo.dao;

import com.gydronium.storedemo.dao.custom.ItemDaoCustom;
import com.gydronium.storedemo.model.Item;
import com.gydronium.storedemo.util.ShopUnitTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemDao extends JpaRepository<Item, UUID>, ItemDaoCustom {
    List<Item> findItemByType(ShopUnitTypeEnum type);
}
