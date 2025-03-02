package ru.elessarov.survey_bot.survey_bot;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.elessarov.survey_bot.enumeration.Command;
import ru.elessarov.survey_bot.handler.CallbackHandler;
import ru.elessarov.survey_bot.handler.CommandHandler;
import ru.elessarov.survey_bot.handler.FileHandler;
import ru.elessarov.survey_bot.properties.BotProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Getter
public class WebhookBot extends SpringWebhookBot implements MyBot {
    private final BotProperties botProperties;
    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final FileHandler fileHandler;

    public WebhookBot(
            BotProperties botProperties,
            SetWebhook setWebhook,
            CommandHandler commandHandler,
            CallbackHandler callbackHandler,
            FileHandler fileHandler
    ) {
        super(setWebhook, botProperties.getToken());
        this.botProperties = botProperties;
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.fileHandler = fileHandler;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return getMessage(update);
    }

    @Override
    public String getBotPath() {
        return botProperties.getWebhookPath();
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public File convertDocumentToFile(Document document) {
        String fileId = document.getFileId();
        org.telegram.telegrambots.meta.api.objects.File file;
        File tempFile;
        try {
            file = this.execute(GetFile.builder().fileId(fileId).build());
            tempFile = File.createTempFile("file", null);
            downloadFile(file.getFilePath(), tempFile);
        } catch (IOException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }

    @PostConstruct
    private void setCommands() throws TelegramApiException {
        List<BotCommand> commands = new ArrayList<>();
        Arrays.stream(Command.values())
                .filter(command -> command != Command.UNKNOWN)
                .filter(command -> !command.isOnlyAdmin())
                .forEach(command -> commands.add(new BotCommand(command.getName(), command.getDescription())));
        SetMyCommands setMyCommands = new SetMyCommands(commands, null, null);
        this.execute(setMyCommands);
    }
}
