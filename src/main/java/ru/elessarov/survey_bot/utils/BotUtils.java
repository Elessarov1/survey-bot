package ru.elessarov.survey_bot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.elessarov.survey_bot.enumeration.Action;
import ru.elessarov.survey_bot.model.Button;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BotUtils {
    public static String getChatId(Update update) {
        return update.getMessage().getChatId().toString();
    }

    public static String getCallbackChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId().toString();
    }

    public static String getUserNameFromCallback(Update update) {
        return update.getCallbackQuery().getFrom().getUserName();
    }

    /**
     * Метод добавления кнопок в один ряд к сообщению от бота
     * @param sendMessage - объект сообщения
     * @param keyBoardList - список кнопок
     */
    public static void addInlineKeyBoard(SendMessage sendMessage, List<Button> keyBoardList, boolean withBackButton) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();

        for (int i = 0; i < keyBoardList.size(); i++) {
            Button button = keyBoardList.get(i);
            if (button.name().length() > 30) {
                throw new RuntimeException("Текст в кнопке '%s' не должен быть больше 30 символов".formatted(button.name()));
            }
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(button.name());
            inlineKeyboardButton.setCallbackData(button.data());
            keyboardButtonsRow.add(inlineKeyboardButton);

            if((i + 1) % 2 == 0 || i == keyBoardList.size() - 1) {
                rowList.add(new ArrayList<>(keyboardButtonsRow));
                keyboardButtonsRow.clear();
            }
        }

        if (withBackButton) {
            addBackButton(rowList, keyboardButtonsRow);
        }

        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

    private static void addBackButton(List<List<InlineKeyboardButton>> rowList, List<InlineKeyboardButton> keyboardButtonsRow) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("◀\uFE0F Назад");
        button.setCallbackData(Action.BACK.name() + "_" + button.getText());
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);
    }

    /**
     * Метод добавления кнопок к сообщению от бота
     * @param sendMessage - объект сообщения
     * @param keyBoardList - список кнопок
     */
    public static void addKeyBoard(SendMessage sendMessage, List<String> keyBoardList) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        if (keyBoardList.size() < 3) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.addAll(keyBoardList);
            keyboardRows.add(keyboardRow);
        } else {
            for (String item: keyBoardList) {
                KeyboardRow keyboardRow = new KeyboardRow();
                keyboardRow.add(item);
                keyboardRows.add(keyboardRow);
            }
        }
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }

    /**
     * Метод удаления клавиатуры с экрана пользователя
     * @param sendMessage - объект сообщения
     */
    public static void removeKeyboard(SendMessage sendMessage) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);
    }
}
