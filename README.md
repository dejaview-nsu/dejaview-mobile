# dejaview-mobile

План Android-разработки для четырёх разработчиков: две недели на первую тестовую поставку и отдельный этап для остальных требований.

Android-приложение открывает выбор режима поиска. В `demoDebug` запрос «космос» в режиме «Описание» показывает синтетический фильм. Режимы «Кадр», «Видео» и «Звук» открываются после демонстрационного входа и позволяют проверить выдачу с 10, 1 или 0 результатами. В `prodDebug` доступность режима зависит от подключения реализации владельца, а защищённые режимы требуют активной сессии.

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
