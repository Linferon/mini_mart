package model.enums;

public enum RoleType {
    DIRECTOR(1L, "Директор"),
    ACCOUNTANT(2L, "Бухгалтер"),
    CASHIER(3L, "Кассир"),
    STOCK_KEEPER(4L, "Кладовщик");

    private final Long id;
    private final String name;

    RoleType(Long id, String name) {
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