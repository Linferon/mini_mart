package util;

import model.FormattableEntity;

import java.util.List;

public class TableFormatter {
    private TableFormatter() {}

    public static <T extends FormattableEntity> String formatTable(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return "Нет данных для отображения";
        }
        
        StringBuilder table = new StringBuilder();
        T first = entities.getFirst();
        
        table.append(first.getTableHeader()).append("\n");
        table.append(first.getTableDivider()).append("\n");
        
        for (T entity : entities) {
            table.append(entity.toTableRow()).append("\n");
        }
        
        return table.toString();
    }

    public static String formatCell(Object value, int width) {
        return String.format("%-" + width + "s", value == null ? "-" : value.toString());
    }

    public static String createDivider(int... widths) {
        StringBuilder divider = new StringBuilder();
        for (int width : widths) {
            divider.append("+").append("-".repeat(width));
        }
        divider.append("+");
        return divider.toString();
    }
}