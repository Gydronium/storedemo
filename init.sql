CREATE TYPE "shop_unit_type" AS ENUM('CATEGORY', 'OFFER');

create table item
(
    id uuid primary key,
    type shop_unit_type not null,
    parent_id uuid,
    constraint item_parent_id_fk foreign key (parent_id) references item(id)
    match simple on update no action on delete cascade;
);

create index item_parent_id_index on item(parent_id);

create table item_story
(
    id uuid primary key,
    name varchar not null,
    date_updated timestamp with time zone not null,
    price integer,
    parent_id uuid,
    item_id uuid not null,
    offer_children_amount  integer not null default value 0,
    constraint item_story_item_id_fk foreign key (item_id) references item(id)
    match simple on update no action on delete cascade;
);

create index item_story_item_id_index on item_story(item_id);
create index item_story_date_updated_index on item_story(date_updated);