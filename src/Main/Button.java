package Main;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

public class Button {
    //Создание кнопок
    protected InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("Мой профиль").callbackData("profile");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Друзья и близкие").callbackData("friends");
        return new InlineKeyboardMarkup(new InlineKeyboardButton[]{button1, button2});
    }

    protected InlineKeyboardMarkup createBackButtonProfile() {
        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад").callbackData("backProfile");
        return new InlineKeyboardMarkup(backButton);
    }
    protected InlineKeyboardMarkup createBackButtonFriends() {
        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад").callbackData("backFriends");
        return new InlineKeyboardMarkup(backButton);
    }

    protected InlineKeyboardMarkup createСonfirmationButton() {
        InlineKeyboardButton backButton = new InlineKeyboardButton("✔\uFE0F").callbackData("сonfirmation");
        return new InlineKeyboardMarkup(backButton);
    }


    protected InlineKeyboardMarkup createEditProfileButton() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("Измень дату рождения").callbackData("editProfile");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Список подарков").callbackData("ListGiftProfile");
        InlineKeyboardButton button3 = new InlineKeyboardButton("Добавить подарок").callbackData("addGiftProfile");
        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад").callbackData("back");
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{button1},
                new InlineKeyboardButton[]{button2},
                new InlineKeyboardButton[]{button3},
                new InlineKeyboardButton[]{backButton}
        );
    }

    protected InlineKeyboardMarkup createEditFriendsButton() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("Добавить").callbackData("addFriends");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Удалить").callbackData("deleteFriends");
        InlineKeyboardButton buttonShowList = new InlineKeyboardButton("Список подарков человека").callbackData("showList");
        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад").callbackData("back");
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{button1, button2},
                new InlineKeyboardButton[]{buttonShowList},
                new InlineKeyboardButton[]{backButton}
        );
    }

    protected InlineKeyboardMarkup createBackGiftsButton() {
        InlineKeyboardButton backButton = new InlineKeyboardButton(" ⬅\uFE0F").callbackData("backGifts");
        return new InlineKeyboardMarkup(backButton);
    }
}
