Telegram bot - survey_bot

Сервис включает в себя:
* Работу с google api для сохранения данных в таблицы;
* Работу с telegram api, сервис выступает в качестве telegram бота

## Описание работы

Сервис позволяет загрузить любой пользовательский опрос и предоставить его для прохождения через месседжер telegram.
Имеется возможность назначить админов в боте и загружать опросы в тестовом формате, для прохождения опросов в тестовом формате.

## Свойства приложения и ENV переменные

| Свойство                  | Описание                              | ENV              |
|---------------------------|---------------------------------------|------------------|
| spring.application.name   | Имя приложения                        |                  |
| telegram.bot.type         | Тип телеграм бота                     |                  |
| telegram.bot.name         | Имя телеграм бота                     |                  |
| telegram.bot.token        | Токен телеграм бота                   | BOT_TOKEN        |
| telegram.bot.webhook-path | Путь эндпоинта для вебхук бота        |                  |
| telegram.bot.webhook-url  | Url вебхук бота                       | WEBHOOK_URL      |
| admin.chat_id             | телеграм chat id владельца приложения | ADMIN_TG_CHAT_ID |


## Описание режима работы бота
Бот может быть запущен в двух режимах: LongPolling или Webhook. За это отвечает настройка telegram.bot.type в файле application.yml
Для разработки или при отсутвисии статического ip адреса удобно использовать тип LongPolling, а при деплое на отдельную машину уже развернуть бот в режиме Webhook.
При работе с вебхуком, после запуска приложения необходимо установить вебхук запросом к telegram api. Запрос можно найти в файле set_webhook_request.http
или воспользоваться примером
```
POST https://api.telegram.org/bot123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11/setWebhook?url=https://www.example.com
```
В запрос необходимо вставить bot id, токен бота а в теле указать url

## Правила работы с загрузкой опросов
Опрос можно загрузить двумя способами:
1. Написать опрос или опросы в файле surveys.json в директории src/main/resources
2. Загрузить через телеграм отправив файл формата json вложенным в сообщение, текст сообщения не имеет значения. 
Сделать это может только владелец приложения, чей chat_id вписан в конфиге application.yml

## Правила составления опросов
Каждый опрос должен соответствовать определенной структуре, иметь обязательные поля: 
* type(Название опроса), 
* link(Ссылка на гугл таблицу в аккаунте владельца), 
* questions(массив с вопросами)

Необязательные поля:
* boolean testing - если true, то опрос не будет отражаться в выборке обычных пользователей, его смогут пройти в тестовом режиме владелец и назначенные админы
* boolean initialized - если false то после первого старта опроса в гугл таблица буду проинициализирована вопросами. Это нужно если вы оставили ссылку на пустую таблицу и не заполняли колонки вопросами

### Вопросы внутри массива questions
Каждый вопрос должен иметь обязательные поля:
* text - Вопрос для пользователя
* type - Принимает два значения CHOICE/FREE_TEXT,  
* type/CHOICE нужно описать ниже массив c объектами options, где внутри поля value будет ответ,  они будут предоставлены пользователю кнопками для выбора ответа. 
* type/FREE_TEXT - бот будет ожидать пользоватльский ввод следующим сообщением
* level - поле описывающее уровень вопроса. Опросы поддерживают два уровня - MAIN - основной вопрос, SUB - подвопрос следующий сразу после ответа на основной вопрос, предусматривает уточнение в зависимости от ответа пользователя 
* options - варинты ответа в виде кнопок, не заполняются в случает типа FREE_TEXT
* subQuestions - массив подвопросов для уточнения ответа на следующий вопрос, здесь ключом должен быть ответ на основоной ответ из массива options а значение - объект вопроса. Должен быть отмечен как- "level": "SUB".  Массив подвопросов не заполняется в случае основного типа вопроса - FREE_TEXT 
* Каждый вариант ответа в массиве options имеет опциональное поле action со значениями: SKIP, заполняется когда у основного вопроса имеются подвопросы но для выбранного варианта не существует подвопроса, в этом случае случится переход к следующему основному вопросу. FLUSH - после ответа на этот вариант весь опрос будет завершен а результаты будут записаны в таблицу

## Пример json c тестовым опросом
```json
{
    "type": "Название опроса",
    "link": "https://docs.google.com/spreadsheets/d/***/edit?gid=***#gid=***",
    "testing": false,
    "initialized": false,
    "questions": [
      {
        "text": "Основной Вопрос 1",
        "type": "CHOICE",
        "level": "MAIN",
        "options": [
          {
            "value": "Ответ без надобности подвопроса",
            "action": "SKIP"
          },
          {
            "value": "Ответ после которого будет подвопрос"
          }
        ],
        "subQuestions": {
          "Ответ после которого будет подвопрос": {
            "text": "Текст подвопроса",
            "type": "FREE_TEXT",
            "level": "SUB",
            "options": []
          }
        }
      },
      {
        "text": "Основной Вопрос 2",
        "type": "CHOICE",
        "level": "MAIN",
        "options": [
          {"value": "Вариант 1"},
          {"value": "Вариант 2 после которого опрос будет завершен",
          "action": "FLUSH"} 
        ],
        "subQuestions": {
          "Вариант 1": {
            "text": "Подвопрос для варианта 1",
            "type": "FREE_TEXT",
            "level": "SUB",
            "options": []
          }
        }
      },
      {
        "text": "Основной Вопрос 3",
        "type": "CHOICE",
        "level": "MAIN",
        "options": [
          {"value": "Вариант 1"},
          {"value": "Вариант 2"}
        ]
      }
    ]
  }
```

## Интеграция Google Api
Бот использует в качестве хранилища гугл таблицы, для того чтобы бот имел право на запись данных в таблицы владельца, необходимо разместить в src/main/resources файл - credentials.json.
Скачать файл можно в настройка аккаунта гугл после активации функции работы с google api

## Сборка и запуск на сервере
Для запуска бота на сервер необходимо 3 файла - Dockerfile, docker-compose.yaml и survey_bot-1.0.0.jar.
Первые два файла копируем из корня проекта. Для получения survey_bot-1.0.0.jar необходимо выполнить в консоли команду
```
./gradlew bootJar      
```
После выполнения команды файл будет лежать по пути - build/libs.
Все три файла должны лежать в одной директории на сервере, после чего для запуска бота выполните команду:
```
docker-compose up -d          
```
Чтобы убедиться что контейнер запустился и бот работает выполните команду:
```
docker ps 
```
В списке контейнеров должен появится контейнер с именем survey_bot