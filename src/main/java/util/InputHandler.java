package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static util.ConsoleUtil.print;
import static util.ConsoleUtil.println;
import static util.LoggerUtil.error;

public class InputHandler {
    private static final Scanner scanner = new Scanner(System.in);

    private InputHandler() {
    }

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

    public static LocalDate getValidBudgetDate(){
        LocalDate budgetDate;
        while (true) {
            int year = getIntInput("Введите год (например, 2025): ");
            int month = getIntInput("Введите месяц (1-12): ");

            try {
                budgetDate = LocalDate.of(year, month, 1);
                LocalDate now = LocalDate.now().withDayOfMonth(1);

                if (budgetDate.isBefore(now)) {
                    error("Нельзя установить бюджет на прошедший месяц.");
                } else {
                    break;
                }
            } catch (Exception e) {
                error("Неверный ввод даты. Попробуйте снова.");
            }
        }
        return budgetDate;
    }

    public static int getValidIntInput(String s) {
        int quantity;
        do {
            quantity = getIntInput(s);
            if (quantity <= 0) {
                println("Вы не можете установить отрицательное количество!");
            }
        } while (quantity < 0);
        return quantity;

    }


    public static Long getUpdatedLongValue(String fieldName, Long currentValue) {
        long value = getLongInput("Введите новый " + fieldName + " (или 0, чтобы оставить текущий): ");
        return value == 0 ? currentValue : value;
    }

    public static Float getUpdatedFloatValue(String fieldName, Float currentValue) {
        Float value = getFloatInput("Введите новую " + fieldName + " (или 0, чтобы оставить текущую): ");
        return value.equals(0F) ? currentValue : value;
    }

    public static BigDecimal getUpdatedBigDecimalValue(String fieldName, BigDecimal currentValue) {
        BigDecimal value = getBigDecimalInput("Введите новую " + fieldName + " (или 0, чтобы оставить текущую): ");
        return value.compareTo(BigDecimal.ZERO) == 0 ? currentValue : value;
    }

    public static LocalDate getUpdatedDateValue(String fieldName, LocalDate currentValue) {
        String dateInput = getStringInput("Введите новую " + fieldName + " (ГГГГ-ММ-ДД или Enter для текущей): ");
        return dateInput.isEmpty() ? currentValue : LocalDate.parse(dateInput);
    }

    public static void inputIfPresent(String prompt, Consumer<String> setter) {
        String input = getStringInput(prompt + " (Enter для пропуска): ");
        if (!input.isEmpty()) setter.accept(input);
    }

    private static <T> T handleInput(String prompt, Supplier<T> inputSupplier) {
        while (true) {
            try {
                print(prompt);
                return inputSupplier.get();
            } catch (Exception e) {
                print("Неверный ввод, попробуйте еще раз!");
                scanner.nextLine();
            }
        }
    }

    public static void closeScanner() {
        scanner.close();
    }
}
