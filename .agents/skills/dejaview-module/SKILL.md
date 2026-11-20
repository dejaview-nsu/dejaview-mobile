---
name: dejaview-module
description: Создать или изменить Gradle-модуль DejaView Android, его варианты demo/prod и DI. Использовать для скелета и модульной конфигурации, а не для обычной вёрстки экрана.
---

# Модули DejaView

## Определить изменение

Прочитать AGENTS.md, карточку задачи и docs/planning/contracts.md. Проверить наличие wrapper, settings, version catalog, convention plugins и существующего аналога. Если приложение ещё не создано, выполнить только согласованный скелет D1-01; планируемые классы не являются существующим образцом.

Владелец root Gradle/app — Dev 1. Выявить, какие строки settings/app wiring нужны до работы в feature. Уложить регистрацию модуля и stub entrypoint в одно небольшое изменение, чтобы другие владельцы не делали несовместимые сборки.

## Реализация

1. Проверить совместимость фактических JDK/AGP/Kotlin/KSP/Compose/Hilt. Фиксировать версии в существующем каталоге; не брать «latest», не обновлять весь стек ради одного модуля.
2. В feature создать presentation/domain/data/di, но не пустые UseCase/manager/interface ради шаблона. Доменная логика и медленные зависимости обосновывают отдельный интерфейс.
3. Feature зависит от core/contracts/model и нужного транспорта, search modality — также search-api. App связывает реализации. Проверить, что dependency graph не имеет цикла.
4. Согласовать backend flavor dimension. Demo bindings и prod bindings взаимоисключающие; release prod не зависит от test fixtures. Production source sets не импортируют core/testing.
5. Stub должен собираться и явно показывать незавершённую функцию. Fake реализует семантику контрактов, а не случайные значения. Скрытый fallback сети запрещён.
6. Manifest entries добавлять только требуемые задачей; content providers не экспортировать случайно. На старте не требовать permissions камеры/микрофона для всех экранов.

## Проверка

Получить список реальных Gradle tasks. Собрать модуль и приложение для demo/prod; проверить Hilt на дублирующиеся bindings и ссылки на отсутствующие source sets. Если SDK отсутствует, указать это как непроведённую проверку, не создавать фиктивный wrapper/task-success.

Проверить diff root files: только заявленные dependencies, plugins и registration. Обновить описание модуля и команды в задаче. В результате перечислить entrypoint, публичный контракт, owner и выполненную сборку. Следующий экран должен подключаться без изменения общей архитектуры.
