-- changeset Ari: 015 add initial data for purchase table

-- Закупки (purchases) - 15 записей
INSERT INTO purchases (product_id, quantity, stock_keeper_id, purchase_date, total_cost)
VALUES ((SELECT id FROM products WHERE name = 'Молоко 2.5% 1л'), 50,
        (SELECT id FROM users WHERE email = 'kate@company.com'),
        DATEADD('DAY', -30, CURRENT_TIMESTAMP()), 2750.00),

       ((SELECT id FROM products WHERE name = 'Хлеб белый'), 70,
        (SELECT id FROM users WHERE email = 'kate@company.com'),
        DATEADD('DAY', -28, CURRENT_TIMESTAMP()), 1750.00),

       ((SELECT id FROM products WHERE name = 'Сыр Российский 300г'), 30,
        (SELECT id FROM users WHERE email = 'dmitry@company.com'),
        DATEADD('DAY', -25, CURRENT_TIMESTAMP()), 5400.00),

       ((SELECT id FROM products WHERE name = 'Колбаса вареная 500г'), 25,
        (SELECT id FROM users WHERE email = 'nikolay@company.com'),
        DATEADD('DAY', -24, CURRENT_TIMESTAMP()), 5250.00),

       ((SELECT id FROM products WHERE name = 'Вода минеральная 0.5л'), 100,
        (SELECT id FROM users WHERE email = 'kate@company.com'),
        DATEADD('DAY', -22, CURRENT_TIMESTAMP()), 2000.00),

       ((SELECT id FROM products WHERE name = 'Сок яблочный 1л'), 45,
        (SELECT id FROM users WHERE email = 'dmitry@company.com'),
        DATEADD('DAY', -20, CURRENT_TIMESTAMP()), 3150.00),

       ((SELECT id FROM products WHERE name = 'Кока-кола 1.5л'), 60,
        (SELECT id FROM users WHERE email = 'kate@company.com'),
        DATEADD('DAY', -18, CURRENT_TIMESTAMP()), 3900.00),

       ((SELECT id FROM products WHERE name = 'Порошок стиральный 3кг'), 20,
        (SELECT id FROM users WHERE email = 'nikolay@company.com'),
        DATEADD('DAY', -15, CURRENT_TIMESTAMP()), 7000.00),

       ((SELECT id FROM products WHERE name = 'Средство для мытья посуды 500мл'), 30,
        (SELECT id FROM users WHERE email = 'kate@company.com'),
        DATEADD('DAY', -12, CURRENT_TIMESTAMP()), 3600.00),

       ((SELECT id FROM products WHERE name = 'Шампунь 400мл'), 35,
        (SELECT id FROM users WHERE email = 'dmitry@company.com'),
        DATEADD('DAY', -10, CURRENT_TIMESTAMP()), 5250.00),

       ((SELECT id FROM products WHERE name = 'Зубная паста 75мл'), 40,
        (SELECT id FROM users WHERE email = 'kate@company.com'),
        DATEADD('DAY', -8, CURRENT_TIMESTAMP()), 3200.00),

       ((SELECT id FROM products WHERE name = 'Батарейки AA 4шт'), 25,
        (SELECT id FROM users WHERE email = 'nikolay@company.com'),
        DATEADD('DAY', -7, CURRENT_TIMESTAMP()), 2750.00),

       ((SELECT id FROM products WHERE name = 'Ручка шариковая'), 100,
        (SELECT id FROM users WHERE email = 'dmitry@company.com'),
        DATEADD('DAY', -5, CURRENT_TIMESTAMP()), 1500.00),

       ((SELECT id FROM products WHERE name = 'Тетрадь 48л'), 80,
        (SELECT id FROM users WHERE email = 'kate@company.com'),
        DATEADD('DAY', -3, CURRENT_TIMESTAMP()), 2400.00),

       ((SELECT id FROM products WHERE name = 'Пакеты для мусора 30л 30шт'), 50,
        (SELECT id FROM users WHERE email = 'nikolay@company.com'),
        DATEADD('DAY', -1, CURRENT_TIMESTAMP()), 4500.00);

