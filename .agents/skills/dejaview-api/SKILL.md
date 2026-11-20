---
name: dejaview-api
description: Подключить HTTP API DejaView или сверить его с backend, написать DTO, mapper и контрактные тесты. Использовать при изменениях data-слоя и несовпадениях схем.
---

# API и доказательства совместимости

Прочитать docs/planning/backend-evidence.md, api-readiness.md и нужную операцию baseline YAML. Публичные snapshots обезличены и не содержат готовых examples: создать fixture по схеме либо обезличить реальный ответ. Не восстанавливать удалённые личные значения из исходников.

## Выбрать источник истины

Для операции записать method/path, request body/query, auth, statuses, response schema, commit backend и подходящий handler/test. Различать «есть контракт», «есть handler в main», «есть тест в исходниках», «тест запускался», «проверено на стенде». Открытый PR подтверждает только работу в ветке, не доступность в main. Номер версии OpenAPI не является процентом готовности.

Сейчас списки реализованы через `/users/me/lists/{list_type}` и `/users/me/movies/{movie_id}`. Целевой мобильный Swagger имеет другие пути/DTO. Не подменять их автоматически и не пробовать URL по очереди после 404. Выбранный adapter объявляется в сборке/DI, domain остаётся прежним.

## Реализация адаптера

1. Retrofit interface и DTO принадлежат data владельца; domain получает mapper result. Проверить required/nullable, Long ID, UTC timestamps, enum и добавочные поля.
2. Тип `204` не парсить как JSON. Empty results — успех, not found — отдельное состояние. Отсутствующее required поле — schema mismatch, не синтетический ноль.
3. Multipart содержит одно поле file, корректный MIME и поток из PreparedMedia. Не писать оригинальные пути/имена пользователя в filename или logs.
4. CookieStore и error mapper общие. 401 protected, 403 validation/forbidden и 429 разные. Учитывать Retry-After и malformed/HTML response.
5. POST search/auth не повторять автоматически после неясного обрыва. Для set membership/rating использовать желаемое значение и согласованную idempotency; не считать любое POST идемпотентным.
6. Исключение схемы оформлять узко: точное поле, expected/actual, handler reference и owner решения. Пока оно обсуждается, UI/Fake продолжаются; production adapter не имитирует успешный ответ.

## Проверки и передача

MockWebServer/fixtures покрывают валидный ответ, null, 204, error body/не-JSON, auth, pagination и cancel. Live smoke отдельно: base URL хранится в локальной конфигурации, credential не попадает в fixture. Приложить обезличенный статус запроса и версию стенда, не cookie или raw auth log.

Если меняется публичный интерфейс, перечислить потребителей и их миграцию; если меняется только путь/DTO адаптера, не переписывать UI. Проверить prod без Fake, затем privacy-check. Итог — доказательства совместимости конкретной операции и оставшийся вопрос, а не общее «backend не готов».
