# Что действительно реализовано в backend

Проверено чтением исходников public main 21 ноября 2026 года, commit `3285bdff6a1c1fe1d7525ee4d12735e72bd762d9`. Это аудит кода и наличия тестов, **не результат запуска backend, тестов или запросов к стенду**. Ранее сравнение двух Swagger давало недостаточно информации о реализации; таблица ниже уточняет его.

В [регистрации маршрутов](https://github.com/dejaview-nsu/dejaview-backend/blob/3285bdff6a1c1fe1d7525ee4d12735e72bd762d9/src/main.cpp#L114) есть 24 операции API и health check. Обработчики содержат рабочую логику, обращения к БД/ML и проверки; это не только объявления маршрутов.

| Функция | Доказательство в коде | Наличие интеграционных тестов | Решение для Android |
| --- | --- | --- | --- |
| Register, confirm/resend email | `src/auth/registration.cpp` | `tests/integration/registration_test.cpp` | Серверная часть существует; клиентские экраны остаются этапом 2 по срокам |
| Login, CAPTCHA, logout, session | `src/auth/login.cpp`, `session.cpp` | `login_test.cpp` | Использовать в MVP; проверить cookies и challenge на стенде |
| Reset/check/complete и смена пароля | `src/auth/password.cpp` | `password_test.cpp` | Не планировать повторную разработку сервера; мобильный этап 2 |
| OIDC start/callback/pending/complete | `src/auth/oidc.cpp` | `oidc_test.cpp` | Браузерный flow реализован; мобильный exchange/PKCE-контракт отдельно |
| Image/video/audio/text search | `src/search/search.cpp`, `query.cpp`, `rules.cpp`, `ml_client.cpp` | `search_test.cpp` | Реальные адаптеры всех четырёх режимов в MVP |
| Карточка фильма, actors | `src/movies/card.cpp` | `movies_test.cpp` | Использовать существующую реализацию |
| Чтение отзывов | `src/reviews.cpp` | `reviews_test.cpp` | Сервер есть; мобильный экран этапа 2 |
| Два списка и membership | `src/lists.cpp` | `lists_test.cpp` | Использовать адаптер существующих путей и DTO |

Все пути в таблице относительны к [зафиксированному дереву backend](https://github.com/dejaview-nsu/dejaview-backend/tree/3285bdff6a1c1fe1d7525ee4d12735e72bd762d9). Упоминание теста означает наличие сценария в исходниках, а не зелёный прогон.

## Расхождения, которые нужно учесть в адаптерах

- Списки: GET `/users/me/lists/{list_type}`, PUT/DELETE `/users/me/lists/{list_type}/{movie_id}`. Типы `want_to_watch` и `watched`. GET возвращает `movies,total`; фильм содержит `movie_id,title,poster_url,average_rating,user_rating`. Страница — 24, шесть сортировок. Не добавлять отсутствующие `year`/`added_at` из воздуха. PUT/DELETE отвечают 204.
- Membership: GET `/users/me/movies/{movie_id}` возвращает `want_to_watch,watched`. Это не полный целевой `/movies/{movie_id}/my-state`: личной оценки здесь нет. `Unknown` rating не равен отсутствию оценки.
- Отзывы: GET возвращает `reviews,has_more`, по 10; есть четыре сортировки. При действующей сессии собственный отзыв исключается из списка. Для полного UI собственного отзыва одного этого GET недостаточно.
- Session возвращает username/avatar_url/has_password/expires_at, но не стабильный user_id. Внутренний ID сервера не означает наличие поля в JSON. Сохраняется согласованное MVP-решение с локальным session namespace и очисткой при выходе.
- Поиск текста: trim Unicode White_Space и длина 3–250 Unicode code points, не байтов и не UTF-16 code units. Файловые ограничения бинарные (MiB); M4A поддерживается. Синхронный поиск не требует придуманного job polling.
- OIDC уже работает для браузера: серверные state/verifier и cookie `dv_oidc`, возврат в SPA. `POST /auth/oidc/exchange` и целевой `client=android` в проверенном main не найдены. Native cookie handoff требует отдельного решения, а не копирования браузерного flow.

## Чего нет в проверенной таблице маршрутов

Не найдены rating PUT/DELETE и rating-summary GET, профиль/статистика/аватар/удаление аккаунта, mutation/reaction endpoints отзывов и billing. Таблица ratings в БД и чтение user_rating в списке не заменяют endpoint записи оценки. Эти пункты требуют подтверждения backend-владельца; неизвестная приватная разработка не объявляется ни отсутствующей, ни готовой.

Проверены семь публичных `feat/*` веток: у всех `ahead_by=0` относительно main; их наличие не доказывает текущую отдельную разработку. На момент проверки единственный открытый PR — №17 с обновлением Docker login action. В публичных открытых PR не найдено реализации перечисленных недостающих функций. Это снимок состояния, его нужно обновить перед D1.

## Влияние на две недели

Большая часть серверных функций уже реализована. Регистрация/reset/OIDC не перенесены из-за полного отсутствия backend: сокращение относится к объёму Android-команды. Для MVP: четыре поиска, вход, карточка и существующие списки можно подключать сразу после получения стенда. Главная подтверждённая API-зависимость оставленного scope — запись/удаление оценки и её summary. До D2 нужен конкретный срок backend либо явное продуктовое решение об исключении оценки; нельзя молча выдавать demo за prod.

Dev 2 фиксирует адаптер списков текущего main, Dev 4 проверяет session/CAPTCHA, Dev 3 — реальные медиа, Dev 1 — text/movie. На стенде отдельно проверяются доступность, deploy commit, схемы, ошибки и cookies. Согласование 1.4.1 не означает требование заново реализовать уже существующие функции.
