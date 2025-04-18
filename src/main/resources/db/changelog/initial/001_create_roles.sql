-- changeset Ari: 001 create role table
create table roles
(
    id   long auto_increment primary key not null,
    name varchar(45)                     not null unique
)