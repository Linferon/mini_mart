package exception.handler;

import util.ConsoleUtil;

public class ExceptionHandler {
    private ExceptionHandler() {}

    public static void execute(Runnable action, String errorMessage) {
        try {
            action.run();
        } catch (Exception e) {
            ConsoleUtil.println(errorMessage + ": " + e.getMessage());
        }
    }
}