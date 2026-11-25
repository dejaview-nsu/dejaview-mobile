# Контракты независимой разработки DejaView

Это проект контрактов v1 для фиксации в D1. Имена и семантика общие; код ниже показывает форму соглашения, а не готовые Kotlin-файлы. После D1 изменение оформляется небольшим PR с миграцией потребителей и автоматическими проверками; ревью потребителя желательно, но не обязательно. Предположения о backend отдельно перечислены в [реестре API](api-readiness.md).

В скелете D1-01 базовые `AppResult` и `AppError` доступны из `core/contracts`, модели (включая `SearchCompletion` для истории и `MovieList` для библиотеки) — из `core/model`, маршруты — из `core/navigation`. Владельцы добавляют свои контракты сессии, медиа, истории и персональных действий в соответствующие пакеты `core/contracts`.

## Границы модулей

```text
app → feature implementations + core/navigation + core/ui
feature presentation → feature domain → core/contracts + core/model
feature data → feature domain + core/network / core/media / core/storage
core/network → core/contracts + core/model
core/security → core/contracts + core/model
core/media, core/storage → core/contracts + core/model
feature/search → feature/search-api + core/contracts
feature/search-{text,image,video,audio} → feature/search-api + core/contracts
```

`app` собирает реализации. Производственные `feature/*` не импортируют реализации других фич. Hilt-связывание контрактов выполняется в модуле владельца реализации. `core/network` принимает абстракцию cookie/session invalidation и не импортирует `feature/auth`. `core/security` не создаёт Retrofit. Так исключается цикл network ↔ security.

Domain не содержит `Context`, `Uri`, `Bitmap`, Compose, Retrofit, Room или DTO. Android-тип `Uri` допустим на границе presentation/data импорта. Общие идентификаторы — `Long` для фильма и отзыва, непрозрачная строка для локального запроса/медиа. DTO остаются в `data` своего владельца; `core/network` содержит только транспорт и общий DTO ошибки. Общие модели содержат реально используемое пересечение полей, а не весь Swagger.

## Модели и ошибки

Dev 1 создаёт `core/model`: `MovieSummary(movieId, title, year?, posterUrl?, overview?)`, `SearchMode`, `MovieId`, `RequestId`. Отсутствующий год/постер/описание отображается заглушкой; `null` не превращается в строку `"null"`. Список search results сохраняет серверный порядок. Числовой score нельзя выдумывать: его нет в схеме.

Dev 1 согласует `AppResult<T> = Success<T> | Failure(AppError)`; Dev 2 реализует преобразование HTTP. `AppError` различает validation с полем, invalid file, unauthenticated, forbidden, not found, rate limit с временем разрешённого повтора, offline, timeout, service unavailable, unknown. Исходный машинный `code` сохраняется. `CancellationException` передаётся как отмена корутины и не становится сетевой ошибкой.

`401` защищённого запроса инвалидирует сессию; `403` не равен logout. `400/403` на login могут означать неправильный пароль, CAPTCHA или неподтверждённый email. Несколько одновременных 401 вызывают одно изменение состояния сессии. Interceptor не запускает Activity и не навигирует; UI реагирует на session state. Ошибка неизвестного формата/HTML не вызывает падение парсера.

## Сессия и возврат после входа

**Владелец Dev 4.** `core/contracts/session` содержит `SessionReader`, `SessionController`, `SessionState`, `SessionCookieStore`. `core/security` реализует защищённое cookie-хранилище и состояние; `feature/auth` — login/session/logout API и UI. Dev 2 подключает cookie store к OkHttp.

```kotlin
interface SessionReader {
    val state: StateFlow<SessionState>
}
// Initializing, Guest, Expired, Authenticated(username, expiresAt, accountScope)
interface SessionController {
    suspend fun restore(): AppResult<Unit>
    suspend fun logout(): AppResult<Unit>
}
```

