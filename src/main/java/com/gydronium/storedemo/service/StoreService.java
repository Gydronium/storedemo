package com.gydronium.storedemo.service;

import com.gydronium.storedemo.dao.ItemDao;
import com.gydronium.storedemo.dao.ItemStoryDao;
import com.gydronium.storedemo.dto.*;
import com.gydronium.storedemo.exception.NotFoundException;
import com.gydronium.storedemo.exception.StoreException;
import com.gydronium.storedemo.model.Item;
import com.gydronium.storedemo.model.ItemStory;
import com.gydronium.storedemo.util.ShopUnitTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Сервис для обработки запросов пользователя
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {
    private final static Integer CHILD_OFFER_AMOUNT_FOR_TYPE_OFFER = 1;
    private final static Integer CHILD_OFFER_AMOUNT_FOR_TYPE_CATEGORY = 0;

    private final ItemDao itemDao;
    private final ItemStoryDao itemStoryDao;

    /**
     * Метод для сохранения товаров и категорий. Товары/категории импортированные повторно обновляют текущие.
     * @param imports входные данные
     */
    public void saveImports(ImportsDto imports) {
        OffsetDateTime date = imports.getUpdateDate();
        Map<UUID, ItemStory> itemStoryMap = new HashMap<>();
        Map<UUID, Item> itemMap = imports.getItems().stream().map(itemDto -> getItemEntity(itemDto, date, itemStoryMap)).collect(Collectors.toMap(Item::getId, Function.identity()));
        itemDao.saveAll(itemMap.values());
        Map<UUID, UUID> oldParentMap = new HashMap<>();
        setParents(itemMap, imports.getItems(), itemStoryMap, oldParentMap);
        setOfferChildrenAmount(itemMap, itemStoryMap, oldParentMap, date);
        itemDao.saveAll(itemMap.values());
        itemStoryDao.saveAll(itemStoryMap.values());
        log.info("items saved");
    }

    /**
     * Метод для маппинга данных в сущности для сохранения в базу данных
     * @param itemDto входной объект
     * @param dateTime время обновления объекта
     * @param itemStoryMap словарь для промежуточного хранения сущностей типа item_story
     * @return
     */
    private Item getItemEntity(ItemDto itemDto, OffsetDateTime dateTime, Map<UUID, ItemStory> itemStoryMap) {
        Optional<Item> itemPrevOpt =  itemDao.findById(itemDto.getId());
        ShopUnitTypeEnum type = itemDto.getType();
        if (itemPrevOpt.isPresent() && !itemPrevOpt.get().getType().equals(type)) {
            throw new StoreException();
        }
        Integer childAmount;
        Optional<Integer> priceOpt = Optional.ofNullable(itemDto.getPrice());
        if (ShopUnitTypeEnum.OFFER.equals(type)) {
            if (!priceOpt.isPresent() || priceOpt.get() < 0) {
                throw new StoreException();
            }
            childAmount = CHILD_OFFER_AMOUNT_FOR_TYPE_OFFER;
        } else {
            if (priceOpt.isPresent()) {
                throw new StoreException();
            }
            childAmount = CHILD_OFFER_AMOUNT_FOR_TYPE_CATEGORY;
        }
        Item item = itemPrevOpt.orElse(new Item());
        item.setId(itemDto.getId());
        item.setType(type);
        ItemStory itemStory = new ItemStory();
        Optional<ItemStory> prevStoryOpt = itemStoryDao.findFirstByItemOrderByDateUpdatedDesc(item);
        itemStory.setOfferChildrenAmount(prevStoryOpt.map(ItemStory::getOfferChildrenAmount)
                .orElse(childAmount));
        itemStory.setId(UUID.randomUUID());
        itemStory.setItem(item);
        itemStory.setName(itemDto.getName());
        Integer price = priceOpt.orElseGet(() -> prevStoryOpt.map(ItemStory::getPrice).orElse(null));
        itemStory.setPrice(price != null ? price : -1);
        itemStory.setDateUpdated(dateTime);
        itemStoryMap.put(item.getId(), itemStory);
        return item;
    }

    /**
     * Метод, который задает родителей для входных объектов
     * @param itemMap словарь для хранения сущностей типа item
     * @param itemDtos входные данные от пользователя
     * @param itemStoryMap словарь для промежуточного хранения сущностей типа item_story
     * @param oldParentMap словарь для промежуточного хранения бывших родителей объектов. Этот словарь нужен для обновления старых родителей
     */
    private void setParents(Map<UUID, Item> itemMap, List<ItemDto> itemDtos, Map<UUID, ItemStory> itemStoryMap, Map<UUID, UUID> oldParentMap) {
        itemDtos.forEach(itemDto -> {
            Item item = itemMap.get(itemDto.getId());
            itemStoryDao.findFirstByItemOrderByDateUpdatedDesc(item).ifPresent(storyWithOldParent -> oldParentMap.put(item.getId(), storyWithOldParent.getParentId()));
            Optional<UUID> parentIdOpt = Optional.ofNullable(itemDto.getParentId());
            if (parentIdOpt.isPresent()) {
                Item parent = parentIdOpt.flatMap(itemDao::findById).orElseGet(() -> getParentFromImports(parentIdOpt.get(), itemMap));
                if (ShopUnitTypeEnum.OFFER.equals(parent.getType())) {
                    throw new StoreException();
                }
                item.setParent(parent);
                itemStoryMap.get(itemDto.getId()).setParentId(parent.getId());
            } else {
                item.setParent(null);
                itemStoryMap.get(itemDto.getId()).setParentId(null);
            }
        });
    }

    private Item getParentFromImports(UUID parentId, Map<UUID, Item> itemMap) {
        return Optional.ofNullable(itemMap.get(parentId)).orElseThrow(StoreException::new);
    }

    /**
     * Метод для запуска рекурсивного обхода родителей. Обычно запуск происходит только от товаров (первый if). Но если в объекте imports для обновления
     * пришла категория без детей, то требуется запустить рекурсию от нее (второй if).
     * @param itemMap словарь для хранения сущностей типа item
     * @param itemStoryMap словарь для промежуточного хранения сущностей типа item_story
     * @param oldParentMap словарь для промежуточного хранения бывших родителей объектов
     * @param dateUpdated время обновления
     */
    private void setOfferChildrenAmount(Map<UUID, Item> itemMap, Map<UUID, ItemStory> itemStoryMap, Map<UUID, UUID> oldParentMap, OffsetDateTime dateUpdated) {
        itemMap.forEach((id, item) -> {
            if (ShopUnitTypeEnum.OFFER.equals(item.getType())) {
                updateParentsForItem(itemStoryMap, oldParentMap, dateUpdated, id, item);
            } else if (ShopUnitTypeEnum.CATEGORY.equals(item.getType())) {
                if (itemMap.values().stream()
                        .map(Item::getParent)
                        .filter(Objects::nonNull)
                        .map(Item::getId)
                        .noneMatch(id::equals)) {
                    updateParentsForItem(itemStoryMap, oldParentMap, dateUpdated, id, item);
                }
            }
        });
    }

    /**
     * Вход рекурсии. Здесь запускается рекурсия с двумя значениями. Если в новой item_story есть parentId, то к этому объекту надо добавить price и childOfferAmount.
     * Если в мапе oldParentMap присутствует текущий проверяемый id, то значит в прошлом item_story хранилось parent_id. Нужно у старого родителя отнять старые значения
     * price и childOfferAmount.
     * @param itemStoryMap словарь для промежуточного хранения сущностей типа item_story
     * @param oldParentMap словарь для промежуточного хранения id бывших родителей объектов
     * @param dateUpdated время обновления
     * @param id текущий проверяемый id объекта
     * @param item текущий обновляемый item
     */
    private void updateParentsForItem(Map<UUID, ItemStory> itemStoryMap, Map<UUID, UUID> oldParentMap, OffsetDateTime dateUpdated, UUID id, Item item) {
        ItemStory itemStory = itemStoryMap.get(id);
        Optional.ofNullable(itemStory.getParentId()).ifPresent(parentId -> {
            updateParent(parentId, itemStoryMap, itemStory.getOfferChildrenAmount(), itemStory.getPrice(), dateUpdated);
        });
        Optional.ofNullable(oldParentMap.get(id)).ifPresent(parentId -> {
            ItemStory oldItemStory = itemStoryDao.findFirstByItemOrderByDateUpdatedDesc(item).orElseThrow(StoreException::new);
            updateParent(parentId, itemStoryMap, -1 * oldItemStory.getOfferChildrenAmount(), -1 * oldItemStory.getPrice(), dateUpdated);
        });
    }

    /**
     * Рекурсивный метод для обхода родителей. Цель метода заключается в обновлении записей item_story, полученных в imports,
     * и добавлении новых записей для дальнейшего сохранения. Метод также используется при удалении объектов, чтобы уменьшить price и offerChildrenAmount
     * @param parentId uuid который проверяется в данный момент
     * @param itemStoryMap словарь для промежуточного хранения сущностей типа item_story
     * @param offerChildrenAmount текущее значение offerChildrenAmount, которое надо задать родителям.
     * @param offerChildPrice текущее значение price, которое надо задать родителям.
     * @param dateUpdated время обновления
     */
    private void updateParent(UUID parentId, Map<UUID, ItemStory> itemStoryMap, Integer offerChildrenAmount, Integer offerChildPrice, OffsetDateTime dateUpdated) {
        if (itemStoryMap.containsKey(parentId)) {
            ItemStory itemStory = itemStoryMap.get(parentId);
            itemStory.setOfferChildrenAmount(itemStory.getOfferChildrenAmount() + offerChildrenAmount);
            itemStory.setPrice(itemStory.getPrice() == -1 ? offerChildPrice : itemStory.getPrice() + offerChildPrice);
            Optional.ofNullable(itemStory.getParentId())
                    .ifPresent(p -> updateParent(p, itemStoryMap, offerChildrenAmount, offerChildPrice, dateUpdated));
        }
        // Сюда заходим когда происходит обновление через endpoint "/imports" и endpoint "/delete". Когда заходим через "/imports", то dateUpdated присутствует.
        // Когда заходим через "/delete", то дата обновления отсутствует.
        else {
            ItemStory prevItemStory = itemDao.findById(parentId).flatMap(itemStoryDao::findFirstByItemOrderByDateUpdatedDesc).orElseThrow(StoreException::new);
            ItemStory itemStoryForParent;
            if (dateUpdated != null) {
                itemStoryForParent = new ItemStory();
                itemStoryForParent.setId(UUID.randomUUID());
                itemStoryForParent.setItem(itemDao.findById(parentId).orElseThrow(StoreException::new));
                itemStoryForParent.setDateUpdated(dateUpdated);
                itemStoryForParent.setName(prevItemStory.getName());
                itemStoryForParent.setParentId(prevItemStory.getParentId());
            } else {
                itemStoryForParent = prevItemStory;
            }
            itemStoryForParent.setOfferChildrenAmount(prevItemStory.getOfferChildrenAmount() + offerChildrenAmount);
            itemStoryForParent.setPrice(prevItemStory.getPrice() == -1 ? offerChildPrice : prevItemStory.getPrice() + offerChildPrice);
            itemStoryMap.put(parentId, itemStoryForParent);
            Optional.ofNullable(prevItemStory.getParentId())
                    .ifPresent(p  -> updateParent(p, itemStoryMap, offerChildrenAmount, offerChildPrice, dateUpdated));
        }
    }

    /**
     * Метод для получения информации об объекте. Возвращает dto, который содержит всех детей.
     * @param itemId
     * @return
     */
    public ItemOutDto getItems(UUID itemId) {
        Item item = itemDao.findById(itemId).orElseThrow(NotFoundException::new);
        return getItemOutDto(item);
    }

    /**
     * Метод для рекурсивного заполения dto. Рекурсия нужно для заполнения поля children
     * @param item
     * @return
     */
    private ItemOutDto getItemOutDto(Item item) {
        ItemStory lastItemStory = itemStoryDao.findFirstByItemOrderByDateUpdatedDesc(item).orElseThrow(StoreException::new);
        ItemOutDto itemOutDto = new ItemOutDto(getSalesItemDto(item));
        Set<ItemOutDto> children = item.getChildren().stream()
                .map(this::getItemOutDto)
                .collect(Collectors.toSet());
        if (!children.isEmpty()) {
            itemOutDto.setChildren(children);
        }
        if (lastItemStory.getPrice() != -1 && lastItemStory.getOfferChildrenAmount() != 0) {
            itemOutDto.setPrice(lastItemStory.getPrice() / lastItemStory.getOfferChildrenAmount());
        }
        return itemOutDto;
    }

    /**
     * Метод для удаления объекта, детей и всех связанных с ними сущностей item_story. Также метод запускает рекурсивный метод для обновления родителей.
     * @param itemId
     */
    public void deleteItem(UUID itemId) {
        Item item = itemDao.findById(itemId).orElseThrow(NotFoundException::new);
        Map<UUID, ItemStory> itemStoryMap = new HashMap<>();
        ItemStory lastItemStory = itemStoryDao.findFirstByItemOrderByDateUpdatedDesc(item).orElseThrow(StoreException::new);
        Optional.ofNullable(item.getParent())
                .ifPresent(p -> updateParent(p.getId(), itemStoryMap, -1 * lastItemStory.getOfferChildrenAmount(), -1 * lastItemStory.getPrice(), null));
        itemStoryDao.saveAll(itemStoryMap.values());
        deleteChildren(item);
    }

    /**
     * Рекурсивный метод для удаления детей.
     * @param item
     */
    private void deleteChildren(Item item) {
        item.getChildren().forEach(this::deleteChildren);
        itemStoryDao.deleteAll(item.getItemStories());
        itemDao.delete(item);
    }

    /**
     * Метод для сбора информации о товарах, у которых цена обновлялась в последние 24 часа.
     * @param date
     * @return
     */
    public SalesOutDto getSales(OffsetDateTime date) {
        List<Item> offers = itemDao.getItemsUpdatedForDayBefore(date);
        SalesOutDto salesOutDto = new SalesOutDto();
        salesOutDto.setItems(offers.stream()
                .map(this::getSalesItemDto)
                .collect(Collectors.toList()));
        return salesOutDto;
    }

    /**
     * Метод для маппинга данных в dto.
     * @param item
     * @return
     */
    private SalesItemOutDto getSalesItemDto(Item item) {
        ItemStory itemStory = itemStoryDao.findFirstByItemOrderByDateUpdatedDesc(item).orElseThrow(StoreException::new);
        SalesItemOutDto salesItemOutDto = new SalesItemOutDto();
        salesItemOutDto.setId(item.getId());
        salesItemOutDto.setName(itemStory.getName());
        Integer price = itemStory.getPrice();
        if (price != -1) {
            salesItemOutDto.setPrice(price);
        }
        salesItemOutDto.setType(item.getType());
        Optional.ofNullable(item.getParent()).ifPresent(p -> salesItemOutDto.setParentId(p.getId()));
        salesItemOutDto.setDate(itemStory.getDateUpdated());
        return salesItemOutDto;
    }

    /**
     * Метод для получения статистики по объекту за период между dateStart и dateEnd.
     * @param itemId
     * @param dateStartOpt
     * @param dateEndOpt
     * @return
     */
    public SalesOutDto getStatisticForItem(UUID itemId, Optional<OffsetDateTime> dateStartOpt, Optional<OffsetDateTime> dateEndOpt) {
        List<ItemStory> stories = itemStoryDao.getAllItemStories(itemId, dateStartOpt.orElse(null), dateEndOpt.orElse(null));
        SalesOutDto dto = new SalesOutDto();
        if (!stories.isEmpty()) {
            Item item = stories.get(0).getItem();
            dto.setItems(stories.stream().map(story -> mapItemStories(story, itemId, item.getType())).collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * Метод для маппинга данных в dto.
     * @param itemStory
     * @param itemId
     * @param itemType
     * @return
     */
    private SalesItemOutDto mapItemStories(ItemStory itemStory, UUID itemId, ShopUnitTypeEnum itemType) {
        SalesItemOutDto salesItemOutDto = new SalesItemOutDto();
        salesItemOutDto.setId(itemId);
        salesItemOutDto.setName(itemStory.getName());
        if (itemStory.getOfferChildrenAmount() != 0) {
            salesItemOutDto.setPrice(itemStory.getPrice() / itemStory.getOfferChildrenAmount());
        } else {
            salesItemOutDto.setPrice(null);
        }
        salesItemOutDto.setType(itemType);
        salesItemOutDto.setParentId(itemStory.getParentId());
        salesItemOutDto.setDate(itemStory.getDateUpdated());
        return salesItemOutDto;
    }
}
