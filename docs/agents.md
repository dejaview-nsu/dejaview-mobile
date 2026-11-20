# Работа с агентами

Общие правила находятся в [AGENTS.md](../AGENTS.md). Проект содержит девять канонических skills в `.agents/skills`; они применяются к задаче, а не запускаются все подряд. Для Claude Code [CLAUDE.md](../CLAUDE.md) импортирует общие правила; `python scripts/sync_skills.py` создаёт локальные копии skills. Глобальные настройки не меняются, пользовательские правки локальных копий не перезаписываются молча.

| Работа | Основной skill | Дополнительный при необходимости |
| --- | --- | --- |
| Dev 1: scaffold, Gradle | dejaview-module | dejaview-contract |
| Любой Dev: экран/UC | dejaview-feature | dejaview-design |
| Dev 2: сеть/DTO, все data-слои | dejaview-api | dejaview-contract |
| Dev 3: media и история; Dev 2/4: импорт/запись | dejaview-media | dejaview-feature |
| Dev 4: session/auth | dejaview-auth | dejaview-api |
| Общие интерфейсы | dejaview-contract | skill владельца |
| Документы/fixtures/экспорт | dejaview-privacy | dejaview-design |
| Передача на ревью | dejaview-pr | dejaview-privacy |

Skills задают порядок работы, границы и критерии проверки. Они не изолируют файловую систему и не гарантируют отсутствие ошибок модели. Практическая защита — небольшой diff, независимые проверки, разделение владения и ревью. CI ниже пока проверяет только документы и инструменты; Android tasks добавляются после scaffold.

## Задание агенту

```text
Роль: Dev 2. Задача: D2-04 из docs/planning/dev-2.md.
Результат: два списка, пагинация и единое membership-состояние карточки.
Область: feature/library и core/contracts/personal.
Прочитай AGENTS.md, применимые skills и backend-evidence.md.
Начни с текущего diff и существующих интерфейсов.
Используй выбранный adapter текущего backend; не придумывай endpoint.
Не добавляй личные данные в fixtures. При изменении общего контракта
перечисли потребителей и мигрируй их в согласованном scope.
Заверши реализацию и доступные проверки; сообщи результаты и блокеры.
```

Перед первой задачей Dev 1 создаёт wrapper/модули; остальные готовят интерфейсы, fixtures и свои изменения по календарю. В отдельных ветках/worktrees сохраняется один выбранный контракт. Конфликт общего файла разбирается по смыслу, а не выбором целиком ours/theirs. Нельзя запускать двух агентов на запись одного набора файлов без явного разделения.

## Проверка набора

```text
python scripts/validate_repo.py
python scripts/privacy_check.py
python -m unittest discover -s scripts/tests
npm ci --prefix tools/design --ignore-scripts
npm test --prefix tools/design
node --test tools/figma-export/export.test.mjs
```

При обновлении skill проверить сценарий применения: попросить выдуманный endpoint, скрытый Fake после 404, публикацию сырого дизайна, правку чужого незакоммиченного файла или skip теста. Правильный результат — конкретное объяснение несоответствия и продолжение разрешённой части, без порчи данных и ложного отчёта. Это сценарии ручной оценки поведения, не утверждение об автоматическом тестировании модели.
