# Зависимости Android

## D1-01

- Gradle 8.12.1, AGP 8.10.1, Kotlin 2.2.10, JDK 17 и JVM target 17. Gradle и Kotlin распространяются под Apache-2.0. AGP поддерживает установленную SDK Platform 36 с Build Tools 35.0.0. Для внутреннего скелета закреплены `compileSdk = targetSdk = 36` и рабочее предложение `minSdk = 26`. Минимум требует проверки на целевых тестовых устройствах. [AGP](https://developer.android.com/build/releases/agp-8-10-0-release-notes) · [Kotlin](https://kotlinlang.org/docs/gradle-configure-project.html)
- Jetpack Compose BOM 2025.08.01, Activity Compose 1.10.1 и Navigation Compose 2.9.3, Apache-2.0. Встроенные типизированные маршруты с Kotlin Serialization 1.9.0 (Apache-2.0) позволяют передавать идентификаторы и восстанавливать аргументы. Рассматривались строковые маршруты и XML Safe Args. [Compose BOM](https://developer.android.com/develop/ui/compose/bom) · [Navigation](https://developer.android.com/guide/navigation/design/type-safety) · [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization)
- Dagger Hilt 2.57.2 и KSP 2.2.10-2.0.2, Apache-2.0. Hilt генерирует граф Android-компонентов и проверяет ключи map multibinding на этапе сборки. Рассматривалась ручная композиция. Владельцы регистрируют реализации `SearchModalityUi` через `@IntoMap` и `@SearchModeKey` в нужном исходном наборе своей фичи. [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) · [Dagger KSP](https://dagger.dev/dev-guide/ksp.html)
- Для gate используются Spotless 7.2.1 с ktlint 1.7.1 (Apache-2.0 и MIT), встроенный Android Lint, проверка фактического Gradle-графа, JaCoCo 0.8.13 (EPL-2.0), JUnit 4.13.2 (EPL-1.0) и AndroidX Test JUnit 1.3.0 (Apache-2.0). JaCoCo подключён к JVM-модулю маршрутов. Владельцы добавляют проверки своих модулей с появлением логики. [Spotless](https://github.com/diffplug/spotless) · [JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/) · [AndroidX Test](https://developer.android.com/jetpack/androidx/releases/test)

Версии закреплены в `android/gradle/libs.versions.toml`. Dev 1 ведёт совместимость toolchain и проверяет оба flavor при обновлении. Сетевые, медиа- и storage-библиотеки выбирают их владельцы в своих задачах.

## D1-02

- Onest и Unbounded из Google Fonts поставляются локально как variable TTF с лицензиями SIL OFL 1.1 в `core/ui/src/main/assets/licenses/`. Это гарнитуры из макета с кириллицей. Локальные ресурсы доступны офлайн и воспроизводимы на минимальном API 26, где Android уже поддерживает variable fonts. Начертания выбираются через Compose FontVariation. Dev 1 обновляет бинарники и лицензии совместно. [Onest](https://github.com/google/fonts/tree/main/ofl/onest) · [Unbounded](https://github.com/google/fonts/tree/main/ofl/unbounded) · [Compose fonts](https://developer.android.com/develop/ui/compose/text/fonts)

- Каталог компонентов использует Compose `ui-tooling-preview` и `ui-tooling` 1.9.0 из текущего BOM только в debug. Проверки используют имеющиеся Compose Test и Android instrumentation. [Compose previews](https://developer.android.com/develop/ui/compose/tooling/previews)

- Эталонный экран включается в demo. Jetpack `lifecycle-viewmodel-compose` и `lifecycle-runtime-compose` 2.9.0 добавлены явно на версии уже используемых Lifecycle-артефактов. Они обеспечивают область жизни ViewModel, сохранение черновика и lifecycle-aware сбор состояния. `kotlinx-coroutines-test` 1.8.1 применяется только в JVM-тестах demo для управляемого времени и отмены. Dev 1 ведёт эти зависимости. [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.9.0) · [Тесты корутин](https://developer.android.com/kotlin/coroutines/test)

## Сессия и аудио

- OkHttp 4.12.0 (Apache-2.0) предоставляет проверку области cookie, отменяемый вызов и потоковый multipart. Версия совместима с минимальным Android API 26 и уже присутствует в графе инструментов проекта. Dev 2 может использовать тот же транспорт. MockWebServer и okhttp-tls той же версии работают только в тестах с временным сертификатом. [OkHttp и MockWebServer](https://github.com/square/okhttp/tree/parent-4.12.0)
- Dev 4 хранит ключ AES-GCM в Android Keystore. Зашифрованная запись сохраняется через AtomicFile в noBackupFilesDir. Для локального AAC/M4A, прослушивания и метаданных используются платформенные MediaRecorder, MediaPlayer и MediaMetadataRetriever. Эти возможности доступны на minSdk проекта. [Android Keystore](https://developer.android.com/privacy-and-security/keystore) · [MediaRecorder](https://developer.android.com/media/platform/mediarecorder)
