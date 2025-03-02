package ru.elessarov.survey_bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.elessarov.survey_bot.enumeration.AdminStateType;

@AllArgsConstructor
@Getter
@Setter
public class AdminState {
    private String chatId;
    private AdminStateType type;
}
