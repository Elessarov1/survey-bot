package ru.elessarov.survey_bot.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.elessarov.survey_bot.properties.AdminProperties;

@Service
@AllArgsConstructor
public class ValidationService {

    private final AdminProperties adminProperties;
    private final InMemoryStorage inMemoryStorage;

    public boolean isNotGlobalAdmin(String chatId) {
        return !adminProperties.getChatId().equals(chatId);
    }

    public boolean isNotAdmin(String chatId) {
        return !inMemoryStorage.getAdminIds().contains(chatId);
    }

    public boolean isGlobalAdmin(String chatId) {
        return adminProperties.getChatId().equals(chatId);
    }

    public boolean isAdmin(String chatId) {
        return inMemoryStorage.getAdminIds().contains(chatId);
    }
}
