package ru.elessarov.survey_bot.service;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SurveyInitializer implements CommandLineRunner {

    private static final String INITIAL_SURVEY_FILE = "surveys.json";
    private final InMemorySurveyStorage inMemorySurveyStorage;

    @Override
    public void run(String... args) {
        inMemorySurveyStorage.loadSurveyFromClasspath(INITIAL_SURVEY_FILE);
    }
}
