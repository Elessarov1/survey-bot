package ru.elessarov.survey_bot.survey_bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.elessarov.survey_bot.handler.CallbackHandler;
import ru.elessarov.survey_bot.handler.CommandHandler;
import ru.elessarov.survey_bot.handler.FileHandler;
import ru.elessarov.survey_bot.utils.BotUtils;

import java.io.File;

public interface MyBot {
    CommandHandler getCommandHandler();
    CallbackHandler getCallbackHandler();
    FileHandler getFileHandler();
    File convertDocumentToFile(Document document);


    default SendMessage getMessage(Update update) {
        SendMessage message = null;
        if (isDocumentMessage(update)) {
            var file = convertDocumentToFile(update.getMessage().getDocument());
            message = getFileHandler().updateData(BotUtils.getChatId(update), file);
        }
        if (isTextMessage(update)) {
            message = getCommandHandler().handlerCommands(update);
        }
        if (isCallbackQuery(update)) {
            message = getCallbackHandler().handleCallback(update);
        }
        return message;
    }

    default boolean isTextMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    default boolean isCallbackQuery(Update update) {
        return update.hasCallbackQuery();
    }

    default boolean isDocumentMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasDocument();
    }
}
