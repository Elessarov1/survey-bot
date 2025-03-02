package ru.elessarov.survey_bot.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.elessarov.survey_bot.enumeration.QuestionLevel;
import ru.elessarov.survey_bot.enumeration.QuestionType;

import java.util.List;
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "level"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MainQuestion.class, name = "MAIN"),
        @JsonSubTypes.Type(value = SubQuestion.class, name = "SUB")
})
public interface Question {

    String getText();
    QuestionType getType();
    QuestionLevel getLevel();
    List<Button> getButtons();


    default boolean isChoiceQuestion() {
        return getType() == QuestionType.CHOICE;
    }

}
