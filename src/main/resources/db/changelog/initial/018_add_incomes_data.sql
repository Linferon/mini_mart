-- changeset Ari: 017 add initial data for income table

-- Доходы (incomes) - 10 записей
INSERT INTO incomes (source_id, total_amount, income_date, accountant_id)
VALUES ((SELECT id FROM income_sources WHERE name = 'Продажи'),
        120000.00,
        DATEADD('DAY', -30, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Инвестиции'),
        50000.00,
        DATEADD('DAY', -27, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'sergey@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Продажи'),
        135000.00,
        DATEADD('DAY', -25, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'elena@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Гранты'),
        200000.00,
        DATEADD('DAY', -20, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Продажи'),
        118000.00,
        DATEADD('DAY', -18, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'sergey@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Прочее'),
        15000.00,
        DATEADD('DAY', -15, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'elena@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Продажи'),
        142000.00,
        DATEADD('DAY', -10, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Инвестиции'),
        75000.00,
        DATEADD('DAY', -8, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'sergey@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Продажи'),
        128000.00,
        DATEADD('DAY', -5, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'elena@company.com')),

       ((SELECT id FROM income_sources WHERE name = 'Прочее'),
        12000.00,
        DATEADD('DAY', -2, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com'));
