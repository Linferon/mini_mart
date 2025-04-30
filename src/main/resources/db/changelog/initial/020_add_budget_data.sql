-- changeset Ari: 020 add initial data for monthly budget table

-- Бюджеты (monthly_budgets) - 10 записей
INSERT INTO monthly_budgets (budget_date, planned_income, planned_expenses, actual_income, actual_expenses, director_id)
VALUES (PARSEDATETIME('01.07.2024', 'dd.MM.yyyy'), 400000.00, 350000.00, 420000.00, 340000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.08.2024', 'dd.MM.yyyy'), 410000.00, 360000.00, 405000.00, 370000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.09.2024', 'dd.MM.yyyy'), 420000.00, 365000.00, 428000.00, 355000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.10.2024', 'dd.MM.yyyy'), 430000.00, 370000.00, 440000.00, 360000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.11.2024', 'dd.MM.yyyy'), 450000.00, 380000.00, 455000.00, 372000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.12.2024', 'dd.MM.yyyy'), 500000.00, 410000.00, 515000.00, 395000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.01.2025', 'dd.MM.yyyy'), 460000.00, 390000.00, 450000.00, 400000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.02.2025', 'dd.MM.yyyy'), 470000.00, 395000.00, 480000.00, 380000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.03.2025', 'dd.MM.yyyy'), 480000.00, 400000.00, 490000.00, 390000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.04.2025', 'dd.MM.yyyy'), 490000.00, 405000.00, 500000.00, 400000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com')),

       (PARSEDATETIME('01.05.2025', 'dd.MM.yyyy'), 500000.00, 405000.00, 500000.00, 400000.00,
        (SELECT id FROM users WHERE email = 'ivan@company.com'));