package ru.elessarov.survey_bot.handler;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.elessarov.survey_bot.enumeration.Action;
import ru.elessarov.survey_bot.enumeration.AdminStateType;
import ru.elessarov.survey_bot.enumeration.Command;
import ru.elessarov.survey_bot.model.AdminState;
import ru.elessarov.survey_bot.model.Button;
import ru.elessarov.survey_bot.model.Survey;
import ru.elessarov.survey_bot.properties.AdminProperties;
import ru.elessarov.survey_bot.service.InMemoryStorage;
import ru.elessarov.survey_bot.service.InMemorySurveyStorage;
import ru.elessarov.survey_bot.service.ValidationService;
import ru.elessarov.survey_bot.utils.BotUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j
public class CommandHandler implements MessageHandler {
    public static final String YOU_ARE_NOT_GLOBAL_ADMIN = "Вы не являетесь глобальным админом";
    private static final String YOU_ARE_NOT_ADMIN = "Вы не являетесь админом";

    private final InMemoryStorage inMemoryStorage;
    private final ValidationService validationService;
    private final InMemorySurveyStorage surveyStorage;
    private final AdminProperties adminProperties;

    public SendMessage handlerCommands(Update update) {
        String text = update.getMessage().getText();
        String chatId = BotUtils.getChatId(update);
        Command command = Command.of(text);
        return switch (command) {
            case START -> handleStartCommand(chatId, command.getAnswer());
            case ADD_ADMIN -> handleAddAdmin(chatId);
            case DELETE_ADMIN -> handleDeleteAdmin(chatId);
            case SHOW_ADMIN -> handleShowAdmins(chatId);
            case SHOW_ADMIN_COMMAND -> handleShowAdminCommands(chatId);
            case SHOW_SURVEYS -> handleShowSurveys(chatId, command.getAnswer());
            case CANCEL_SURVEY -> handleCancelSurvey(chatId, command.getAnswer());
            case DELETE_SURVEY -> handleDeleteSurvey(chatId, command.getAnswer());
            case UNKNOWN -> handleUnknownCommand(chatId, command.getAnswer(), text);
        };
    }

    private SendMessage handleDeleteSurvey(String chatId, String answer) {
        if (validationService.isNotGlobalAdmin(chatId) && validationService.isNotAdmin(chatId)) {
            return new SendMessage(chatId, YOU_ARE_NOT_ADMIN);
        }
        List<String> surveyType = surveyStorage.getSurveys().keySet().stream().toList();
        var message = new SendMessage(chatId, answer);
        List<Button> buttons = surveyType.stream()
                .map(type -> new Button(type, Action.DELETE.name() + "_" + type))
                .toList();
        BotUtils.addInlineKeyBoard(message, buttons, false);
        return message;
    }

    private SendMessage handleCancelSurvey(String chatId, String answer) {
        inMemoryStorage.deleteUserState(chatId);
        return new SendMessage(chatId, answer);
    }

    private SendMessage handleShowSurveys(String chatId, String answer) {
        Set<String> adminIds = new HashSet<>(inMemoryStorage.getAdminIds());
        adminIds.add(adminProperties.getChatId());
        List<String> surveyTypes = surveyStorage.getSurveys()
                .values()
                .stream()
                .filter(survey -> adminIds.contains(chatId) || !survey.isTesting())
                .map(Survey::getType)
                .toList();
        if (surveyTypes.isEmpty()) {
            return new SendMessage(chatId, "Нет доступных опросов");
        }
        var message = new SendMessage(chatId, answer);
        List<Button> buttons = surveyTypes.stream()
                .map(type -> new Button(type, Action.START.name() + "_" + type))
                .toList();

        BotUtils.addInlineKeyBoard(message, buttons, false);
        return message;
    }

