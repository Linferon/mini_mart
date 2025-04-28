package controller;

import exception.handler.ExceptionHandler;
import util.MenuBuilder;

import java.time.LocalDate;

import static util.ConsoleUtil.println;
import static util.DateTimeUtils.getDateRange;
import static util.InputHandler.getIntInput;

public abstract class BaseController {

    public abstract void showMenu();

    protected void showDateRangeMenu(DateRangeAction action) {
        ExceptionHandler.execute(() -> {
            LocalDate[] dateRange = getDateRange();
            action.execute(dateRange[0], dateRange[1]);
        });
    }

    @FunctionalInterface
    protected interface DateRangeAction {
        void execute(LocalDate startDate, LocalDate endDate);
    }

    protected void showConfirmationMenu(String message, Runnable action) {
        println(message);
        println("1. Да");
        println("2. Нет");

        int choice = getIntInput("Выберите действие: ");

        if (choice == 1) {
            action.run();
        }
    }

    protected MenuBuilder createMenu(String title) {
        return new MenuBuilder(title);
    }

    protected void showSuccess(String message) {
        println("\n✓ " + message);
    }

    protected void showError(String message) {
        println("\n✗ " + message);
    }
}