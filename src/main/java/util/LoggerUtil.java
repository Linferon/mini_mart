package util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {
    private LoggerUtil() {}
    private static final Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    private static FileHandler fileHandler;
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/mini_mart.log";

    static {
        try {
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                boolean created = logDir.mkdir();
                if (!created) {
                   LOGGER.severe("Не удалось создать директорию для логов: " + LOG_DIR);
                }
            }

            fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);

            LOGGER.setLevel(Level.INFO);

            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            LOGGER.severe("Не удалось настроить файловый лог: " + e.getMessage());
        }
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message) {
        ConsoleUtil.printError(message);
        LOGGER.severe(message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}