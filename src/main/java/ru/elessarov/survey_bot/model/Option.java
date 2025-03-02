package ru.elessarov.survey_bot.model;

import ru.elessarov.survey_bot.enumeration.Action;

import java.util.Objects;

public record Option(
        String value,
        Action action
) {
    public String getCallbackData() {
        return Objects.requireNonNullElse(action, Action.NEXT).name() + "_" + value;
    }

    public String getSubquestionCallbackData() {
        return Objects.requireNonNullElse(action, Action.SUBQUESTION).name() + "_" + value;
    }
}
