-- changeset Ari: 014 add initial data for user table

-- Продукты (products) - 15 записей
INSERT INTO products (name, category_id, buy_price, sell_price)
VALUES ('Молоко 2.5% 1л', (SELECT id FROM product_categories WHERE name = 'Продукты питания'), 55.00, 75.50),
       ('Хлеб белый', (SELECT id FROM product_categories WHERE name = 'Продукты питания'), 25.00, 35.00),
       ('Сыр Российский 300г', (SELECT id FROM product_categories WHERE name = 'Продукты питания'), 180.00, 240.00),
       ('Колбаса вареная 500г', (SELECT id FROM product_categories WHERE name = 'Продукты питания'), 210.00, 280.00),
       ('Вода минеральная 0.5л', (SELECT id FROM product_categories WHERE name = 'Напитки'), 20.00, 35.00),
       ('Сок яблочный 1л', (SELECT id FROM product_categories WHERE name = 'Напитки'), 70.00, 95.00),
       ('Кока-кола 1.5л', (SELECT id FROM product_categories WHERE name = 'Напитки'), 65.00, 90.00),
       ('Порошок стиральный 3кг', (SELECT id FROM product_categories WHERE name = 'Товары для дома'), 350.00, 450.00),
       ('Средство для мытья посуды 500мл', (SELECT id FROM product_categories WHERE name = 'Товары для дома'), 120.00,
        160.00),
       ('Шампунь 400мл', (SELECT id FROM product_categories WHERE name = 'Личная гигиена'), 150.00, 200.00),
       ('Зубная паста 75мл', (SELECT id FROM product_categories WHERE name = 'Личная гигиена'), 80.00, 110.00),
       ('Батарейки AA 4шт', (SELECT id FROM product_categories WHERE name = 'Электроника'), 110.00, 160.00),
       ('Ручка шариковая', (SELECT id FROM product_categories WHERE name = 'Канцтовары'), 15.00, 25.00),
       ('Тетрадь 48л', (SELECT id FROM product_categories WHERE name = 'Канцтовары'), 30.00, 45.00),
       ('Пакеты для мусора 30л 30шт', (SELECT id FROM product_categories WHERE name = 'Прочее'), 90.00, 120.00);

-- Склад (stock) - заполнение для всех продуктов
INSERT INTO stock (product_id, quantity)
SELECT id, FLOOR(RAND() * 100) + 20
FROM products;