Fake допускает явно выбранное состояние и управляемое истечение срока. В prod восстановление начинается с Initializing, читает cookie и проверяет `/auth/session`; ошибка сети не должна ошибочно подтверждать действующую авторизацию. Cookie `dv_session` хранится с host/domain, path, secure и expires; не прикладывается к TMDB, сторонним картинкам или произвольным URL. Выход очищает локальную сессию даже при недоступном сервере и сообщает, если удалённый logout не подтверждён. Уже отправленные запросы старого session generation не могут восстановить cookie после выхода.

`SessionInfo` 1.4.1 не содержит стабильного `user_id`. Для MVP локальный `accountScope` — случайный namespace **одного входа**. При logout/истечении истории этого namespace очищаются; после нового входа создаётся новый. При обычном перезапуске с действующей сессией namespace сохраняется. Это сознательное упрощение: история не переживает повторный вход. Для устойчивой истории нескольких аккаунтов нужен согласованный стабильный ID, нельзя использовать изменяемый username как надёжный ключ.

В D1-03 приложение принимает опциональные Hilt-привязки `SessionReader` и `HistoryRepository`. Когда модули владельцев регистрируют интерфейсы, оболочка использует их автоматически. Экран входа подключается в `SearchLoginDestination`. После изменения сессии на `Authenticated` оболочка выполняет ожидающее действие.

**Владелец возврата Dev 1.** Маршрут `LoginRoute(pendingActionId)` хранит только ID. `PendingActionStore` содержит типизированное действие: `OpenMode(mode, draftId?)`, `SetMembership(movieId, list, desiredValue)`, `SetRating(movieId, rating?)`. Возврат выполняет его ровно один раз; cancel входа удаляет ожидающее действие. Сохраняется желаемое значение, а не `toggle`, чтобы повтор не инвертировал результат. Пароли и cookie никогда не входят в route/SavedState.

Медиа после login остаётся на экране подтверждения и отправляется по «Найти». Не следует неожиданно загружать файл сразу после входа. Списки и оценка после входа выполняют уже выбранное пользователем действие. При завершении процесса незавершённая операция не повторяется автоматически; экран восстанавливает безопасный черновик либо предлагает выбрать файл снова.

## Оболочка и режимы поиска

**UI-контракт владеет Dev 1**, каждый режим реализует свой владелец. `SearchMode` принадлежит `core/model`, чтобы история не зависела от feature. `feature/search-api` описывает UI-вход:

```kotlin
interface SearchModalityUi {
    val mode: SearchMode
    @Composable
    fun Content(
        draftId: String?,
        onCompleted: (SearchCompletion) -> Unit,
        modifier: Modifier
    )
}
```

Каждая реализация регистрируется через Hilt `@IntoMap` и `@SearchModeKey` в модуле владельца. Значение ключа должно совпадать с `mode` реализации. Определение двух bindings для одного ключа отклоняется при сборке. В D1-03 оболочка упорядочит режимы как TEXT, IMAGE, VIDEO, AUDIO и применит правила доступа. TEXT доступен гостю, остальные режимы требуют сессии. Сервер также проверяет сессию. Переходы выполняет оболочка.

`SearchCompletion` содержит `requestId`, `mode`, `results`, безопасное описание ввода и время завершения. Ошибка/прогресс остаются в UiState режима: `Idle → Preparing → Ready → Uploading(sent,total?) → Processing → Completed/Failed/Cancelled`. Для текстового запроса Uploading можно пропустить. Процент — только по отправленным байтам; Processing без фиктивного процента.

На новый поиск создаётся requestId. Нажатия «Найти» блокируются на время активного запроса. Отмена отменяет HTTP-call и локальную обработку; ответ старого requestId не открывает выдачу. Повтор создаёт новый запрос только по действию пользователя. Серверный поиск синхронный: не придумываем job ID и polling endpoint.

