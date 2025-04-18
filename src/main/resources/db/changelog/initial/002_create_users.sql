-- changeset Ari: 002 create user table
create table users
(
    id         long auto_increment primary key not null,
    name       varchar(50)                     not null,
    surname    varchar(50),
    email      varchar(255) unique             not null,
    password   varchar(255)                    not null,
    enabled    boolean                         not null default true,
    role_id    long                            not null,
    created_at timestamp                       not null default current_timestamp,
    updated_at timestamp                       not null default current_timestamp,

    constraint fk_user_role
        foreign key (role_id)
            references roles(id)
            on delete restrict
            on update cascade
)