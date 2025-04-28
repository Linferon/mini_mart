package exception.handler;

import static util.ConsoleUtil.println;

public class ExceptionHandler {
    private ExceptionHandler() {}

    public static void execute(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            println(e.getMessage());
        }
    }
}