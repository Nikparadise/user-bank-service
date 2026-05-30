# Трассировка требований ТЗ → код

Чеклист требований из `задание.docx`. Каждый пункт:

- **`[x]`** — требование выполнено (на GitHub отображается отмеченной галочкой), **`[ ]`** — не выполнено;
- ссылка справа — кликабельный переход к месту реализации в коде (относительный путь с номером строки; работает на GitHub и в IDE).

Все пункты ТЗ реализованы — см. ссылки.

---

## 1. Стек и тулы

- [x] База данных **PostgreSQL** — [pom.xml#L60](pom.xml#L60), [docker-compose.yml](docker-compose.yml)
- [x] Redis/Elastic «на усмотрение» → выбран лёгкий встроенный кэш **Caffeine** (не требует отдельного сервера) — [pom.xml#L55](pom.xml#L55)
- [x] **Java 11** — [pom.xml#L21](pom.xml#L21)
- [x] Имплементация на **Spring Boot** — [pom.xml#L7](pom.xml#L7)
- [x] **REST API** — [api/controller](src/main/java/home/interviewtask/userbank/api/controller)
- [x] Сборка **Maven** — [pom.xml](pom.xml#L1)

## 2. Структура таблиц БД

- [x] Таблица **USER** (PK id, name, date_of_birth, password 8–500) — [User.java#L29](src/main/java/home/interviewtask/userbank/postgresql/entity/User.java#L29), [001-init-schema.sql#L7](src/main/resources/db/changelog/changes/001-init-schema.sql#L7)
- [x] Таблица **ACCOUNT** (balance `BigDecimal`, FK user_id unique) — [Account.java#L27](src/main/java/home/interviewtask/userbank/postgresql/entity/Account.java#L27), баланс [#L38](src/main/java/home/interviewtask/userbank/postgresql/entity/Account.java#L38), [001-init-schema.sql#L17](src/main/resources/db/changelog/changes/001-init-schema.sql#L17)
- [x] Таблица **EMAIL_DATA** (email unique) — [EmailData.java#L26](src/main/java/home/interviewtask/userbank/postgresql/entity/EmailData.java#L26), [001-init-schema.sql#L30](src/main/resources/db/changelog/changes/001-init-schema.sql#L30)
- [x] Таблица **PHONE_DATA** (phone unique, формат `79207865432`) — [PhoneData.java#L26](src/main/java/home/interviewtask/userbank/postgresql/entity/PhoneData.java#L26), валидация формата [PhoneRequest.java#L13](src/main/java/home/interviewtask/userbank/api/dto/PhoneRequest.java#L13)

## 3. Общие требования к системе

- [x] **3 слоя** — API: [api/controller](src/main/java/home/interviewtask/userbank/api/controller) · service: [api/service](src/main/java/home/interviewtask/userbank/api/service) · DAO: [postgresql/repository](src/main/java/home/interviewtask/userbank/postgresql/repository)
- [x] Пользователи создаются **миграциями** (нет операции создания) — [002-seed-data.yaml#L3](src/main/resources/db/changelog/changes/002-seed-data.yaml#L3)
- [x] У пользователя **≥ 1 phone** (минимум один не удалить) — [ContactService#L132](src/main/java/home/interviewtask/userbank/api/service/ContactService.java#L132)
- [x] У пользователя **≥ 1 email** (минимум один не удалить) — [ContactService#L87](src/main/java/home/interviewtask/userbank/api/service/ContactService.java#L87)
- [x] У пользователя **строго один ACCOUNT** (FK user_id unique) — [Account.java#L27](src/main/java/home/interviewtask/userbank/postgresql/entity/Account.java#L27), [001-init-schema.sql#L17](src/main/resources/db/changelog/changes/001-init-schema.sql#L17)
- [x] Начальный **BALANCE задаётся при создании** (`initial_balance`) — [Account.java#L47](src/main/java/home/interviewtask/userbank/postgresql/entity/Account.java#L47), [002-seed-data.yaml#L3](src/main/resources/db/changelog/changes/002-seed-data.yaml#L3)
- [x] **BALANCE не уходит в минус** — проверка в переводе [TransferService#L49](src/main/java/home/interviewtask/userbank/api/service/TransferService.java#L49) + БД-constraint [001-init-schema.sql#L24](src/main/resources/db/changelog/changes/001-init-schema.sql#L24)
- [x] **Валидация входных данных** API — `@Valid` в контроллерах [ContactController#L47](src/main/java/home/interviewtask/userbank/api/controller/ContactController.java#L47) + ограничения в DTO [EmailRequest.java](src/main/java/home/interviewtask/userbank/api/dto/EmailRequest.java), [PhoneRequest.java#L13](src/main/java/home/interviewtask/userbank/api/dto/PhoneRequest.java#L13)

## 4. Обязательные фичи

- [x] **CRUD своего email** (add / change / delete) — [add #L47](src/main/java/home/interviewtask/userbank/api/controller/ContactController.java#L47) · [change #L55](src/main/java/home/interviewtask/userbank/api/controller/ContactController.java#L55) · [delete #L64](src/main/java/home/interviewtask/userbank/api/controller/ContactController.java#L64)
- [x] **CRUD своего phone** (add / change / delete) — [add #L80](src/main/java/home/interviewtask/userbank/api/controller/ContactController.java#L80) · [change #L88](src/main/java/home/interviewtask/userbank/api/controller/ContactController.java#L88) · [delete #L97](src/main/java/home/interviewtask/userbank/api/controller/ContactController.java#L97)
- [x] **Менять только своё** + значение **не занято** другим пользователем — владение [requireOwnership #L158](src/main/java/home/interviewtask/userbank/api/service/ContactService.java#L158) · уникальность [ensureEmailFree #L141](src/main/java/home/interviewtask/userbank/api/service/ContactService.java#L141), [ensurePhoneFree #L147](src/main/java/home/interviewtask/userbank/api/service/ContactService.java#L147)
- [x] **Поиск пользователей** с фильтрами + пагинацией — эндпоинт [UserController.search #L34](src/main/java/home/interviewtask/userbank/api/controller/UserController.java#L34) · сервис [UserService.search #L44](src/main/java/home/interviewtask/userbank/api/service/UserService.java#L44)
  - dateOfBirth `>` — [UserSpecifications.dateOfBirthAfter #L21](src/main/java/home/interviewtask/userbank/postgresql/repository/UserSpecifications.java#L21)
  - name `like 'text%'` — [nameStartsWith #L26](src/main/java/home/interviewtask/userbank/postgresql/repository/UserSpecifications.java#L26)
  - phone (100% совпадение) — [hasPhone #L31](src/main/java/home/interviewtask/userbank/postgresql/repository/UserSpecifications.java#L31)
  - email (100% совпадение) — [hasEmail #L39](src/main/java/home/interviewtask/userbank/postgresql/repository/UserSpecifications.java#L39)
- [x] **JWT**, единственный claim — **USER_ID** — выпуск [JwtTokenProvider.generateToken #L30](src/main/java/home/interviewtask/userbank/api/security/JwtTokenProvider.java#L30), claim [#L35](src/main/java/home/interviewtask/userbank/api/security/JwtTokenProvider.java#L35) · фильтр [JwtAuthenticationFilter #L21](src/main/java/home/interviewtask/userbank/api/security/JwtAuthenticationFilter.java#L21)
- [x] **Аутентификация** по email+password **или** phone+password — [AuthController.login #L28](src/main/java/home/interviewtask/userbank/api/controller/AuthController.java#L28) · ветвление [AuthService #L39](src/main/java/home/interviewtask/userbank/api/service/AuthService.java#L39)
- [x] **+10% каждые 30с, потолок 207%** от депозита — планировщик [BalanceAccrualScheduler #L25](src/main/java/home/interviewtask/userbank/scheduler/BalanceAccrualScheduler.java#L25) · логика [BalanceAccrualProcessor.accrueOne #L41](src/main/java/home/interviewtask/userbank/processor/BalanceAccrualProcessor.java#L41) · параметры [BalanceProperties #L14](src/main/java/home/interviewtask/userbank/config/BalanceProperties.java#L14)
- [x] **Трансфер денег** — транзакционный, потокобезопасный — эндпоинт [AccountController.transfer #L37](src/main/java/home/interviewtask/userbank/api/controller/AccountController.java#L37) · [TransferService.transfer #L32](src/main/java/home/interviewtask/userbank/api/service/TransferService.java#L32) · упорядоченный захват блокировок [#L41](src/main/java/home/interviewtask/userbank/api/service/TransferService.java#L41) · пессимистичная блокировка [AccountRepository #L21](src/main/java/home/interviewtask/userbank/postgresql/repository/AccountRepository.java#L21)
- [x] **Получение баланса** — [AccountController.myBalance #L31](src/main/java/home/interviewtask/userbank/api/controller/AccountController.java#L31)

## 5. Необязательные фичи

- [x] **Swagger** (минимальная конфигурация) — [OpenApiConfig #L12](src/main/java/home/interviewtask/userbank/config/OpenApiConfig.java#L12)
- [x] **Значимое логирование** — переводы [TransferService #L56](src/main/java/home/interviewtask/userbank/api/service/TransferService.java#L56), вход [AuthService #L49](src/main/java/home/interviewtask/userbank/api/service/AuthService.java#L49), аспект SQL-репозиториев [SqlLoggingAspect #L23](src/main/java/home/interviewtask/userbank/postgresql/log/SqlLoggingAspect.java#L23)
- [x] **Кэширование (Caffeine)** на API/DAO — конфиг [application.yml#L31](src/main/resources/application.yml#L31) · `@Cacheable` [UserService #L33](src/main/java/home/interviewtask/userbank/api/service/UserService.java#L33) · инвалидация `@CacheEvict` [ContactService](src/main/java/home/interviewtask/userbank/api/service/ContactService.java)

### Дополнительно (сверх ТЗ)

- [x] **AOP-логирование запросов к репозиториям** — аспект перед каждым вызовом пишет аргументы и сам запрос (`@Query` / производный) — [SqlLoggingAspect #L23](src/main/java/home/interviewtask/userbank/postgresql/log/SqlLoggingAspect.java#L23) · подключение AOP [pom.xml#L45](pom.xml#L45)
- [x] **Два формата миграций — SQL и YAML** (наглядность и вариативность) — оркестрация [db.changelog-master.yaml](src/main/resources/db/changelog/db.changelog-master.yaml) · схема в SQL [001-init-schema.sql](src/main/resources/db/changelog/changes/001-init-schema.sql) · сид-данные в YAML [002-seed-data.yaml](src/main/resources/db/changelog/changes/002-seed-data.yaml)
- [x] **Документация и чек-лист** — описание проекта [README.md](README.md) · трассировка требований ТЗ → код [REQUIREMENTS.md](REQUIREMENTS.md)
- [x] **Шаблоны запросов/ответов для всех ручек API** — на каждую ручку пример запроса и ответа (успех, 401, ошибки валидации/бизнес-логики); разложены по папкам [request/](src/main/resources/template/request) и [response/](src/main/resources/template/response), описание [template/README.md](src/main/resources/template/README.md)

## 6. Тестирование

- [x] **Unit-тесты трансфера денег** — [TransferServiceTest #L24](src/test/java/home/interviewtask/userbank/api/service/TransferServiceTest.java#L24)
- [x] Unit-тесты начисления (бонус) — [BalanceAccrualProcessorTest #L19](src/test/java/home/interviewtask/userbank/processor/BalanceAccrualProcessorTest.java#L19)
- [x] **API-операция через Testcontainers + MockMvc** — [UserApiIntegrationTest #L31](src/test/java/home/interviewtask/userbank/api/controller/UserApiIntegrationTest.java#L31)
