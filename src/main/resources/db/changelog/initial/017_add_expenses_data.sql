-- changeset Ari: 017 add initial data for expense table

-- Расходы (expenses) - 10 записей
INSERT INTO expenses (category_id, total_amount, expense_date, accountant_id)
VALUES ((SELECT id FROM expense_categories WHERE name = 'Аренда'),
        60000.00,
        DATEADD('DAY', -30, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Коммунальные услуги'),
        12500.00,
        DATEADD('DAY', -28, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'sergey@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Заработная плата'),
        250000.00,
        DATEADD('DAY', -25, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'elena@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Покупка товара'),
        45000.00,
        DATEADD('DAY', -20, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Налоги'),
        35000.00,
        DATEADD('DAY', -18, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'sergey@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Прочее'),
        8500.00,
        DATEADD('DAY', -15, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'elena@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Коммунальные услуги'),
        13200.00,
        DATEADD('DAY', -10, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Покупка товара'),
        38000.00,
        DATEADD('DAY', -8, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'sergey@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Прочее'),
        7200.00,
        DATEADD('DAY', -5, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'elena@company.com')),

       ((SELECT id FROM expense_categories WHERE name = 'Налоги'),
        18500.00,
        DATEADD('DAY', -2, CURRENT_TIMESTAMP()),
        (SELECT id FROM users WHERE email = 'maria@company.com'));

