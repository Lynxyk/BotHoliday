package Main.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
    private final String LOG_FILE_PATH = "chat_log.txt";

    public void logChat(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(message + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
