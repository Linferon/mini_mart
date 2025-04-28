package model;

import util.TableFormatter;

public record Role(Long id, String name) implements FormattableEntity {

    private static final int ID_WIDTH = 5;
    private static final int NAME_WIDTH = 30;

    @Override
    public String toString() {
        return "Роль: id=" + id + ", название='" + name;
    }

    @Override
    public String getTableHeader() {
        return TableFormatter.formatCell("ID", ID_WIDTH) +
                TableFormatter.formatCell("Название", NAME_WIDTH);
    }

    @Override
    public String toTableRow() {
        return TableFormatter.formatCell(id, ID_WIDTH) +
                TableFormatter.formatCell(name, NAME_WIDTH);
    }

    @Override
    public String getTableDivider() {
        return TableFormatter.createDivider(ID_WIDTH, NAME_WIDTH);
    }
}
