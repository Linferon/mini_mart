-- changeset Ari: 011 create monthly budget table
create table monthly_budgets
(
    id               long auto_increment primary key not null,
    budget_date      date                            not null,
    planned_income   decimal(10, 2)                  not null default 0.00,
    planned_expenses decimal(10, 2)                  not null default 0.00,
    actual_income    decimal(10, 2)                  not null default 0.00,
    actual_expenses  decimal(10, 2)                  not null default 0.00,
    net_result       decimal(10, 2) as (actual_income - actual_expenses),
    created_at       timestamp                       not null default current_timestamp,
    updated_at       timestamp                       not null default current_timestamp,
    director_id      long                            not null,

    constraint fk_director_id
        foreign key (director_id)
            references users (id)
            on delete restrict
            on update cascade
)