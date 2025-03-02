package ru.elessarov.survey_bot.model;

import lombok.Getter;
import lombok.Setter;
import ru.elessarov.survey_bot.enumeration.QuestionLevel;
import ru.elessarov.survey_bot.enumeration.QuestionType;

import java.util.List;

@Getter
@Setter
public class SubQuestion implements Question{

    private final String text;
    private final QuestionType type;
    private final List<Option> options;
    private final QuestionLevel level;

    private MainQuestion mainQuestion;

    public SubQuestion(String text, QuestionType type, List<Option> options) {
        this.text = text;
        this.type = type;
        this.options = options;
        this.level = QuestionLevel.SUB;
    }

    @Override
    public List<Button> getButtons() {
        if (type == QuestionType.FREE_TEXT) {
            throw new RuntimeException("Can't create buttons for question type " + type);
        }
        return options.stream()
                .map(option -> new Button(option.value(), option.getCallbackData()))
                .toList();
    }
}
