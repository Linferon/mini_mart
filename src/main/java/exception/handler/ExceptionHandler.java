package exception.handler;

import util.ConsoleUtil;

public class ExceptionHandler {
    private ExceptionHandler() {}

    public static void execute(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            ConsoleUtil.println(e.getMessage());
        }
    }
}