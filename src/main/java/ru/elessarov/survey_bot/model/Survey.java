package ru.elessarov.survey_bot.model;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.elessarov.survey_bot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Survey {
    private String type;
    private List <Question> questions;
    private String link;
    private boolean testing;
    private boolean initialized;

    public List<Object> getNamesForInitializing(){
        List<Object> names = new ArrayList<>();
        names.add("id");
        names.add("Дата");
        for (Question question : questions) {
            names.add(question.getText());
            if (question instanceof MainQuestion mainQuestion) {
                if (mainQuestion.hasSubQuestions()) {
                    names.add(question.getText() + " (уточняющий вопрос)");
                }
            }
        }
        return names;
    }

    public SendMessage getNextQuestion(UserState userState, String userAnswer) {
        int currentIndex = userState.getCurrentQuestionIndex();
        if (currentIndex >= questions.size()) {
            return null;
        }
        Question question = questions.get(currentIndex);
        userState.setCurrentQuestion((MainQuestion) question);
        return createNewQuestionMessage(question, userState, userAnswer);
    }

    public SendMessage getSubQuestion(UserState userState, String userAnswer) {
        int currentIndex = userState.getCurrentQuestionIndex();
        MainQuestion mainQuestion = (MainQuestion) questions.get(currentIndex);
        SubQuestion subQuestion = mainQuestion.getSubQuestions().get(userAnswer);
        userState.setSubQuestion(subQuestion);
        return createNewQuestionMessage(subQuestion, userState, userAnswer);
    }

    public boolean isCompleted(UserState userState) {
        return userState.isCompleted(questions.size());
    }

    private SendMessage createFreeTextMessage(String chatId, Question question, String userAnswer) {
        SendMessage message = new SendMessage(chatId, createQuestionText(userAnswer, question.getText()));
        BotUtils.addInlineKeyBoard(message, Collections.emptyList(), true);
        return message;
    }

    private SendMessage createChoiceMessage(String chatId, Question question, String userAnswer) {
        SendMessage message = new SendMessage(chatId, createQuestionText(userAnswer, question.getText()));
        BotUtils.addInlineKeyBoard(message, question.getButtons(), true);
        return message;
    }

    private String createQuestionText(String userAnswer, String questionText) {
        return "Предыдущий ответ: %s\n\n%s".formatted(userAnswer, questionText);
    }

    private SendMessage createNewQuestionMessage(Question question, UserState userState, String answer) {
        if (question.isChoiceQuestion()) {
            return createChoiceMessage(userState.getChatId(), question, answer);
        } else {
            return createFreeTextMessage(userState.getChatId(), question, answer);
        }
    }
}
