package ru.elessarov.survey_bot.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.elessarov.survey_bot.survey_bot.WebhookBot;

import static ru.elessarov.survey_bot.constants.Constants.BOT_TYPE;
import static ru.elessarov.survey_bot.constants.Constants.WEBHOOK_BOT_TYPE;

@RestController
@RequestMapping
@AllArgsConstructor
@Slf4j
@ConditionalOnProperty(name = BOT_TYPE, havingValue = WEBHOOK_BOT_TYPE)
public class WebhookController {
    private final WebhookBot webhookBot;

    @PostMapping("/webhook")
    public ResponseEntity<BotApiMethod<?>> onUpdateReceived(@RequestBody Update update) {
        BotApiMethod<?> response = webhookBot.onWebhookUpdateReceived(update);
        return ResponseEntity.ok(response);
    }
}
