# dejaview-mobile

План Android-разработки для четырёх разработчиков: две недели на первую тестовую поставку и отдельный этап для остальных требований.

Android-приложение открывает выбор режима поиска. В `demoDebug` запрос «космос» в режиме «Описание» показывает синтетический фильм. Режимы «Кадр», «Видео» и «Звук» открываются после входа через общую форму. Для кадра и видео доступны демонстрационные варианты выдачи, для звука — выбор файла и запись с микрофона. В `prodDebug` доступность режима зависит от подключения реализации владельца, а защищённые режимы требуют активной сессии.

Каталог базовых Compose-компонентов находится в `core/ui/src/debug/`. Откройте `ComponentPreviews.kt` в Android Studio с выбранным debug-вариантом модуля `core/ui`. Доступны элементы управления, состояния FR-04 и карточка для узкого экрана с масштабом шрифта 200%.

## Локальная сборка

Android-проект находится в `android/`, модуль приложения — в `android/app/`. Для работы в Android Studio откройте каталог `android/`.

Установите JDK 17 и Android SDK Platform 36 с Build Tools 35.0.0. Укажите SDK через `ANDROID_HOME` или создайте `android/local.properties` в Android Studio.

```powershell
.\android\gradlew.bat -p android :app:assembleDemoDebug :app:assembleProdDebug
npm ci --prefix tools/design --ignore-scripts
python scripts/quality_gate.py
```

В Linux/macOS команда сборки из корня репозитория — `./android/gradlew -p android :app:assembleDemoDebug :app:assembleProdDebug`. APK находятся в `android/app/build/outputs/apk/`. Для smoke-тестов подключите устройство или запустите эмулятор и выполните `python scripts/quality_gate.py --ui` из корня репозитория.

## Вход и аудиопоиск

На стартовом экране выберите «Аудиопоиск». После входа приложение откроет выбор документа или запись с микрофона. Отправка начинается по кнопке «Найти фильм». Выйти из аккаунта можно в профиле.

В `demoDebug` подойдут синтетическое имя `viewer` и любой непустой пароль. Имена `invalid`, `unconfirmed`, `blocked`, `captcha`, `locked`, `offline` и `error` включают соответствующие ответы формы. Аудиофрагмент короче секунды даёт пустую выдачу, более длинный допустимый фрагмент — один демонстрационный фильм.

Для `prodDebug` передайте Gradle-свойство `-PdejaviewApiOrigin=https://<host>` при сборке. Укажите HTTPS origin стенда без пути. Адаптер добавляет `/api/v1`. Без адреса форма показывает ошибку конфигурации. Для серверного CAPTCHA остаётся открытым Q-03 — нужны доверенная страница, origin и согласованный возврат одноразового токена.

Свойство `dejaviewApiOrigin` также можно задать локально в `android/local.properties`. Готовый `app-prod-debug.apk` устанавливается на Android 8.0 (API 26) и новее. Для повторного живого smoke на выделенном тестовом телефоне или эмуляторе Android 10+ сначала соберите `:app:assembleProdDebug :app:assembleProdDebugAndroidTest`, затем выполните команду ниже. Скрипт установит обе сборки, запросит данные тестового аккаунта, отправит короткий синтетический WAV и завершит созданную сессию.

```text
python scripts/staging_audio_smoke.py --serial <serial-из-adb-devices> --origin https://<host>
```

Для аппаратной проверки автостопа на выделенном телефоне запустите Gradle-задачу `:app:connectedDemoDebugAndroidTest` с параметром `-Pandroid.testInstrumentationRunnerArguments.recordSmoke=true`. Тест включает микрофон примерно на десять секунд, проверяет готовность записи к прослушиванию, запускает проигрыватель и удаляет фрагмент.

## Документация

- [Общий план и календарь](docs/planning/README.md)
- [Dev 1 — приложение, текстовый поиск и карточка фильма](docs/planning/dev-1.md)
- [Dev 2 — сеть, CI, изображение, списки и оценки](docs/planning/dev-2.md)
- [Dev 3 — файлы, видео, история и профиль](docs/planning/dev-3.md)
- [Dev 4 — сессии, вход и аудио](docs/planning/dev-4.md)
- [Общие контракты](docs/planning/contracts.md)
- [Зависимости Android и версии инструментов](docs/dependencies.md)
- [Покрытие требований](docs/planning/coverage.md)
- [API и внешние зависимости](docs/planning/api-readiness.md)
- [Приёмка](docs/planning/acceptance.md)
- [Следующие этапы](docs/planning/phase-2.md)
- [Фактическая реализация backend](docs/planning/backend-evidence.md)
- [Дизайн: 88 фреймов, токены и приватный экспорт](docs/design/README.md)
- [Работа с агентами, промпты и 13 skills](docs/agents.md)
- [Конфиденциальность и проверки](docs/privacy.md)
- [Статус подготовки и оставшиеся действия](docs/preparation-status.md)
