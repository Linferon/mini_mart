-- changeset Ari: 006 create purchase table

create table purchases
(
    id              long auto_increment primary key not null,
    product_id      long                            not null,
    quantity        int                             not null,
    stock_keeper_id long                            not null,
    purchase_date   timestamp                       not null default current_timestamp,
    total_cost      decimal(10, 2)                  not null,

    constraint fk_pur_product_id
        foreign key (product_id)
            references products (id)
            on delete restrict
            on update cascade,

    constraint fk_del_stock_keeper
        foreign key (stock_keeper_id)
            references users (id)
            on delete restrict
            on update cascade
)