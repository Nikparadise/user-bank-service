-- Бутстрап локальной БД: создание роли и базы для приложения.
--
-- РАЗОВЫЙ ШАГ перед запуском приложения; НЕ часть Liquibase-changelog (в db.changelog-master.yaml
-- не подключён): CREATE ROLE/DATABASE выполняются до того, как Liquibase подключится к базе.
--
-- Креды передаются psql-переменными (-v) из secret.properties.
-- Ожидаемые переменные: db_name, db_user, db_password.
--
-- Пример запуска под суперпользователем (значения берутся из secret.properties):
--
--   PowerShell (из корня проекта):
--     $p = ConvertFrom-StringData (Get-Content src/main/resources/secret.properties -Raw)
--     $name = ($p.'db.url' -split '/')[-1]
--     & "E:\DB\postgresQL\bin\psql.exe" -U postgres -h 127.0.0.1 -p 5432 -d postgres `
--         -v db_name=$name -v db_user=$($p.'db.username') -v db_password=$($p.'db.password') `
--         -f src/main/resources/db/changelog/changes/000-create-database.sql
--
--   bash:
--     psql -U postgres -h 127.0.0.1 -p 5432 -d postgres \
--         -v db_name=userbank -v db_user=<USER> -v db_password=<PASSWORD> \
--         -f 000-create-database.sql
--
-- Идемпотентно: повторный запуск не упадёт, если роль/база уже существуют.
-- %I/%L в format(...) безопасно экранируют идентификатор и строковый литерал.

SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'db_user', :'db_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = :'db_user')\gexec

SELECT format('CREATE DATABASE %I OWNER %I', :'db_name', :'db_user')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'db_name')\gexec
