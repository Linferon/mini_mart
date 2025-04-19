package model.enums;

public enum ProductType {
    FOOD(1L, "Продукты питания"),
    BEVERAGES(2L, "Напитки"),
    HOUSEHOLD(3L, "Товары для дома"),
    PERSONAL_CARE(4L, "Личная гигиена"),
    ELECTRONICS(5L, "Электроника"),
    CLOTHING(6L, "Одежда"),
    STATIONERY(7L, "Канцтовары"),
    OTHER(8L, "Прочее");

    private final Long id;
    private final String name;

    ProductType(Long id, String name) {
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