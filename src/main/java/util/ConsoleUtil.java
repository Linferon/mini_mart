package util;

import model.FormattableEntity;

import java.time.LocalDate;
import java.util.List;

import static util.InputHandler.getDateInput;

public class ConsoleUtil {
    private ConsoleUtil() {
    }

    public static void println(Object obj) {
        System.out.println(obj);
    }

    public static void print(Object obj) {
        System.out.print(obj);
    }

    public static void printDivider() {
        System.out.println("--------------------------------");
    }

    public static void printHeader(String title) {
        printDivider();
        println(title);
        printDivider();
    }

    public static void printError(String s) {
        System.out.println(s);
    }

    public static void showEntityDetails(Object entity, String title) {
        printHeader(title);
        println(entity.toString());
    }

    @FunctionalInterface
    public interface DateRangeSupplier<T> {
        List<T> getForDateRange(LocalDate startDate, LocalDate endDate);
    }

    public static <T extends FormattableEntity> void selectEntitiesForDateRange(DateRangeSupplier<T> supplier, String headerMessage) {
        LocalDate startDate = getDateInput("Введите период для поиска" + " (начальная дата, ГГГГ-ММ-ДД): ");
        LocalDate endDate = getDateInput("Введите период для поиска" + " (конечная дата, ГГГГ-ММ-ДД): ");

        List<T> entities = supplier.getForDateRange(startDate, endDate);
        showEntitiesTable(entities, headerMessage);
    }

    public static <T extends FormattableEntity> void showEntitiesTable(List<T> entities, String title) {
        printHeader(title);
        println(TableFormatter.formatTable(entities));
    }
}