Оболочка принимает completion, записывает историю **один раз** через `HistoryRepository`, кладёт выдачу в свой `SearchResultStore` и открывает `ResultsRoute(requestId)`. Режимы не записывают историю самостоятельно. Ошибка записи истории не отменяет успешный поиск. Фейки истории также дедуплицируют requestId.

`SearchResultStore` живёт в scope графа поиска; поворот сохраняет выдачу. После уничтожения процесса большой список не передаётся в navigation arguments: найденный фильм доступен через локальную историю, иначе предлагается повторить поиск. `MovieRoute(movieId)` содержит только идентификатор. Типизированные маршруты поддерживаются Navigation Compose; команда фиксирует совместимый набор библиотек в D1. [Android Developers](https://developer.android.com/guide/navigation/design/type-safety).

## Медиа

**Владелец Dev 3.** `core/contracts/media` содержит метаданные без Android-зависимостей:

```kotlin
data class PreparedMedia(
    val id: String,
    val mimeType: String,
    val byteCount: Long,
    val durationMs: Long?,
    val displayName: String
)
interface MediaStore {
    suspend fun metadata(id: String): AppResult<PreparedMedia>
    suspend fun release(id: String)
}
```

Android-адаптер `core/media` принимает content URI, читает поток с ограничением размера, копирует во внутренний временный файл и выдаёт ID. Поток для multipart открывает data-адаптер по этому ID. Физический путь и content URI не уходят в domain и route. `displayName` — только подпись, никогда часть вычисляемого файлового пути. Нельзя предполагать наличие реального пути у content URI или достоверность MIME/размера провайдера.

Проверки: файл доступен и непустой, формат/содержимое согласованы, размер проверяется также во время копирования, длительность медиа определена. Неизвестная длительность не принимается как нулевая. Повреждение/неподдерживаемый кодек дают понятную ошибку. Размеры первой реализации: image ≤10×1 048 576, video ≤50×1 048 576, audio ≤20×1 048 576 байт; video ≤30 000, audio ≤10 000 мс. Бинарная единица подтверждена также кодом backend main; совместимость реального стенда проверяется D2.

Форматы: JPG/JPEG, PNG, WEBP; MP4, MOV, WEBM; MP3, WAV, OGG, M4A. Multipart использует одно поле `file` и соответствующий MIME. Для input image/video используем системный Photo Picker с fallback; для audio — документный picker. Доступ предоставляется к выбранным файлам. [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker).

Каждая модальность владеет временем жизни своего draft: до отправки/после ошибки файл доступен для ручного повтора; после успеха, явной отмены или замены освобождается. App startup удаляет оставшиеся бесхозные временные файлы. История не удерживает оригинальный файл. При недостатке места импорт прерывается без частичного рабочего draft. Нет загрузки всего 50-МБ видео в `ByteArray`.

Триммер, камера, crop и извлечение звука не входят в первую реализацию. Интерфейс MediaStore не должен заранее содержать десятки методов будущего редактора.

## Списки и оценки

**Владелец Dev 2.** Общий интерфейс живёт в `core/contracts/personal`; одна реализация — в `feature/library/data`. Его используют карточка фильма, выдача и библиотека:

```kotlin
interface PersonalMoviesRepository {
    fun observe(movieId: Long): Flow<PersonalMovieState>
    suspend fun refresh(movieId: Long): AppResult<Unit>
    suspend fun setMembership(movieId: Long, list: MovieList, present: Boolean): AppResult<Unit>
    suspend fun setRating(movieId: Long, rating: Int?): AppResult<Unit>
}
```

`PersonalMovieState` различает Unknown/Loading/Ready/Error; отсутствие ответа не равно `false` и оценке 0. Ready содержит `inWatchlist`, `isWatched`, `myRating?`. Гостю не выполняется `/my-state`; действия переводят к login. Отметки независимы: один фильм может находиться в обоих списках, оценка не выставляет автоматически watched.

В MVP действия подтверждаются сервером; пока запрос идёт, повтор для той же пары movie/list заблокирован. `204` — успех без JSON. После изменения наблюдатели карточки и библиотеки получают одно состояние. На ошибке прежнее состояние сохраняется. Undo — обратный setMembership к предыдущему значению, тоже с обработкой сетевой ошибки. Оценка принимает 1–10 либо null для удаления. После изменения обновляется summary; UI не вычисляет серверную среднюю локально.

Карточка отображает кнопки сама, используя интерфейс: `movies` не импортирует UI `library`. Редактор отзывов во втором этапе открывается отдельным route, результат закрытия инвалидирует данные карточки. Пагинация списков — 24; `sort` меняет запрос и сбрасывает страницу, старые ответы не приклеиваются к новой сортировке.

## История и профиль

**Владелец Dev 3.** `HistoryRepository` в `core/contracts/history`: `record(completion, accountScope)`, `observe(scope)`, `clear(scope)`. `core/storage` реализует Room. `feature/history` показывает экран, профиль только открывает маршрут. `null` в `accountScope` обозначает гостевую историю. `observe` отдаёт завершения поиска в пределах указанного scope, поэтому оболочка восстанавливает выдачу по `requestId`. История хранит режим, дату, текст или безопасное имя/метаданные файла, результат и requestId; оригинальные медиа не сохраняет. Успех/пусто записываются, сетевые ошибки и отмена — нет, пока не введена отдельная спецификация ошибок истории.

Для гостя история первой версии временная, в памяти. Для вошедшего пользователя — Room в пределах session namespace. Очистка/выход удаляют локальную историю этого scope и незавершённые персональные действия. Нельзя смешать историю двух аккаунтов. Повтор текста подставляет текст в режим TEXT, но не отправляет автоматически. Для медиа первая версия предлагает выбрать файл снова.

Минимальный профиль использует username/avatar из `SessionInfo`; не зависит от ещё не согласованного `/users/me`. Самостоятельного HTTP-кэша профиля в MVP нет. Logout вызывает `SessionController`, а реакция на новое состояние очищает профиль/библиотеку/историю независимо от того, какой экран инициировал выход.

## Демо и реальные сборки

Dev 1 объявляет flavor dimension `backend` и варианты `demoDebug`, `prodDebug`; точные Gradle tasks проверяются после скелета. Demo никогда не обращается в сеть, имеет постоянную отметку «Демо» и сценарии success/empty/error/expired. Prod никогда не переключается на Fake при ошибке. Обе реализации живут у владельца фичи; общие test rules/fixtures — `core/testing` только в test-зависимостях.

Fake-сценарии используют одинаковые movieId, null-поля, session events и состояния списков. У всех Fake фиксированная управляемая задержка, а не случайные ответы. ProdDebug предназначен для стенда; prodRelease запрещает cleartext и диагностические тела auth-запросов. Версии JDK/AGP/Kotlin/Compose/KSP фиксируются после пробной сборки D1, а не копируются из несверенного черновика.

## Точки передачи

| Контракт | Кто отдаёт | Кому нужен | Срок stub | Срок реализации |
| --- | --- | --- | --- | --- |
| Routes, SearchCompletion, UI режима | Dev 1 | Все | D1 | D3–D4 |
| HTTP-client и AppError mapping | Dev 2 | Все data-слои | D1 | D2 |
| PersonalMoviesRepository | Dev 2 | Dev 1 | D1 | D6–D7 |
| MediaStore | Dev 3 | Dev 2 и Dev 4 | D1 | D2 |
| HistoryRepository | Dev 3 | Dev 1 | D1 | D5–D6 |
| SessionReader/CookieStore | Dev 4 | Все, Dev 2 | D1 | D2–D3 |

Stub должен собираться, а Fake должен выполнять семантику. Реализация может отставать, интерфейс — нет. Потребитель проверяет общий контракт на Fake владельца и возвращает замечания до работы над деталями UI.
