-- changeset Ari: 013 add initial data for user table

-- Пользователи (users) - 10 записей
INSERT INTO users (name, surname, email, password, role_id)
VALUES ('Иван', 'Иванов', 'ivan@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Директор')),
       ('Мария', 'Петрова', 'maria@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Бухгалтер')),
       ('Алексей', 'Смирнов', 'alex@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Кассир')),
       ('Екатерина', 'Козлова', 'kate@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Кладовщик')),
       ('Сергей', 'Новиков', 'sergey@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Бухгалтер')),
       ('Анна', 'Морозова', 'anna@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Кассир')),
       ('Дмитрий', 'Волков', 'dmitry@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Кладовщик')),
       ('Ольга', 'Соколова', 'olga@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Кассир')),
       ('Николай', 'Васильев', 'nikolay@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Кладовщик')),
       ('Елена', 'Попова', 'elena@company.com', 'qwerty', (SELECT id FROM roles WHERE name = 'Бухгалтер'));