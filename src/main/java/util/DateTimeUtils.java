package util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeUtils {
    private DateTimeUtils() {}

    public static Timestamp startOfDay(LocalDate date) {
        return Timestamp.valueOf(date.atStartOfDay());
    }

    public static Timestamp endOfDay(LocalDate date) {
        return Timestamp.valueOf(date.atTime(23, 59, 59));
    }

    public static Timestamp fromLocalDate(LocalDate date) {
        return new Timestamp(date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    public static LocalDate[] getDateRange() {
        LocalDate startDate = InputHandler.getDateInput("Введите начальную дату (ГГГГ-ММ-ДД): ");
        LocalDate endDate = InputHandler.getDateInput("Введите конечную дату (ГГГГ-ММ-ДД): ");
        return new LocalDate[]{startDate, endDate};
    }

    public static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }
}