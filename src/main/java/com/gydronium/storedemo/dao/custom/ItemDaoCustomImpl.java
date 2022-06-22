package com.gydronium.storedemo.dao.custom;

import com.gydronium.storedemo.model.Item;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class ItemDaoCustomImpl implements ItemDaoCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Item> getItemsUpdatedForDayBefore(OffsetDateTime dateTime) {
        return entityManager.createQuery("SELECT item " +
                        "                    FROM Item AS item " +
                        "                      JOIN FETCH item.itemStories AS story " +
                        "                    WHERE story.dateUpdated = (SELECT MAX (story.dateUpdated) from story where story.item = item)" +
                                "                AND story.dateUpdated <= :dateTime" +
                                "                AND story.dateUpdated >= :dayBeforeDateTime" +
                                "                AND item.type = 'OFFER'"
                        , Item.class)
                .setParameter("dateTime", dateTime)
                .setParameter("dayBeforeDateTime", dateTime.minusDays(1))
                .getResultList();
    }
}
