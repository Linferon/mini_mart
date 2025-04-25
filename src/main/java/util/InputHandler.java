package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Supplier;

public class InputHandler {
    private static final Scanner scanner = new Scanner(System.in);

    private InputHandler() {}

    public static int getIntInput(String prompt) {
        return handleInput(prompt, () -> {
            int input = scanner.nextInt();
            scanner.nextLine();
            return input;
        });
    }

    public static long getLongInput(String prompt) {
        return handleInput(prompt, () -> {
            long input = scanner.nextLong();
            scanner.nextLine();
            return input;
        });
    }

    public static float getFloatInput(String prompt) {
        return handleInput(prompt, () -> {
            float input = scanner.nextFloat();
            scanner.nextLine();
            return input;
        });
    }

    public static String getStringInput(String prompt) {
        return handleInput(prompt, scanner::nextLine);
    }

    public static BigDecimal getBigDecimalInput(String prompt) {
        return handleInput(prompt, () -> {
            BigDecimal input = scanner.nextBigDecimal();
            scanner.nextLine();
            return input;
        });
    }

    public static LocalDate getDateInput(String prompt) {
        return handleInput(prompt, () -> {
            String dateString = scanner.nextLine();
            return dateString.isEmpty() ? LocalDate.now() : LocalDate.parse(dateString);
        });
    }

    private static <T> T handleInput(String prompt, Supplier<T> inputSupplier) {
        while (true) {
            try {
                ConsoleUtil.print(prompt);
                return inputSupplier.get();
            } catch (Exception e) {
                ConsoleUtil.print("Неверный ввод, попробуйте еще раз!");
                scanner.nextLine();
            }
        }
    }

    public static void closeScanner() {
        scanner.close();
    }
}
