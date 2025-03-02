package ru.elessarov.survey_bot.handler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.elessarov.survey_bot.enumeration.Action;
import ru.elessarov.survey_bot.model.Survey;
import ru.elessarov.survey_bot.model.UserState;
import ru.elessarov.survey_bot.service.InMemoryStorage;
import ru.elessarov.survey_bot.service.InMemorySurveyStorage;

public interface MessageHandler {

    default boolean isUserHasAnySurveys(String chatId, InMemoryStorage storage) {
        return storage.getUserStates().containsKey(chatId);
    }

    default SendMessage getSurveyMessage(String chatId, String text, Action action, InMemoryStorage storage, InMemorySurveyStorage surveyStorage, boolean saveSkipped) {
        UserState userState = storage.getUserState(chatId);
        Survey survey = surveyStorage.getSurveyByType(userState.getSurveyType());
        initializeSurvey(survey, storage);
        userState.saveAnswer(text, saveSkipped);

        if (action == null || action == Action.NEXT) {
            userState.incrementCurrentQuestionIndex();

            if (survey.isCompleted(userState)) {
                return writeAnswersToSheet(chatId, survey, userState, storage);
            }
            return survey.getNextQuestion(userState, text);
        }

        if (action == Action.SUBQUESTION) {
            return survey.getSubQuestion(userState, text);
        }
        return null;
    }

    default SendMessage getPreviousMessage(String chatId, String text, InMemoryStorage storage, InMemorySurveyStorage surveyStorage) {
        UserState userState = storage.getUserState(chatId);
        userState.decrementCurrentQuestionIndex();
        Survey survey = surveyStorage.getSurveyByType(userState.getSurveyType());
        return survey.getNextQuestion(userState, text);
    }

    default SendMessage completeSurvey(String chatId, String text, InMemoryStorage storage, InMemorySurveyStorage surveyStorage) {
        UserState userState = storage.getUserState(chatId);
        Survey survey = surveyStorage.getSurveyByType(userState.getSurveyType());
        userState.saveAnswer(text, false);
        return writeAnswersToSheet(chatId, survey, userState, storage);

    }

    private SendMessage writeAnswersToSheet(String chatId, Survey survey, UserState userState, InMemoryStorage storage) {
        storage.writeAnswersToSheet(userState, survey);
        storage.deleteUserState(userState);
        return new SendMessage(chatId, "Опрос завершен");
    }

    private void initializeSurvey(Survey survey, InMemoryStorage storage) {
        storage.initializeSurvey(survey);
    }
}
