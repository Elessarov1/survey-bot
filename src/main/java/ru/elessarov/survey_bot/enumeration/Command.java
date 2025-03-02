package ru.elessarov.survey_bot.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Command {
    START("/start", "Приветствие", "Привет! Выберите одну из команд:", false),
    ADD_ADMIN("/add_admin", "Добавление администратора", "", true),
    DELETE_ADMIN("/delete_admin", "удаление администратора", "", true),
    SHOW_ADMIN("/show_admin", "Вывести список администраторов", "", true),
    SHOW_ADMIN_COMMAND("/show_admin_command", "Показать команды для администраторов", "", false),
    SHOW_SURVEYS("/show_surveys", "Показать доступные вопросы", "Вы всегда можете отменить опрос командой - /cancel_survey\n\nВыберете опрос:", false),
    CANCEL_SURVEY("/cancel_survey","Отменить начатый опрос","Опрос успешно отменен",false),
    DELETE_SURVEY("/delete_survey","Удалить опрос","Какой опрос удалить",true),
    UNKNOWN("/неизветсная команда", "Заглушка", "Бот не знает такой команды!", false);

    private final String name;
    private final String description;
    private final String answer;
    private final boolean onlyAdmin;

    Command(String name, String description, String answer, boolean onlyAdmin) {
        this.name = name;
        this.description = description;
        this.answer = answer;
        this.onlyAdmin = onlyAdmin;
    }

    public static List<Command> getUserCommands() {
        return Arrays.stream(Command.values())
                .filter(command -> !command.onlyAdmin)
                .filter(command -> command != Command.START)
                .filter(command -> command != Command.UNKNOWN)
                .toList();
    }

    public static Command of(String text) {
        return Arrays.stream(Command.values())
                .filter(command -> command.getName().equals(text))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