    private SendMessage handleStartCommand(String chatId, String answer) {
        String commands = Command.getUserCommands().stream()
                .map(command -> command.getName() + " - " + command.getDescription() + "\n")
                .collect(Collectors.joining());
        SendMessage message = new SendMessage(chatId, answer + "\n\n" + commands);
        BotUtils.removeKeyboard(message);
        return message;
    }

    private SendMessage handleShowAdminCommands(String chatId) {
        if (validationService.isNotGlobalAdmin(chatId) && validationService.isNotAdmin(chatId)) {
            return new SendMessage(chatId, YOU_ARE_NOT_ADMIN);
        }
        String commands = Arrays.stream(Command.values())
                .filter(command -> command != Command.UNKNOWN)
                .filter(Command::isOnlyAdmin)
                .map(command -> command.getName() + " - " + command.getDescription())
                .collect(Collectors.joining("\n"));
        return new SendMessage(chatId, commands);
    }

    private SendMessage handleShowAdmins(String chatId) {
        if (validationService.isNotGlobalAdmin(chatId)) {
            return new SendMessage(chatId, YOU_ARE_NOT_GLOBAL_ADMIN);
        }
        var admins = inMemoryStorage.getAdminIds();
        if (admins.isEmpty()) {
            return new SendMessage(chatId, "Список админов пуст");
        }
        return new SendMessage(chatId, String.join("\n", admins));
    }

    private SendMessage handleDeleteAdmin(String chatId) {
        if (validationService.isNotGlobalAdmin(chatId)){
            return new SendMessage(chatId,YOU_ARE_NOT_GLOBAL_ADMIN);
        }
        if (inMemoryStorage.getAdminIds().isEmpty()) {
            return new SendMessage(chatId, "Список админов пуст");
        }
        AdminState adminState = new AdminState(chatId, AdminStateType.DELETE_ADMIN);
        inMemoryStorage.addAdminState(adminState);
        return new SendMessage(chatId, "Введите список админов");
    }

    private SendMessage handleAddAdmin(String chatId) {
        if (validationService.isNotGlobalAdmin(chatId)){
            return new SendMessage(chatId,YOU_ARE_NOT_GLOBAL_ADMIN);
        }
        AdminState adminState = new AdminState(chatId, AdminStateType.ADD_ADMIN);
        inMemoryStorage.getAdminStates().put(chatId, adminState);
        return new SendMessage(chatId, "Введите список админов");
    }

    private SendMessage handleUnknownCommand(String chatId, String answer, String text) {
        var states = inMemoryStorage.getAdminStates();
        if (isAdminFreeText(chatId, text, states)) {
            var state = states.get(chatId);
            return switch (state.getType()) {
                case ADD_ADMIN -> addAdmin(chatId, text, state);
                case DELETE_ADMIN -> deleteAdmin(chatId, text, state);
            };
        }
        if (isUserHasAnySurveys(chatId, inMemoryStorage)){
            return getSurveyMessage(chatId, text, null, inMemoryStorage, surveyStorage, Boolean.FALSE);
        }
        return new SendMessage(chatId,answer);
    }

    private boolean isAdminFreeText(String chatId, String text, Map<String, AdminState> states) {
        return !text.isBlank()
                && (validationService.isGlobalAdmin(chatId) || validationService.isAdmin(chatId))
                && states.containsKey(chatId);
    }

    private SendMessage addAdmin(String chatId, String text, AdminState state) {
        List<String> adminIDs = List.of(text.split(","));
        Collection<String> actualIDs = inMemoryStorage.getAdminIds();
        actualIDs.addAll(adminIDs);
        inMemoryStorage.deleteAdminState(state);
        return new SendMessage(chatId, String.join("\n", actualIDs));
    }

    private SendMessage deleteAdmin(String chatId, String text, AdminState state) {
        inMemoryStorage.deleteAdmin(text);
        inMemoryStorage.deleteAdminState(state);
        return new SendMessage(chatId, "Администратор успешно удален");
    }
}
