---
name: dejaview-auth
description: Реализовать или проверить авторизацию DejaView, cookie-сессию, logout, CAPTCHA, App Links и мобильный OIDC без утечек и гонок состояния.
---

# Авторизация DejaView

Прочитать session contract, соответствующий UC и backend-evidence.md. В main есть password auth и browser OIDC; Android exchange из мобильного Swagger отдельно не подтверждён. Не считать браузерную cookie автоматически доступной OkHttp.

## Cookie и состояние

Одна реализация CookieStore/SessionReader обслуживает все фичи. Сохранять host/domain/path/secure/expiry; исключить сторонние hosts. DataStore без шифрования не защищает cookie — ключ Keystore, зашифрованное значение на диске, корректная реакция на потерю ключа. Пароль не сохраняется.

State: Initializing, Guest, Authenticated. Restore проверяет session; network error не подтверждает действительность неизвестной сессии. Logout локально очищает сессию и personal state даже при серверном сбое, который отображается отдельно. Session generation/cancellation не позволяет позднему Set-Cookie старого запроса вернуть авторизацию.

401 защищённого запроса изменяет state один раз. 400/403 login могут быть invalid credentials, CAPTCHA, blocked/unconfirmed. Interceptor не открывает Activity. UI навигирует по конкретному защищённому действию; публичная карточка остаётся доступной гостю.

Серверный user_id есть внутри backend, но пока не выдаётся session response. Не использовать изменяемый username как стабильный owner history. Сохранять согласованный session namespace; для cross-login history/Worker требуется отдельный стабильный ID.

## Потоки входа

PendingAction содержит desired state, а не toggle. Success выполняет действие один раз, cancel удаляет pending. Медиа возвращается к подтверждению, не отправляется без нового нажатия. Секреты не входят в routes/SavedState.

CAPTCHA определяется ответом сервера; свежий token на попытку, allowlisted page/origin и минимальный bridge. Не отключать защиту для демонстрации. Lock основан на Retry-After, не на счётчике клиента.

Для регистрации/reset проверять все альтернативы UC: одноразовая ссылка, expiry, повтор письма, browser fallback. Для OIDC — state + PKCE, callback allowlist, exchange одноразового кода native HTTP, pending cookie, complete/link. Секрет provider остаётся на сервере. Неподтверждённый endpoint блокирует только зависимый поток, не остальные задачи.

## Проверки

Устройство: cold start, expires, corrupted storage/key, logout while request, concurrent 401, другой аккаунт, back stack после выхода. HTTP: Set-Cookie и последующий session, host/path scope, late response. CAPTCHA: valid/invalid/cancel/429. OIDC: wrong state/verifier, expired/reused code, cancellation и два типа pending.

Без credentials/домена отметить live проверку как непроведённую, продолжить deterministic Fake/unit. Не писать token/cookie/email/пароль в отчёт, screenshot публичного PR или CI artifact. Перед передачей выполнить privacy-check.
