package ru.elessarov.survey_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.elessarov.survey_bot.properties.AdminProperties;
import ru.elessarov.survey_bot.properties.BotProperties;

@SpringBootApplication
@EnableConfigurationProperties({BotProperties.class, AdminProperties.class})
public class SurveyBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurveyBotApplication.class, args);
    }

}
