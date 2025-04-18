-- changeset Ari: 005 create stock table
create table stock
(
    product_id long                            not null primary key,
    quantity   int                             not null default 0,
    created_at timestamp                       not null default current_timestamp,
    updated_at timestamp                       not null default current_timestamp,

    constraint fk_stock_product
        foreign key (product_id)
            references products (id)
            on delete restrict
            on update cascade
)