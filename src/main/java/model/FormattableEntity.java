package model;

public interface FormattableEntity {
    String getTableHeader();

    String toTableRow();

    String getTableDivider();
}