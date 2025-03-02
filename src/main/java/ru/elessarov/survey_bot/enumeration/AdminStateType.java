package ru.elessarov.survey_bot.enumeration;

import lombok.Getter;

@Getter
public enum AdminStateType {
    DELETE_ADMIN("Удалить администратора"),
    ADD_ADMIN("Добавить администратора");

    private final String answer;

    AdminStateType (String answer) {
        this.answer = answer;
    }
}
