# north-star-server

Ktor-сервер для мобильного приложения North Star (автопарк, рейсы, диспетчерская).

## Требования

- JDK 17+
- Docker (для PostgreSQL)

## База данных

```bash
docker compose up -d
```

| Параметр | Значение |
| --- | --- |
| Хост | `localhost` |
| Порт | `5434` |
| База | `northstar` |
| Пользователь | `northstar_user` |
| Пароль | `northstar_password` |

Порт `5434` выбран, чтобы не пересекаться с другими проектами на `5433`.

## Запуск API

```bash
.\gradlew.bat :server:run
```

При старте Flyway накатывает миграции из `server/src/main/resources/db/migration` (отключить: `FLYWAY_MIGRATE=false`).

Проверка:

```bash
curl http://127.0.0.1:8080/
```

Ожидается JSON: `{"status":"ok","service":"north-star-server"}`.

Проверка таблиц:

```bash
docker exec -it north-star-db psql -U northstar_user -d northstar -c "\dt"
```

## Авторизация

- `POST /api/auth/register` — логин, почта, пароль → статус «Ожидает»
- `POST /api/auth/login` — логин и пароль
- `GET /api/auth/me` — профиль (JWT)
- `POST /api/auth/logout`

Демо-админ (после миграции V2): логин `admin`, пароль `admin123`.
