package com.gydronium.storedemo.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Сущность, предназначенная для хранения изменяемых полей item'а.
 * Используется для вывода статистики по товарам и категориям.
 */
@Entity
@Getter
@Setter
@Table(name = "item_story")
public class ItemStory {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date_updated", nullable = false)
    private OffsetDateTime dateUpdated;

    /**
     * Это поле хранит цену товара или категории. Если тип объекта Категория, то здесь хранится суммарная сумма цен детей.
     * Используется для отслеживания цены товара и средней цены категории.
     */
    @Column(name = "price")
    private Integer price;

    @Column(name = "parent_id")
    private UUID parentId;

    @JoinColumn(name = "item_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    private Item item;

    /**
     * Это поле хранит хранит количество детей типа OFFER. У объекта типа OFFER это поле равно 1.
     * Используется для отслеживания цены товара и средней цены категории.
     */
    @NotNull(message = "Поле offer_children_amount не может быть равно null")
    @Column(name = "offer_children_amount")
    private Integer offerChildrenAmount;

    @Override
    public String toString() {
        return "ItemStory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateUpdated=" + dateUpdated +
                ", price=" + price +
                ", parentId=" + parentId +
                ", item=" + item.getId() +
                ", offerChildrenAmount=" + offerChildrenAmount +
                '}';
    }
}
