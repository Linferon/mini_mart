-- changeset Ari: 009 create income table
create table incomes
(
    id            long auto_increment primary key not null,
    source_id     long                            not null,
    total_amount  decimal(10, 2)                  not null,
    income_date   timestamp                       not null default current_timestamp,
    accountant_id long                            not null,

    constraint fk_income_source
        foreign key (source_id)
            references income_sources (id)
            on delete restrict
            on update cascade,

    constraint fk_income_accountant
        foreign key (accountant_id)
            references users (id)
            on delete restrict
            on update cascade
)