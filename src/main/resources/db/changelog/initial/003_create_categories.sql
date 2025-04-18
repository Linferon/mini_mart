-- changelog Ari: 003 create product, expenses and source categories

-- product categories
create table product_categories
(
    id   long auto_increment primary key not null,
    name varchar(50)                     not null unique
);

-- expenses categories
create table expense_categories
(
    id   long auto_increment primary key not null,
    name varchar(50)                     not null unique
);

-- income sources
create table income_sources
(
    id   long auto_increment primary key not null,
    name varchar(50)                     not null unique
)