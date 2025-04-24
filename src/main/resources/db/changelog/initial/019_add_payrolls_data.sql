-- changeset Ari: 019 add initial data for payroll table

-- Зарплаты (payrolls) - 10 записей
INSERT INTO payrolls (employee_id, accountant_id, hours_worked, hourly_rate, period_start, period_end, payment_date,
                      is_paid)
VALUES ((SELECT id FROM users WHERE email = 'alex@company.com'),
        (SELECT id FROM users WHERE email = 'maria@company.com'),
        160.0, 200.00,
        PARSEDATETIME('01.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('31.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('05.04.2025', 'dd.MM.yyyy'), true),

       ((SELECT id FROM users WHERE email = 'kate@company.com'),
        (SELECT id FROM users WHERE email = 'sergey@company.com'),
        168.0, 180.00,
        PARSEDATETIME('01.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('31.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('05.04.2025', 'dd.MM.yyyy'), true),

       ((SELECT id FROM users WHERE email = 'anna@company.com'),
        (SELECT id FROM users WHERE email = 'elena@company.com'),
        160.0, 190.00,
        PARSEDATETIME('01.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('31.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('05.04.2025', 'dd.MM.yyyy'), true),

       ((SELECT id FROM users WHERE email = 'dmitry@company.com'),
        (SELECT id FROM users WHERE email = 'maria@company.com'),
        176.0, 170.00,
        PARSEDATETIME('01.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('31.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('05.04.2025', 'dd.MM.yyyy'), true),

       ((SELECT id FROM users WHERE email = 'olga@company.com'),
        (SELECT id FROM users WHERE email = 'sergey@company.com'),
        152.0, 195.00,
        PARSEDATETIME('01.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('31.03.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('05.04.2025', 'dd.MM.yyyy'), true),

       ((SELECT id FROM users WHERE email = 'alex@company.com'),
        (SELECT id FROM users WHERE email = 'elena@company.com'),
        168.0, 200.00,
        PARSEDATETIME('01.04.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('30.04.2025', 'dd.MM.yyyy'),
        null, false),

       ((SELECT id FROM users WHERE email = 'kate@company.com'),
        (SELECT id FROM users WHERE email = 'maria@company.com'),
        160.0, 180.00,
        PARSEDATETIME('01.04.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('30.04.2025', 'dd.MM.yyyy'),
        null, false),

       ((SELECT id FROM users WHERE email = 'anna@company.com'),
        (SELECT id FROM users WHERE email = 'sergey@company.com'),
        170.0, 190.00,
        PARSEDATETIME('01.04.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('30.04.2025', 'dd.MM.yyyy'),
        null, false),

       ((SELECT id FROM users WHERE email = 'dmitry@company.com'),
        (SELECT id FROM users WHERE email = 'elena@company.com'),
        176.0, 170.00,
        PARSEDATETIME('01.04.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('30.04.2025', 'dd.MM.yyyy'),
        null, false),

       ((SELECT id FROM users WHERE email = 'olga@company.com'),
        (SELECT id FROM users WHERE email = 'maria@company.com'),
        162.0, 195.00,
        PARSEDATETIME('01.04.2025', 'dd.MM.yyyy'),
        PARSEDATETIME('30.04.2025', 'dd.MM.yyyy'),
        null, false);
