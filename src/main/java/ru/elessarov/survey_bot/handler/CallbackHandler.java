package ru.elessarov.survey_bot.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.elessarov.survey_bot.enumeration.Action;
import ru.elessarov.survey_bot.model.Survey;
import ru.elessarov.survey_bot.model.UserState;
import ru.elessarov.survey_bot.service.InMemoryStorage;
import ru.elessarov.survey_bot.service.InMemorySurveyStorage;
import ru.elessarov.survey_bot.utils.BotUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
public class CallbackHandler implements MessageHandler{
    private static final String UNKNOWN_BUTTON = "Неизвестная кнопка";
    private static final Pattern SURVEY_TYPE_PATTERN = Pattern.compile("_(.+)");
    private static final Pattern DELETE_SURVEY_PATTERN = Pattern.compile("^[^_]+");

    private final InMemoryStorage inMemoryStorage;
    private final InMemorySurveyStorage surveyStorage;

    public SendMessage handleCallback(Update update) {
        String chatId = BotUtils.getCallbackChatId(update);
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData != null) {
            Action action = extractAction(callbackData);
            String value = extractSurveyType(callbackData);
           return switch (action) {
               case DELETE -> deleteSurveyByType(chatId, value);
               case START -> startSurvey(chatId, value);
               case NEXT, SUBQUESTION -> toTheNextQuestion(chatId, value, action, Boolean.FALSE);
               case SKIP -> skipQuestion(chatId, value);
               case FLUSH -> completeSurvey(chatId, value, inMemoryStorage, surveyStorage);
               case BACK -> getPreviousMessage(chatId, value, inMemoryStorage, surveyStorage);
               case null -> handleUnknown(chatId);
           };
        }
        return handleUnknown(chatId);
    }

    private SendMessage skipQuestion(String chatId, String value) {
        return toTheNextQuestion(chatId, value, Action.NEXT, Boolean.TRUE);
    }

    private SendMessage toTheNextQuestion(String chatId, String value, Action action, Boolean saveSkipped) {
        return getSurveyMessage(chatId, value, action, inMemoryStorage, surveyStorage, saveSkipped);
    }

    private SendMessage startSurvey(String chatId, String value) {
        Survey survey = surveyStorage.getSurveyByType(value);
        if (survey != null) {
            UserState userState = new UserState(chatId, value);
            inMemoryStorage.addUserState(userState);
            return survey.getNextQuestion(userState, value);
        }
        return handleUnknown(chatId);
    }

    private SendMessage deleteSurveyByType(String chatId, String value) {
        surveyStorage.deleteSurveyByType(value);
        return new SendMessage(chatId, "Опрос успешно удален");
    }

    private Action extractAction(String callbackData) {
        Matcher matcher = DELETE_SURVEY_PATTERN.matcher(callbackData);
        if (matcher.find()) {
            return Action.valueOf(matcher.group());
        }
        return null;
    }

    private String extractSurveyType(String callbackData) {
        Matcher matcher = SURVEY_TYPE_PATTERN.matcher(callbackData);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private SendMessage handleUnknown(String chatId) {
        return new SendMessage(chatId, UNKNOWN_BUTTON);
    }
}
