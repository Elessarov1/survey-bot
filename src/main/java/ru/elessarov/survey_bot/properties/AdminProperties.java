package ru.elessarov.survey_bot.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("admin")
public class AdminProperties {
    private String chatId;
}
