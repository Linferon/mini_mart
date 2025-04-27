package controller;

import util.ConsoleUtil;
import util.DateTimeUtils;
import exception.handler.ExceptionHandler;
import util.InputHandler;
import util.MenuBuilder;

import java.time.LocalDate;

public abstract class BaseController {

    public abstract void showMenu();

    protected void showDateRangeMenu(DateRangeAction action) {
        ExceptionHandler.execute(() -> {
            LocalDate[] dateRange = DateTimeUtils.getDateRange();
            action.execute(dateRange[0], dateRange[1]);
        }, "Ошибка при работе с диапазоном дат");
    }

    @FunctionalInterface
    protected interface DateRangeAction {
        void execute(LocalDate startDate, LocalDate endDate);
    }

    protected void showConfirmationMenu(String message, Runnable action) {
        ConsoleUtil.println(message);
        ConsoleUtil.println("1. Да");
        ConsoleUtil.println("2. Нет");

        int choice = InputHandler.getIntInput("Выберите действие: ");

        if (choice == 1) {
            action.run();
        }
    }

    protected MenuBuilder createMenu(String title) {
        return new MenuBuilder(title);
    }

    protected void showSuccess(String message) {
        ConsoleUtil.println("\n✓ " + message);
    }

    protected void showError(String message) {
        ConsoleUtil.println("\n✗ " + message);
    }
}