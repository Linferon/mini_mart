package util;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {
    private LoggerUtil() {}
    private static final Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    private static FileHandler fileHandler;

    static {
        try {
            fileHandler = new FileHandler("logs/mini_mart.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);

            LOGGER.setLevel(Level.ALL);

            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            LOGGER.severe("Не удалось настроить файловый лог: " + e.getMessage());
        }
    }

    public static void setLogLevel(Level level) {
        LOGGER.setLevel(level);
    }

    public static void debug(String message) {
        LOGGER.fine(message);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message) {
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