package Main.validation;

import Main.entity.MessageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//класс для валидации данных, которые отправляет пользователь
public class MesFormat {

    public static List<MessageModel> parseMessages(String input) {
        List<MessageModel> messages = new ArrayList<>();
        String[] entries = input.split("\\n");

        // Регулярное выражение для проверки правильности ввода
        String regex = "^(.+)\\s+(https?://\\S+|\\S+\\.\\S+)$";
        Pattern pattern = Pattern.compile(regex);

        for (String entry : entries) {
            Matcher matcher = pattern.matcher(entry.trim());
            if (matcher.matches()) {
                String name = matcher.group(1);
                String link = matcher.group(2);
                messages.add(new MessageModel(name, link));
            } else {
                System.out.println("Неправильный формат: " + entry);
            }
        }

        return messages;
    }
}
