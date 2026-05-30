# User Bank Service

Тестовое задание: сервис пользователей и счетов с переводами денег, периодическим
начислением процентов, поиском и JWT-аутентификацией.

## Стек

- **Java 11** (компиляция с `--release 11`)
- **Spring Boot 2.7** (последняя ветка с поддержкой Java 11): Web, Data JPA, Security, Validation, Cache
- **PostgreSQL** + **Liquibase** (миграции схемы и сид-данных)
- **Caffeine** — лёгкий встроенный кэш (в отличие от Redis не требует отдельного сервера)
- **JJWT** — выпуск/проверка JWT
- **springdoc-openapi** — Swagger UI
- **Maven** — сборка
- **JUnit 5 + Mockito + Testcontainers + MockMvc** — тесты

## Архитектура

Три слоя:

- **API** — `controller/*` (REST, валидация входных данных, Swagger-аннотации)
- **Service** — `service/*` (бизнес-логика, транзакции, кэширование)
- **DAO** — `repository/*` (Spring Data JPA, спецификации поиска, блокировки)

Периодическое начисление вынесено в отдельные пакеты: `scheduler/*` (планировщик, раз в 30с)
и `processor/*` (`BalanceAccrualProcessor` — начисление на один счёт под блокировкой).

## Структура БД

| Таблица | Поля |
|---|---|
| `users` | `id`, `name` (VARCHAR 500), `date_of_birth`, `password` (BCrypt) |
| `account` | `id`, `user_id` (UNIQUE, FK), `balance` (DECIMAL 19,2), `initial_balance` |
| `email_data` | `id`, `user_id` (FK), `email` (UNIQUE, VARCHAR 200) |
| `phone_data` | `id`, `user_id` (FK), `phone` (UNIQUE, VARCHAR 13) |

> Поле `initial_balance` добавлено сверх ТЗ — нужно, чтобы ограничивать начисление 207% от
> первоначального депозита. На уровне БД действует CHECK-ограничение `balance >= 0`.

Пользователи создаются **миграцией** (`db/changelog/changes/002-seed-data.yaml`), отдельной
операции создания пользователя нет — как и требует задание.

## Запуск

### 1. Поднять PostgreSQL

```bash
docker compose up -d postgres
```

(или используйте свой PostgreSQL и переопределите `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

### 2. Запустить приложение

```bash
mvn spring-boot:run
```

Приложение поднимется на `http://localhost:8080`. Liquibase сам накатит схему и демо-данные.

> Перед первым запуском создайте файл секретов — см. раздел [«Секреты и подключение к БД»](#секреты-и-подключение-к-бд).

- **Swagger UI:** http://localhost:8080/swagger-ui.html

### Профили

По умолчанию активен профиль **`dev`** (`spring.profiles.active` в `application.yml`). В него
вынесены параметры, которые обычно меняют/тюнят между окружениями:

- уровни логирования (`DEBUG` для кода приложения и SQL Hibernate);
- вывод SQL (`spring.jpa.show-sql`, `format_sql`);
- настройки кэша Caffeine (`maximumSize`, `expireAfterWrite`);
- параметры начисления процентов (`accrual-rate`, `max-multiplier`, интервал планировщика);
- время жизни JWT (`expiration-ms`).

Общий (профиле-независимый) блок содержит инфраструктуру: имя приложения, подключение к БД
(оно уже вынесено в env/`secret.properties`), Liquibase, тип кэша, путь Swagger, секрет JWT.

Профиль переопределяется без правки файла — переменной окружения:

```bash
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

(параметры из `dev` при этом не применяются; значения по начислению/JWT берутся из дефолтов в
коде, а БД/секрет — из переменных окружения.)

### Секреты и подключение к БД

Секрет JWT и параметры подключения к БД (URL, логин, пароль) **не хранятся в репозитории**.
Источник каждого значения (по приоритету):

1. переменная окружения (`JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`);
2. соответствующее свойство из файла `src/main/resources/secret.properties` — он добавлен в
   `.gitignore` и не коммитится.

Перед локальным запуском создайте `secret.properties` из шаблона:

```bash
# из каталога src/main/resources
cp template/secret.properties.example secret.properties        # Linux/Mac
Copy-Item template/secret.properties.example secret.properties # PowerShell

# сгенерируйте ключ JWT (Base64 от 32 байт, >= 256 бит для HS256) и впишите в jwt.secret
openssl rand -base64 32
```

В шаблоне `db.*` заданы плейсхолдерами — подставьте свои значения. Для `docker-compose.yml`
по умолчанию это база `bank` и пользователь `userbank` (их же значения по умолчанию у контейнера).
В CI/проде достаточно задать переменные окружения — файл не нужен (подключение `secret.properties`
в `application.yml` помечено как `optional:`).

Контейнер и приложение можно настроить одним набором переменных: `DB_USERNAME` и `DB_PASSWORD`
читают и `docker-compose.yml`, и приложение, например:

```bash
DB_USERNAME=userbank DB_PASSWORD='psw_user*1234' POSTGRES_DB=bank docker compose up -d postgres
```

### Переменные окружения

| Переменная | Источник по умолчанию | Назначение |
|---|---|---|
| `DB_URL` | `db.url` из `secret.properties` | строка подключения к БД |
| `DB_USERNAME` / `DB_PASSWORD` | `db.username` / `db.password` из `secret.properties` | учётные данные БД |
| `JWT_SECRET` | `jwt.secret` из `secret.properties` | секрет подписи JWT (Base64) |
| `JWT_EXPIRATION_MS` | `3600000` | время жизни токена |

## Демо-пользователи (создаются миграцией)

| id | Имя | email | телефон | пароль | баланс |
|----|-----|-------|---------|--------|--------|
| 1 | Мильков Никита Дмитриевич | nikparadise@mail.ru | 89201306265, 89999999999 | `password1` | 100.00 |
| 2 | Милькова Александра Федоровна | sadovskaf@mail.ru | 79992251484 | `password2` | 250.50 |
| 3 | Americano Bill Gates | test@icloud.com, secondemail@gmail.com | 81234654500 | `password3` | 1000.00 |

> Телефоны хранятся нормализованными до цифр (колонка `VARCHAR(13)`): `+7(999)225-14-84` → `79992251484`,
> `8-123-465-45-00` → `81234654500`. Дата рождения у демо-пользователей не задана (`NULL`).

## API

### Аутентификация (публичный эндпоинт)

`POST /api/auth/login` — вход по **email+пароль** или **телефон+пароль**, возвращает JWT
(единственный значимый claim — `USER_ID`).

```bash
# по email
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"nikparadise@mail.ru","password":"password1"}'

# по телефону
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"phone":"79992251484","password":"password2"}'
```

Дальше токен передаётся в заголовке `Authorization: Bearer <token>`.

### Пользователи (чтение и поиск)

- `GET /api/users/{id}` — профиль пользователя (кэшируется).
- `GET /api/users` — поиск с пагинацией (`page`, `size`) и фильтрами:
  - `dateOfBirth` (формат `dd.MM.yyyy`) — записи, где `date_of_birth` **больше** переданной даты;
  - `phone` — точное совпадение (100%);
  - `name` — `LIKE '{name}%'`;
  - `email` — точное совпадение (100%).

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/users?name=Мильков&page=0&size=20"
```

