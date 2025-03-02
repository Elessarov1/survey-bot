package ru.elessarov.survey_bot.survey_bot;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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

@AllArgsConstructor
@Getter
public class LongPollingBot extends TelegramLongPollingBot implements MyBot {
    private final BotProperties botProperties;
    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final FileHandler fileHandler;

    @Override
    public void onUpdateReceived(Update update) {
       sendMessage(getMessage(update));
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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
