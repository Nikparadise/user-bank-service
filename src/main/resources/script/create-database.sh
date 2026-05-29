#!/usr/bin/env bash
#
# Создаёт роль и базу для приложения, выполняя 000-create-database.sql.
# Креды берутся из secret.properties — в самом скрипте ничего не захардкожено.
#
# Запуск (из корня проекта):
#   bash src/main/resources/script/create-database.sh
#
# ВАЖНО (Windows): запускайте через Git Bash, а НЕ через WSL. В WSL команда `bash` ведёт к
# врапперу postgresql-common без самого клиента ("You must install at least one
# postgresql-client package"), к тому же подключение пойдёт не с 127.0.0.1 и не попадёт под trust.
# В терминале IDEA (по умолчанию это PowerShell) запускайте Git Bash по полному пути:
#   & "C:\Program Files\Git\bin\bash.exe" src/main/resources/script/create-database.sh
# либо переключите Shell терминала IDEA на "C:\Program Files\Git\bin\bash.exe"
# (Settings -> Tools -> Terminal) и тогда работает просто `bash src/.../create-database.sh`.
#
# К серверу подключаемся под СУПЕРПОЛЬЗОВАТЕЛЕМ (по умолчанию postgres; переопределяется
# переменной окружения SUPERUSER). Бинарь psql берётся из PATH либо из переменной PSQL.
# Если суперпользователю нужен пароль — задайте переменную окружения PGPASSWORD.
#
# Примеры:
#   SUPERUSER=postgres bash src/main/resources/script/create-database.sh
#   PSQL="/e/DB/postgresQL/bin/psql.exe" bash src/main/resources/script/create-database.sh

set -euo pipefail

# Пути вычисляем относительно расположения скрипта, чтобы он работал из любого CWD.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESOURCES_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SECRET_FILE="$RESOURCES_DIR/secret.properties"
SQL_FILE="$RESOURCES_DIR/db/changelog/changes/000-create-database.sql"

PSQL="${PSQL:-psql}"
SUPERUSER="${SUPERUSER:-postgres}"

[[ -f "$SECRET_FILE" ]] || { echo "Не найден $SECRET_FILE (скопируйте из template/secret.properties.example)"; exit 1; }
[[ -f "$SQL_FILE" ]]    || { echo "Не найден $SQL_FILE"; exit 1; }

# Достаём значение свойства из secret.properties (tr -d '\r' — на случай CRLF в Windows).
prop() { grep -E "^$1=" "$SECRET_FILE" | head -1 | cut -d'=' -f2- | tr -d '\r'; }

DB_URL="$(prop 'db.url')"
DB_USER="$(prop 'db.username')"
DB_PASSWORD="$(prop 'db.password')"

[[ -n "$DB_URL" && -n "$DB_USER" && -n "$DB_PASSWORD" ]] \
  || { echo "В secret.properties должны быть заданы db.url, db.username и db.password"; exit 1; }

# Разбираем jdbc:postgresql://host:port/dbname[?params]
rest="${DB_URL#*://}"        # host:port/dbname[?params]
hostport="${rest%%/*}"       # host:port
dbpart="${rest#*/}"          # dbname[?params]
DB_NAME="${dbpart%%\?*}"     # dbname (без query-параметров)
DB_HOST="${hostport%%:*}"    # host
if [[ "$hostport" == *:* ]]; then DB_PORT="${hostport##*:}"; else DB_PORT="5432"; fi

echo "Создаю роль '$DB_USER' и базу '$DB_NAME' на $DB_HOST:$DB_PORT (суперпользователь: $SUPERUSER)…"

"$PSQL" -U "$SUPERUSER" -h "$DB_HOST" -p "$DB_PORT" -d postgres \
    -v db_name="$DB_NAME" -v db_user="$DB_USER" -v db_password="$DB_PASSWORD" \
    -f "$SQL_FILE"

echo "Готово: роль '$DB_USER' и база '$DB_NAME' созданы (или уже существовали)."
