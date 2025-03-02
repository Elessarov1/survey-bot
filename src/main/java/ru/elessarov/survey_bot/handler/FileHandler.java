package ru.elessarov.survey_bot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.elessarov.survey_bot.model.Survey;
import ru.elessarov.survey_bot.service.InMemorySurveyStorage;
import ru.elessarov.survey_bot.service.ValidationService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
@AllArgsConstructor
public class FileHandler {

    private final ObjectMapper objectMapper;
    private final InMemorySurveyStorage inMemorySurveyStorage;
    private final ValidationService validationService;

    public SendMessage updateData(String chatId, File file){
        if (validationService.isNotGlobalAdmin(chatId)){
            return new SendMessage(chatId, CommandHandler.YOU_ARE_NOT_GLOBAL_ADMIN);
        }
        Survey survey = extractSurveyFromFile(file);
        inMemorySurveyStorage.add(survey);
        return new SendMessage(chatId, "Опрос успешно добавлен");
    }

    private Survey extractSurveyFromFile(File file){
        try {
            String jsonString = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return objectMapper.readValue(jsonString, Survey.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
