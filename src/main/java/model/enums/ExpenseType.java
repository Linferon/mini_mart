package model.enums;

public enum ExpenseType {
    RENT(1L, "Аренда"),
    UTILITIES(2L, "Коммунальные услуги"),
    SALARIES(3L, "Заработная плата"),
    SUPPLIES(4L, "Расходные материалы"),
    MARKETING(5L, "Маркетинг"),
    TAXES(6L, "Налоги"),
    OTHER(7L, "Прочее");

    private final Long id;
    private final String name;

    ExpenseType(Long id, String name) {
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