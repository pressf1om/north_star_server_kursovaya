ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(255);

INSERT INTO users (email, username, password, status)
VALUES (
    'admin@northstar.ru',
    'admin',
    '63d6861ad43d73d9085f5ef0fbffac5a:cf1d0cdae87e3e9b9ab7ead209babbb2ef1997bbc940ec1c23dffa36c94cd3df',
    'Администратор'
)
ON CONFLICT (username) DO NOTHING;
