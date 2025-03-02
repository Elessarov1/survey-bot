package ru.elessarov.survey_bot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class UserAnswer {

    private String mainAnswer;
    private Map<SubQuestion, String> subAnswers;

    public UserAnswer(String mainAnswer) {
        this.mainAnswer = mainAnswer;
        this.subAnswers = new HashMap<>();
    }

    public void saveSubAnswers(SubQuestion key, String value) {
        subAnswers.put(key, value);
    }
}
