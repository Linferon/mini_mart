-- changeset Ari: 010 create payroll table
create table payrolls
(
    id            long auto_increment primary key not null,
    employee_id   long                            not null,
    accountant_id long                            not null,
    hours_worked  real                            not null,
    hourly_rate   decimal(10, 2)                  not null,
    total_amount  decimal(10, 2) as (hours_worked * hourly_rate),
    period_start  date                            not null,
    period_end    date,
    payment_date  date,
    is_paid       boolean                         not null default false,
    created_at    timestamp                       not null default current_timestamp,
    updated_at    timestamp                       not null default current_timestamp,

    constraint fk_employee_id
        foreign key (employee_id)
            references users (id)
            on delete restrict
            on update cascade,

    constraint fk_accountant_id
        foreign key (accountant_id)
            references users (id)
            on delete restrict
            on update cascade
)