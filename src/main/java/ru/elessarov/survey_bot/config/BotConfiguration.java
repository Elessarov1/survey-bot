package ru.elessarov.survey_bot.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.elessarov.survey_bot.handler.CallbackHandler;
import ru.elessarov.survey_bot.handler.CommandHandler;
import ru.elessarov.survey_bot.handler.FileHandler;
import ru.elessarov.survey_bot.properties.BotProperties;
import ru.elessarov.survey_bot.survey_bot.LongPollingBot;
import ru.elessarov.survey_bot.survey_bot.WebhookBot;

import static ru.elessarov.survey_bot.constants.Constants.*;

@Configuration
@AllArgsConstructor
@Slf4j
public class BotConfiguration {


    private final BotProperties botProperties;

    @Bean
    @ConditionalOnProperty(name = BOT_TYPE, havingValue = LONG_POLLING_BOT_TYPE, matchIfMissing = true)
    public LongPollingBot longPollingBot(
            BotProperties botProperties,
            CommandHandler commandHandler,
            CallbackHandler callbackHandler,
            FileHandler fileHandler
    ) {
        log.info("App started with Long polling bot");
        return new LongPollingBot(botProperties, commandHandler, callbackHandler, fileHandler);
    }

    @Bean
    @ConditionalOnProperty(name = BOT_TYPE, havingValue = WEBHOOK_BOT_TYPE)
    public SetWebhook setWebhook() {
        log.info("Setting webhook on {}", botProperties.getWebhookUrl());
        return SetWebhook.builder()
                .url(botProperties.getWebhookUrl())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = BOT_TYPE, havingValue = WEBHOOK_BOT_TYPE)
    public WebhookBot webhookBot(
            BotProperties botProperties,
            SetWebhook setWebhook,
            CommandHandler commandHandler,
            CallbackHandler callbackHandler,
            FileHandler fileHandler
    ) {
        log.info("App started with webhook bot");
        return new WebhookBot(botProperties, setWebhook, commandHandler, callbackHandler, fileHandler);
    }


    @Bean
    @ConditionalOnProperty(name = BOT_TYPE, havingValue = LONG_POLLING_BOT_TYPE,  matchIfMissing = true)
    public TelegramBotsApi telegramLongPollingBotsApi(LongPollingBot bot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        return botsApi;
    }

    @Bean
    @ConditionalOnProperty(name = BOT_TYPE, havingValue = WEBHOOK_BOT_TYPE)
    public TelegramBotsApi telegramWebhookBotsApi(SpringWebhookBot webhookBot, SetWebhook setWebhook) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(webhookBot, setWebhook);
        return botsApi;
    }
}
