-- changeset Ari: 007 create sale table
create table sales
(
    id           long auto_increment primary key not null,
    product_id   long                            not null,
    quantity     int                             not null,
    cashier_id   long                            not null,
    total_amount decimal(10, 2)                  not null,
    sale_date    timestamp                       not null default current_timestamp,

    constraint fk_sales_product
        foreign key (product_id)
            references products (id)
            on delete restrict
            on update cascade,

    constraint fk_sales_cashier
        foreign key (cashier_id)
            references users (id)
            on delete restrict
            on update cascade
)