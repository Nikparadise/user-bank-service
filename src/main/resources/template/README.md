# Шаблоны запросов и ответов API

Примеры запросов (`request/request-*.json`) и ответов (`response/response-*.json`) для всех ручек API.
Шаблоны разложены по двум подпапкам: `request/` — запросы, `response/` — ответы.

- **request/request-*.json** — пример запроса: метод, путь, заголовки и тело (`body`) либо параметры (`queryParams`/`pathParams`). Для защищённых ручек нужен заголовок `Authorization: Bearer <accessToken>` — токен берётся из `POST /api/auth/login`.
- **response/response-*.json** — все возможные ответы ручки, сгруппированные по сценариям: корректный формат и ответы при ошибках (нет/невалидный токен → 401, ошибки валидации → 400, бизнес-ограничения, 404, 409 и т.д.). Каждый сценарий: `status` (HTTP-код) + `body` (тело ответа, `null` — пустое тело).

Формат ошибок (кроме «голого» 401 от Spring Security) — единый `ErrorResponse`:
`{ timestamp, status, error, message, fieldErrors? }` (`fieldErrors` присутствует только при ошибках валидации).

> Примечание: неавторизованный запрос к защищённой ручке отбивается фильтром Spring Security
> (`HttpStatusEntryPoint`) — возвращается **пустое тело** со статусом `401`, ещё до контроллера.
> Ответ `401` с телом `ErrorResponse` бывает только у `POST /api/auth/login` (неверные креды).

## Список ручек

| Ручка | Request (в `request/`) | Response (в `response/`) |
|---|---|---|
| `POST /api/auth/login` | request-login.json | response-login.json |
| `GET /api/accounts/me` | request-account-balance.json | response-account-balance.json |
| `POST /api/accounts/transfer` | request-transfer.json | response-transfer.json |
| `GET /api/me/emails` | request-list-emails.json | response-list-emails.json |
| `POST /api/me/emails` | request-add-email.json | response-add-email.json |
| `PUT /api/me/emails/{emailId}` | request-change-email.json | response-change-email.json |
| `DELETE /api/me/emails/{emailId}` | request-delete-email.json | response-delete-email.json |
| `GET /api/me/phones` | request-list-phones.json | response-list-phones.json |
| `POST /api/me/phones` | request-add-phone.json | response-add-phone.json |
| `PUT /api/me/phones/{phoneId}` | request-change-phone.json | response-change-phone.json |
| `DELETE /api/me/phones/{phoneId}` | request-delete-phone.json | response-delete-phone.json |
| `GET /api/users/{id}` | request-get-user.json | response-get-user.json |
| `GET /api/users` (поиск) | request-search-users.json | response-search-users.json |
