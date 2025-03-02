package ru.elessarov.survey_bot.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("telegram.bot")
public class BotProperties {
    private String type;
    private String name;
    private String token;
    private String webhookPath;
    private String webhookUrl;
}