### Свои контакты (только собственные данные)

Владелец всегда берётся из токена (`USER_ID`), а не из тела запроса.

- `GET /api/me/emails`, `POST /api/me/emails`, `PUT /api/me/emails/{id}`, `DELETE /api/me/emails/{id}`
- `GET /api/me/phones`, `POST /api/me/phones`, `PUT /api/me/phones/{id}`, `DELETE /api/me/phones/{id}`

Правила: значение нельзя занять, если оно уже используется другим пользователем; всегда
должны оставаться хотя бы один email и один телефон.

### Счёт и переводы

- `GET /api/accounts/me` — текущий баланс (всегда актуальный, без кэша).
- `POST /api/accounts/transfer` — перевод денег. Отправитель берётся из токена, получатель и
  сумма — из запроса.

```bash
curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"toUserId":2,"value":30.00}'
```

## Ключевые бизнес-правила

- **Начисление процентов.** Раз в 30 секунд баланс каждого счёта растёт на 10%, но не более
  207% от первоначального депозита. Реализовано в `BalanceAccrualScheduler` +
  `BalanceAccrualProcessor`: каждый счёт обрабатывается в своей короткой транзакции под
  пессимистичной блокировкой, поэтому начисление не конкурирует с переводами.
- **Перевод денег** (`TransferService`) — высокозначимая операция: транзакционна и
  потокобезопасна. Обе строки счетов блокируются `SELECT ... FOR UPDATE` в едином порядке
  (сначала меньший `user_id`), что исключает взаимоблокировки. Баланс не может уйти в минус —
  проверка в коде плюс CHECK-ограничение в БД.
- **Кэширование.** Профиль пользователя и результаты поиска кэшируются в Caffeine; кэш
  инвалидируется при изменении контактов. Баланс намеренно не кэшируется (меняется каждые 30с).

## Тесты

```bash
mvn test
```

- `TransferServiceTest`, `BalanceAccrualProcessorTest` — unit-тесты бизнес-логики переводов и
  начисления (включая потолок 207% и порядок блокировок).
- `UserApiIntegrationTest` — интеграционный тест через **Testcontainers (PostgreSQL) + MockMvc**:
  логин, поиск, перевод денег между пользователями, проверка ошибок.

> Для интеграционного теста нужен запущенный **Docker** (Testcontainers поднимает PostgreSQL).

## Git-хук: авто-дата в шапке копирайта

В репозитории есть pre-commit хук [`.githooks/pre-commit`](.githooks/pre-commit): при коммите он
проставляет текущую дату в строку `Last edited:` в шапке каждого изменённого `.java`-файла и
пере-индексирует его. Хук подключается через `core.hooksPath` — это **локальная** настройка, поэтому
после клонирования репозитория её нужно один раз включить:

```bash
git config core.hooksPath .githooks
```

`.gitattributes` фиксирует LF-окончания для `.githooks/**` и `*.sh`, чтобы шебанг хука не ломался на
Windows (`autocrlf`).
