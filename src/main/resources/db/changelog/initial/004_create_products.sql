-- changeset Ari: 004 create product table
create table products
(
    id          long auto_increment primary key not null,
    name        varchar(100)                    not null,
    category_id long                            not null,
    buy_price   decimal(10, 2)                  not null,
    sell_price  decimal(10, 2)                  not null,
    created_at  timestamp                       not null default current_timestamp,
    updated_at  timestamp                       not null default current_timestamp,

    constraint fk_prod_category_id
        foreign key (category_id)
            references product_categories (id)
            on delete restrict
            on update cascade
)