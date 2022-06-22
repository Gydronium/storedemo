package com.gydronium.storedemo.model;

import com.gydronium.storedemo.util.ShopUnitTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

/**
 * В этой сущности хранятся поля, которые не могут измениться в ходе обновления.
 */
@Entity
@Getter
@Setter
@Table(name = "item")
public class Item {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ShopUnitTypeEnum type;

    /**
     * Поле parent хранит текущего родителя объекта.
     * Поле parent нужно, чтобы было легко найти детей у любого item'а.
     */
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Item parent;

    /**
     * Коллекция детей item привязанных к этому item'у.
     */
    @OneToMany(mappedBy = "parent")
    private Set<Item> children;

    /**
     * Коллекция сущностей item_story привязанных к этому item'у.
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemStory> itemStories;

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", type=" + type +
                ", parent=" + (parent != null ? parent.getId() : "null") +
                '}';
    }
}
