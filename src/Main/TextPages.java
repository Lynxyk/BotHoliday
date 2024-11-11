package Main;

import Main.logging.Log;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import db.entity.Gift;
import db.Postgre;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TextPages {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    Log log = new Log();
    Button button = new Button();

    Postgre postgre;

    {
        try {
            postgre = new Postgre("postgres", "12345", "localhost");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Текстовые странички
    String hello = "Привет! \uD83C\uDF89 Я твой новый телеграмм бот, созданный, чтобы сделать дни рождения особенными!\n" +
            "Вот что я умею:\n" +
            "Запоминаю твой день рождения и твои пожелания по подаркам \uD83C\uDF81\n" +
            "Помогаю друзьям и близким узнать о твоем дне рождения и твоих пожеланиях \uD83C\uDF82\n" +
            "Напоминаю тебе о днях рождения твоих друзей и близких, чтобы ты не пропустил важный день \uD83D\uDDD3\uFE0F\n" +
            "Давай начнем:\n" +
            "Нажми на кнопку \"Профиль\", чтобы заполнить информацию о себе.\n" +
            "Нажми на кнопку \"Близкие\", чтобы посмотреть даты дней рождения друзей и их списки подарков.\n";

    public void start(long chatId, TelegramBot bot){
        SendMessage message = new SendMessage(chatId, hello);
        message.replyMarkup(button.createInlineKeyboard());
        SendResponse response = bot.execute(message);
        log.logChat(String.format("[%s] Bot: Приветственное сообщение отправлено.", dtf.format(LocalDateTime.now())));
    }

    public void backStart(long chatId, TelegramBot bot, int messageId){
        String responseText = hello;
        EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText)
                .replyMarkup(button.createInlineKeyboard());
        bot.execute(newMessage);
        log.logChat(String.format("[%s] Bot: Возврат к начальному сообщению.", dtf.format(LocalDateTime.now())));
    }

    public void unknown(long chatId, TelegramBot bot, int messageId){
        SendMessage message = new SendMessage(chatId, "Извините, я не понимаю эту команду");
        message.replyMarkup(button.createСonfirmationButton());
        SendResponse response = bot.execute(message);
        HolidayBot.saveMessageId(chatId, response.message().messageId(), HolidayBot.botMessages);
    }

    public void profilePage(long chatId, TelegramBot bot, int messageId, String userName){

        try {
            if (postgre.availabilityUser("@" + userName)) {
                String responseText = String.format("Мой профиль @%s \n" +
                        "Дата рождения: " + postgre.showUser("@" +userName)
                        , userName);
                EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createEditProfileButton());
                bot.execute(newMessage);
            } else {
                String responseText = String.format("Мой профиль @%s \n" +
                                "Дата рождения: xx.xx.xxxx"
                        , userName);
                EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createEditProfileButton());
                bot.execute(newMessage);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void friendsPage(long chatId, TelegramBot bot, int messageId, String userName){
        List<String> listFriends;
        String friends = "Список ваших друзей:";


        try {
            listFriends = postgre.showFriends("@" + userName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < listFriends.size(); i++){
            try {
                friends += "\n" + (i+1) + ". " + listFriends.get(i) + " | " + postgre.showUser(listFriends.get(i));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(friends);

        assert friends != null;
        String responseText = String.format(friends);
        EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createEditFriendsButton());
        bot.execute(newMessage);
        log.logChat(String.format("[%s] Bot: Сообщение обновлено (кнопка friends).", dtf.format(LocalDateTime.now())));
    }

    public void editDateProfilePage(long chatId, TelegramBot bot, int messageId, String userName){
        String responseText = String.format("Введите дату в формате дд.мм.гггг");
        EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createBackButtonProfile());
        bot.execute(newMessage);
    }
    public void addListProfilePage(long chatId, TelegramBot bot, int messageId, String userName){
        String responseText = String.format("Отправьте желаемое в таком формате: \nназвание ссылка");
        EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createBackButtonProfile());
        bot.execute(newMessage);
    }
    public void addFriendsPage(long chatId, TelegramBot bot, int messageId, String userName){
        String responseText = String.format("Добавление человека в список \n - Введите тег в формате @example - ");
        EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createBackButtonFriends());
        bot.execute(newMessage);
    }
    public void deleteFriendsPage(long chatId, TelegramBot bot, int messageId, String userName){
        String responseText = String.format("Удаление человека из списка \n - Введите тег в формате @example - ");
        EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createBackButtonFriends());
        bot.execute(newMessage);
    }

    public void choiceListFriendsPage(long chatId, TelegramBot bot, int messageId, String userName){
        String responseText = String.format("Показ списка подарков \n - Введите тег в формате @example - ");
        EditMessageText newMessage = new EditMessageText(chatId, messageId, responseText).replyMarkup(button.createBackButtonFriends());
        bot.execute(newMessage);
    }

    public void finishPage(long chatId, TelegramBot bot, String text){
        SendMessage message = new SendMessage(chatId, text);
        message.replyMarkup(button.createСonfirmationButton());
        SendResponse response = bot.execute(message);
        HolidayBot.saveMessageId(chatId, response.message().messageId(), HolidayBot.botMessages);
    }
    public void backForAllGiftPage(long chatId, TelegramBot bot){
        SendMessage message = new SendMessage(chatId, "Вернуться назад");
        message.replyMarkup(button.createBackGiftsButton());
        SendResponse response = bot.execute(message);
        HolidayBot.saveMessageId(chatId, response.message().messageId(), HolidayBot.botMessages);
    }
    public void showListFriendsPage(long chatId, TelegramBot bot, int messageId, String userName, List<Gift> gifts){
        String text = "Список пользователя " + userName;
        for (int i = 0; i < gifts.size(); i++){
            text += "\n" + "Подарок №" + (i+1) + "\n" +
                    gifts.get(i).getName() + " | " + gifts.get(i).getLink();
        }
        SendMessage message = new SendMessage(chatId, text);
        message.replyMarkup(button.createСonfirmationButton());
        SendResponse response = bot.execute(message);
        HolidayBot.saveMessageId(chatId, response.message().messageId(), HolidayBot.botMessages);
    }











}
