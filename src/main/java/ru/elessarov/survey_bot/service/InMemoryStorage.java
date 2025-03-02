package ru.elessarov.survey_bot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.elessarov.survey_bot.model.AdminState;
import ru.elessarov.survey_bot.model.Survey;
import ru.elessarov.survey_bot.model.UserState;
import ru.elessarov.survey_bot.properties.AdminProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
@Getter
public class InMemoryStorage {

    private final GoogleApiService googleApiService;
    private final Map<String, AdminState> adminStates = new HashMap<>();
    private final Map<String, UserState> userStates = new HashMap<>();
    private final AdminProperties adminProperties;
    private final Set<String> adminIds = new HashSet<>();

    public void writeAnswersToSheet(UserState userState, Survey survey){
        googleApiService.writeAnswersToSheet(userState, survey, false);
    }

    public void initializeSurvey(Survey survey){
        if (!survey.isInitialized()) {
            googleApiService.writeAnswersToSheet(null, survey, true);
        }
    }

    public void addAdminState(AdminState adminState) {
        adminStates.put(adminState.getChatId(), adminState);
    }

    public void deleteAdminState(AdminState adminState) {
        adminStates.remove(adminState.getChatId());
    }

    public void addUserState(UserState userState) {
        userStates.put(userState.getChatId(), userState);
    }

    public void deleteUserState(UserState userState) {
        userStates.remove(userState.getChatId());
    }

    public void deleteUserState(String chatId) {
        userStates.remove(chatId);
    }

    public UserState getUserState(String chatId) {
        return userStates.get(chatId);
    }

    public void deleteAdmin(String chatId){
        adminIds.remove(chatId);
    }

    public void addAdmin(String chatId){
        adminIds.add(chatId);
    }
}
