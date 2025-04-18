-- changeset Ari: 008 create expense table
create table expenses
(
    id            long auto_increment primary key not null,
    category_id   long                            not null,
    total_amount  decimal(10, 2)                  not null,
    expense_date  timestamp                       not null default current_timestamp,
    accountant_id long                            not null,

    constraint fk_expense_category
        foreign key (category_id)
            references expense_categories (id)
            on delete restrict
            on update cascade,

    constraint fk_expense_accountant
        foreign key (accountant_id)
            references users (id)
            on delete restrict
            on update cascade
)