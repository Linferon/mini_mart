package model.enums;

public enum IncomeType {
    SALES(1L, "Продажи"),
    INVESTMENTS(2L, "Инвестиции"),
    RENT(3L, "Аренда"),
    INTEREST(4L, "Проценты"),
    GRANTS(5L, "Гранты"),
    OTHER(6L, "Прочее");

    private final Long id;
    private final String name;

    IncomeType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}