# AI Service

Отдельный Spring Boot сервис интеграции игры-собеседования с локальной AI-моделью.

## Стек

- Java 25
- Spring Boot 4.1
- Spring AI 2.0
- Google Gemini Developer API
- Gemini 3.5 Flash
- Maven

## Возможности

- генерация технического вопроса;
- генерация ответа виртуального кандидата;
- проверка свободного ответа пользователя;
- формирование итогового отчёта по интервью.

Состояние интервью сервис не хранит. Интервью, раунды, правильные ответы
и статистика должны оставаться в Interview Service.

## Запуск

Создайте API-ключ в Google AI Studio, затем подготовьте локальный `.env`:

```bash
cp .env.example .env
# Замените your-gemini-api-key в .env на полученный ключ
./mvnw spring-boot:run
```

Запуск через Docker Compose использует тот же `.env`:

```bash
docker compose up --build
```

Либо обычным Maven:

```bash
mvn spring-boot:run
```

Сервис запускается на `http://localhost:8087`.

Для production в репозитории есть `render.yaml`. При первом создании Blueprint
Render запросит секрет `GEMINI_API_KEY`; ключ нельзя добавлять в Git. Сервис
будет доступен по `https://teach-sim-ai.onrender.com`, а `PORT` назначит Render.
Для прогрева frontend вызывает `GET /api/v1/ai/health` до начала интервью.
В Blueprint явно задан `PORT=10000`; приложение слушает `0.0.0.0` и лениво
создаёт AI-бины, чтобы health endpoint открылся до инициализации Gemini.

## API

### Генерация вопроса

```bash
curl -X POST http://localhost:8087/api/v1/ai/questions/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "technology": "Java",
    "level": "SENIOR",
    "difficulty": 8,
    "variationSeed": "interview-42-round-3-7f19",
    "previousQuestions": ["Что такое HashMap?"],
    "previousAnswers": ["HashMap всегда потокобезопасен"],
    "candidates": [
      {"id":"maria","name":"Мария","level":"SENIOR","personality":"системная и спокойная"},
      {"id":"alex","name":"Алексей","level":"MIDDLE+","personality":"практичный и краткий"},
      {"id":"igor","name":"Игорь","level":"MIDDLE+","personality":"уверенный, любит детали"},
      {"id":"dmitry","name":"Дмитрий","level":"MIDDLE","personality":"осторожный и вдумчивый"}
    ]
  }'
```

Один вызов возвращает вопрос, эталонный ответ и ровно четыре
персонализированные реплики — по одной для каждого переданного `candidateId`.
Ровно одна реплика имеет `correct=true`. `variationSeed` меняется для каждого
раунда, а история вопросов и ответов запрещает модели повторять прежний контент.
Если модель всё же вернула повтор или слишком похожие реплики, сервис выполняет
ещё одну генерацию и повторно проверяет результат.

```json
{
  "question": "Как работает volatile в Java?",
  "correctAnswer": "volatile гарантирует видимость изменений между потоками...",
  "answers": [
    {
      "candidateId": "maria",
      "answer": "volatile обеспечивает happens-before для записи и чтения...",
      "correct": true,
      "reaction": "confident"
    },
    {
      "candidateId": "alex",
      "answer": "volatile делает все операции над объектом атомарными...",
      "correct": false,
      "reaction": "thoughtful"
    },
    {
      "candidateId": "igor",
      "answer": "volatile синхронизирует потоки через отдельный lock...",
      "correct": false,
      "reaction": "confident"
    },
    {
      "candidateId": "dmitry",
      "answer": "volatile запрещает процессору кэшировать любые данные метода...",
      "correct": false,
      "reaction": "uncertain"
    }
  ],
  "explanation": "Ключевое свойство volatile — видимость и порядок операций.",
  "difficulty": 8
}
```

### Ответ виртуального кандидата

```bash
curl -X POST http://localhost:8087/api/v1/ai/candidate-answers/generate           -H 'Content-Type: application/json'           -d '{
    "question": "Как работает volatile?",
    "candidateName": "Алексей",
    "candidateLevel": "SENIOR",
    "candidatePersonality": "уверенный и краткий",
    "correct": true
  }'
```

### Проверка ответа пользователя

```bash
curl -X POST http://localhost:8087/api/v1/ai/answers/evaluate           -H 'Content-Type: application/json'           -d '{
    "question": "Как работает volatile?",
    "expectedAnswer": "Гарантирует видимость изменений между потоками...",
    "userAnswer": "Значение читается из общей памяти и изменения видны потокам"
  }'
```

### Итог интервью

```bash
curl -X POST http://localhost:8087/api/v1/ai/interviews/result           -H 'Content-Type: application/json'           -d '{
    "interviewId": 10,
    "position": "Senior Java Developer",
    "declaredLevel": "SENIOR",
    "answers": [
      {
        "question": "Как работает volatile?",
        "answer": "Обеспечивает видимость изменений",
        "correct": true
      }
    ]
  }'
```

## Интеграция

Interview Service вызывает этот сервис по HTTP. Для production следует добавить:
авторизацию service-to-service, таймауты, circuit breaker, метрики стоимости
токенов и ограничение размера входных данных.
