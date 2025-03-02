package ru.elessarov.survey_bot.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserState {

   private final String chatId;
   private final LocalDate date;
   private final String surveyType;
   private final Map<Question, UserAnswer> answers;

   private int currentQuestionIndex;
   private boolean completed;
   private MainQuestion currentQuestion;
   private SubQuestion subQuestion;

   public UserState(String chatId, String surveyType) {
       this.chatId = chatId;
       this.date = LocalDate.now();
       this.surveyType = surveyType;
       this.answers = new LinkedHashMap<>();
       this.currentQuestionIndex = 0;
       this.completed = false;
   }

   public void incrementCurrentQuestionIndex() {
       currentQuestionIndex++;
   }

    public void decrementCurrentQuestionIndex() {
       if (currentQuestionIndex <= 0) {
           return;
       }
        currentQuestionIndex--;
       removeAnswersBeyondCurrentIndex();
       subQuestion = null;
    }

    private void removeAnswersBeyondCurrentIndex() {
       List<Question> questionList = new ArrayList<>(answers.keySet());
       for (int i = questionList.size() - 1; i >= currentQuestionIndex; i--) {
           Question questionToRemove = questionList.get(i);
           answers.remove(questionToRemove);
       }
    }

   public void saveAnswer(String answer, boolean saveSkipped) {
       if (subQuestion != null) {
           answers.get(currentQuestion).saveSubAnswers(subQuestion, answer);
           subQuestion = null;
           return;
       }
       if (currentQuestion != null) {
           answers.put(currentQuestion, new UserAnswer(answer));
           if (saveSkipped) {
               answers.get(currentQuestion).getSubAnswers().put(currentQuestion.getSubQuestions().get(answer), "-");
           }
       }
   }

   public boolean isCompleted(int totalQuestions) {
       return  completed = currentQuestionIndex >= totalQuestions;
   }

   public List<Object> getAnswersRawData() {
       List<Object> rawData = new ArrayList<>();
       rawData.add(chatId);
       rawData.add(date.toString());
       for (UserAnswer userAnswer: answers.values()) {
           rawData.add(userAnswer.getMainAnswer());
           if (!userAnswer.getSubAnswers().isEmpty()) {
               rawData.addAll(userAnswer.getSubAnswers().values());
           }
       }
       return rawData;
   }
}
