package ru.elessarov.survey_bot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.elessarov.survey_bot.model.Survey;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Service
@Slf4j
public class InMemorySurveyStorage {

    private final Map<String, Survey> surveys = new HashMap<>();
    private final ObjectMapper objectMapper;

    /**
     * Загружает опросы из файла, расположенного в classpath.
     * @param resourcePath путь к ресурсу, например "surveys.json"
     */
    public void loadSurveyFromClasspath(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                List<Survey> surveyList = objectMapper.readValue(inputStream, new TypeReference<>() {
                });
                surveyList.forEach(this::add);
                log.info("Успешно загружено {} опросов из ресурса {}", surveyList.size(), resourcePath);
            }
        } catch (IOException e) {
            log.error("Не удалось загрузить опросы из ресурса {}: {}", resourcePath, e.getMessage());
        }
    }

    public Survey getSurveyByType(String type) {
        return surveys.get(type);
    }

    public Survey add(Survey survey) {
        surveys.put(survey.getType(), survey);
        log.info("Добавлен новый опрос: {}", survey.getType());
        return survey;
    }

    public void deleteSurveyByType(String type) {
        surveys.remove(type);
    }

}
