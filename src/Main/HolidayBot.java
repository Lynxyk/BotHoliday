package Main;

import Main.logging.Log;
import Main.validation.MesFormat;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import db.entity.Gift;
import db.Postgre;
import Main.entity.MessageModel;
import Main.entity.UserState;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HolidayBot {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final Map<Long, UserState> userStates = new HashMap<>(); // Хранение состояния для каждого пользователя

    private static final Map<Long, List<Integer>> userMessages = new HashMap<>(); // Хранение ID сообщений для каждого пользователя
    protected static final Map<Long, List<Integer>> botMessages = new HashMap<>(); // Хранение ID сообщений для каждого пользователя
    protected static final Map<Long, List<Integer>> podarok = new HashMap<>(); // Хранение ID сообщений для каждого пользователя
    private static final Map<Long, Map<Integer, List<Gift>>> userMessagesGift = new HashMap<>();

    static TextPages textPages = new TextPages();

    static Postgre postgre;

    {
        try {
            postgre = new Postgre("postgres", "12345", "localhost");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws SQLException {
        // Инициализация бота с токеном
        TelegramBot bot = new TelegramBot("");
        Log log = new Log();
        Postgre postgre = new Postgre("postgres", "12345", "localhost");

        // Установка слушателя обновлений
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    String messageText = update.message().text();
                    long chatId = update.message().chat().id();
                    int messageId = update.message().messageId();
                    String userName = update.message().chat().username();
                    String logMessage = String.format("[%s] %s: %s", dtf.format(LocalDateTime.now()), userName, messageText);

                    log.logChat(logMessage);

                    // Сохранение ID сообщения пользователя
                    saveMessageId(chatId, messageId, userMessages);


                    // Проверка состояния пользователя + его ответа
                    if (userStates.containsKey(chatId) && userStates.get(chatId).state().equals("waiting_for_message")) {
                        try {
                            if (userStates.get(chatId).method().equals("editProfile") && isValidDate(messageText)) {
                                if (postgre.availabilityUser("@" + userName)) {
                                    postgre.updateUserBirth(messageText, "@" + userName);
                                } else {
                                    postgre.addUser("@" + userName, messageText);
                                }
                                // Пользователь отправил сообщение после нажатия кнопки
                                handleUserMessage(userName, messageText);
                                textPages.finishPage(chatId, bot, "Готово");
                            } else if (userStates.get(chatId).method().equals("addGiftProfile") && postgre.availabilityUser("@" + userName)) {
                                List<MessageModel> list = MesFormat.parseMessages(messageText);
                                for (int i = 0; i < list.size(); i++) {
                                    postgre.addGifts("@" + userName, list.get(i).getName(), list.get(i).getLink());
                                }
                                textPages.finishPage(chatId, bot, "Готово");
                            } else if (userStates.get(chatId).method().equals("addFriends") && isValidTelegramTag(messageText) && postgre.availabilityUser("@" + userName) && postgre.availabilityUser(messageText)) {
                                postgre.addFriends("@" + userName, messageText);
                                textPages.finishPage(chatId, bot, "Готово");
                            } else if (userStates.get(chatId).method().equals("deleteFriends") && isValidTelegramTag(messageText) && postgre.availabilityUser("@" + userName) && postgre.availabilityUser(messageText)) {
                                postgre.deleteFriends("@" + userName, messageText);
                                textPages.finishPage(chatId, bot, "Готово");
                            } else if (userStates.get(chatId).method().equals("showList") && isValidTelegramTag(messageText) && postgre.availabilityUser(messageText)) {
                                textPages.showListFriendsPage(chatId, bot, messageId, messageText, postgre.showGifts(messageText));
                            } else if (!postgre.availabilityUser("@" + userName)) {
                                textPages.finishPage(chatId, bot, "Зарегистрируйтесь. Для этого зайдите в раздел -Мой профиль- и нажмите -Изменить дату рождения-" + "\n" +
                                        "После данных действий вы сможете пользоваться этим сервисом");
                            } else {
                                textPages.finishPage(chatId, bot, "Данные введены неверно или пользователя не существует");
                            }
                            deleteAllMessages(bot, chatId, userMessages);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        //userStates.remove(chatId); // отключение состояния ожидания сообщения
                        continue;
                    }

                    if (messageText.equals("/start")) {
                        textPages.start(chatId, bot);
                    } else {
                        textPages.unknown(chatId, bot, messageId);
                        // Удаляем все предыдущие сообщения
                        deleteAllMessages(bot, chatId, userMessages);
                    }
                } else if (update.callbackQuery() != null) {
                    String callbackData = update.callbackQuery().data();
                    long chatId = update.callbackQuery().message().chat().id();
                    int messageId = update.callbackQuery().message().messageId();
                    String userName = update.callbackQuery().from().username();
                    String logMessage = String.format("[%s] %s clicked: %s", dtf.format(LocalDateTime.now()), userName, callbackData);

                    log.logChat(logMessage);

                    switch (callbackData) {
                        case "profile":
                            textPages.profilePage(chatId, bot, messageId, userName);
                            break;
                        case "friends":
                            textPages.friendsPage(chatId, bot, messageId, userName);
                            break;
                        case "addFriends":
                            textPages.addFriendsPage(chatId, bot, messageId, userName);
                            userStates.put(chatId, new UserState("waiting_for_message", callbackData));
                            break;
                        case "deleteFriends":
                            textPages.deleteFriendsPage(chatId, bot, messageId, userName);
                            userStates.put(chatId, new UserState("waiting_for_message", callbackData));
                            break;
                        case "editProfile":
                            textPages.editDateProfilePage(chatId, bot, messageId, userName);
                            userStates.put(chatId, new UserState("waiting_for_message", callbackData));
                            break;
                        case "ListGiftProfile":
                            try {
                                sendGiftMessages(chatId, bot, "@" + userName);
                                textPages.backForAllGiftPage(chatId, bot);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "addGiftProfile":
                            textPages.addListProfilePage(chatId, bot, messageId, userName);
                            userStates.put(chatId, new UserState("waiting_for_message", callbackData));
                            break;
                        case "showList":
                            textPages.choiceListFriendsPage(chatId, bot, messageId, userName);
                            userStates.put(chatId, new UserState("waiting_for_message", callbackData));
                            break;
                        case "backProfile":
                            textPages.profilePage(chatId, bot, messageId, userName);
                            userStates.remove(chatId);
                            break;
                        case "backFriends":
                            textPages.friendsPage(chatId, bot, messageId, userName);
                            userStates.remove(chatId);
                            break;
                        case "сonfirmation":
                            deleteMessages(bot, chatId, userMessagesGift);
                            deleteAllMessages(bot, chatId, botMessages);
                            break;
                        case "backGifts":
                            deleteMessages(bot, chatId, userMessagesGift);
                            deleteAllMessages(bot, chatId, botMessages);
                            break;
                        default:
                            if (callbackData.startsWith("delete_")) {
                                String messageToDelete = callbackData.split("_")[1];
                                bot.execute(new DeleteMessage(chatId, messageId));
                                userMessagesGift.get(chatId).remove(messageId);
                                try {
                                    postgre.deleteGifts("@" + userName, Integer.parseInt(messageToDelete));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("Message deleted: " + messageToDelete + " by user @" + update.callbackQuery().from().username());
                            } else {
                                textPages.backStart(chatId, bot, messageId);
                            }
                            userStates.remove(chatId);
                            break;
                    }

                    // Ответ на callback query
                    bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static void handleUserMessage(String userName, String messageText) {
        // вывод тега пользователя и его сообщения в консоль
        System.out.println("@" + userName + ": " + messageText);
    }

    public static boolean isValidDate(String dateStr) {
        // установка формата даты
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        sdf.setLenient(false); // Устанавливаем строгую проверку

        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    protected static void saveMessageId(long chatId, int messageId, Map<Long, List<Integer>> messages) {
        messages.computeIfAbsent(chatId, k -> new ArrayList<>()).add(messageId);
    }

    private static void deleteAllMessages(TelegramBot bot, long chatId, Map<Long, List<Integer>> messages) {
        if (messages.containsKey(chatId)) {
            for (int messageId : messages.get(chatId)) {
                bot.execute(new DeleteMessage(chatId, messageId));
            }
            messages.get(chatId).clear();
        }
    }

    private static void sendGiftMessages(long chatId, TelegramBot bot, String username) throws SQLException {
        postgre = new Postgre("postgres", "12345", "localhost");
        List<Gift> gift = postgre.showGifts(username);
        Map<Integer, List<Gift>> messages = new HashMap<>();

        for (int i = 0; i < gift.size(); i++) {
            SendMessage message = new SendMessage(chatId, gift.get(i).getName() + " " + gift.get(i).getLink());
            message.replyMarkup(new InlineKeyboardMarkup(
                    new InlineKeyboardButton("Удалить").callbackData("delete_" + gift.get(i).getGift_id())
            ));
            SendResponse response = bot.execute(message);
            if (response.isOk()) {
                messages.put(response.message().messageId(), gift);
            }
        }

        userMessagesGift.put(chatId, messages);
    }

    //проверка
    private static void deleteMessages(TelegramBot bot, long chatId, Map<Long, Map<Integer, List<Gift>>> messages) {
        if (messages.containsKey(chatId)) {
            Map<Integer, List<Gift>> chatMessages = messages.get(chatId);
            for (Integer messageId : chatMessages.keySet()) {
                DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);

                BaseResponse response = bot.execute(deleteMessage);
                if (!response.isOk()) {
                    System.err.println("Failed to delete message: " + response.description());
                }
            }
            chatMessages.clear();
        }
    }

    public static boolean isValidTelegramTag(String tag) {
        if (tag == null) {
            return false;
        }
        return tag.matches("^@[A-Za-z][A-Za-z0-9_]{4,31}$") && !tag.contains("__");
    }

}
