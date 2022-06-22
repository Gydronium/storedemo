package com.gydronium.storedemo.dao.custom;

import com.gydronium.storedemo.model.ItemStory;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ItemStoryDaoCustomImpl implements ItemStoryDaoCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ItemStory> getAllItemStories(UUID itemId, OffsetDateTime dateStart, OffsetDateTime dateEnd) {

        String query = "SELECT story " +
                "            FROM ItemStory AS story " +
                "                JOIN story.item AS item " +
                "            WHERE item.id = :itemId";

        if (dateStart != null) {
            query += "  AND story.dateUpdated >= :dateStart ";
        }
        if (dateEnd != null) {
            query += "  AND story.dateUpdated < :dateEnd ";
        }

        Query query1  = entityManager.createQuery(query, ItemStory.class).setParameter("itemId", itemId);
        if (dateStart != null) {
            query1.setParameter("dateStart", dateStart);
        }
        if (dateEnd != null) {
            query1.setParameter("dateEnd", dateEnd);
        }
        return query1.getResultList();
    }

}
