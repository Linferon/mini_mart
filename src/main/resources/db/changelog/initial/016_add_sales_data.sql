-- changeset Ari: 016 add initial data for  table

-- Продажи (sales) - 20 записей
INSERT INTO sales (product_id, quantity, cashier_id, total_amount, sale_date)
VALUES ((SELECT id FROM products WHERE name = 'Молоко 2.5% 1л'), 3,
        (SELECT id FROM users WHERE email = 'alex@company.com'),
        226.50, DATEADD('DAY', -25, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Хлеб белый'), 2,
        (SELECT id FROM users WHERE email = 'anna@company.com'),
        70.00, DATEADD('DAY', -25, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Сыр Российский 300г'), 1,
        (SELECT id FROM users WHERE email = 'olga@company.com'),
        240.00, DATEADD('DAY', -24, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Колбаса вареная 500г'), 2,
        (SELECT id FROM users WHERE email = 'alex@company.com'),
        560.00, DATEADD('DAY', -23, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Вода минеральная 0.5л'), 5,
        (SELECT id FROM users WHERE email = 'anna@company.com'),
        175.00, DATEADD('DAY', -22, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Сок яблочный 1л'), 2,
        (SELECT id FROM users WHERE email = 'olga@company.com'),
        190.00, DATEADD('DAY', -21, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Кока-кола 1.5л'), 3,
        (SELECT id FROM users WHERE email = 'alex@company.com'),
        270.00, DATEADD('DAY', -20, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Порошок стиральный 3кг'), 1,
        (SELECT id FROM users WHERE email = 'anna@company.com'),
        450.00, DATEADD('DAY', -19, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Средство для мытья посуды 500мл'), 2,
        (SELECT id FROM users WHERE email = 'olga@company.com'),
        320.00, DATEADD('DAY', -18, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Шампунь 400мл'), 1,
        (SELECT id FROM users WHERE email = 'alex@company.com'),
        200.00, DATEADD('DAY', -17, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Зубная паста 75мл'), 3,
        (SELECT id FROM users WHERE email = 'anna@company.com'),
        330.00, DATEADD('DAY', -16, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Батарейки AA 4шт'), 2,
        (SELECT id FROM users WHERE email = 'olga@company.com'),
        320.00, DATEADD('DAY', -15, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Ручка шариковая'), 5,
        (SELECT id FROM users WHERE email = 'alex@company.com'),
        125.00, DATEADD('DAY', -14, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Тетрадь 48л'), 3,
        (SELECT id FROM users WHERE email = 'anna@company.com'),
        135.00, DATEADD('DAY', -13, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Пакеты для мусора 30л 30шт'), 2,
        (SELECT id FROM users WHERE email = 'olga@company.com'),
        240.00, DATEADD('DAY', -12, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Молоко 2.5% 1л'), 4,
        (SELECT id FROM users WHERE email = 'alex@company.com'),
        302.00, DATEADD('DAY', -10, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Хлеб белый'), 3,
        (SELECT id FROM users WHERE email = 'anna@company.com'),
        105.00, DATEADD('DAY', -8, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Сыр Российский 300г'), 2,
        (SELECT id FROM users WHERE email = 'olga@company.com'),
        480.00, DATEADD('DAY', -6, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Кока-кола 1.5л'), 4,
        (SELECT id FROM users WHERE email = 'alex@company.com'),
        360.00, DATEADD('DAY', -4, CURRENT_TIMESTAMP())),

       ((SELECT id FROM products WHERE name = 'Шампунь 400мл'), 2,
        (SELECT id FROM users WHERE email = 'anna@company.com'),
        400.00, DATEADD('DAY', -2, CURRENT_TIMESTAMP()));
