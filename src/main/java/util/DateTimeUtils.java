package util;

import model.Expense;
import model.TimestampedEntity;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static util.InputHandler.getDateInput;

public class DateTimeUtils {
    private DateTimeUtils() {
    }

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
        LocalDate startDate = getDateInput("Введите начальную дату (ГГГГ-ММ-ДД): ");
        LocalDate endDate = getDateInput("Введите конечную дату (ГГГГ-ММ-ДД): ");
        return new LocalDate[]{startDate, endDate};
    }

    public static LocalDate extractLocalDate(Expense expense) {
        return expense.getExpenseDate().toLocalDateTime().toLocalDate();
    }

    public static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }

    public static LocalDate getLocalDateFromTimestamp(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalDate();
    }

    public static Timestamp convertToTimestamp(LocalDate date) {
        return Timestamp.valueOf(date.atStartOfDay());
    }


    public static void setupTimestamps(TimestampedEntity stock) {
        Timestamp now = Timestamp.from(Instant.now());

        if (stock.getCreatedAt() == null) {
            stock.setCreatedAt(now);
        }

        if (stock.getUpdatedAt() == null) {
            stock.setUpdatedAt(now);
        }
    }
}
