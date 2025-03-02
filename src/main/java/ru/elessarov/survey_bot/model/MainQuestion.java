package ru.elessarov.survey_bot.model;

import lombok.Getter;
import lombok.Setter;
import ru.elessarov.survey_bot.enumeration.QuestionLevel;
import ru.elessarov.survey_bot.enumeration.QuestionType;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MainQuestion implements Question {

    private final String text;
    private final QuestionType type;
    private final List<Option> options;
    private final QuestionLevel level;
    private final Map<String, SubQuestion> subQuestions;

    public MainQuestion(String text, QuestionType type, List<Option> options, Map<String, SubQuestion> subQuestions) {
        this.text = text;
        this.type = type;
        this.options = options;
        this.level = QuestionLevel.MAIN;
        this.subQuestions = subQuestions;
    }

    public boolean isChoiceQuestion() {
        return type == QuestionType.CHOICE;
    }

    public boolean hasSubQuestions() {
        return subQuestions != null && !subQuestions.isEmpty();
    }

    public List<Button> getButtons() {
        if (type == QuestionType.FREE_TEXT) {
            throw new RuntimeException("Can't create buttons for question type " + type);
        }
        return options.stream()
                .map(option -> new Button(
                        option.value(),
                        hasSubQuestions()
                                ? option.getSubquestionCallbackData()
                                : option.getCallbackData()
                )).toList();
    }

}
