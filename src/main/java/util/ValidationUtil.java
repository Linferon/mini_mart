package util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import static java.math.BigDecimal.ZERO;

public class ValidationUtil {
    private ValidationUtil() {
    }

    public static void validateQuantity(Integer quantity, String message) {
        if (quantity == null) {
            throw new IllegalArgumentException("Количество должно быть указано");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
    public static void validateQuantity(Integer quantity) {
        validateQuantity(quantity, "Количество товара не может быть отрицательным");
    }

    public static void validatePositiveFloat(Float value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validatePositiveFloat(Float value) {
        validatePositiveFloat(value, "Значение должно быть положительным числом");
    }

    public static void validatePositiveAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validatePositiveAmount(BigDecimal amount) {
        validatePositiveAmount(amount, "Значение должно быть положительным числом") ;
    }

    public static void validateId(Long id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateId(Long id) {
        validateId(id, "ID не может быть null");
    }

    public static void validateDate(LocalDate date, String message) {
        if (date == null) {
            throw new IllegalArgumentException(message);
        }
    }
    public static void validateDateRange(Timestamp startDate, Timestamp endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания периода должны быть указаны");
        }

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
    }
    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания периода должны быть указаны");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
    }
    public static void validateString(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

}
