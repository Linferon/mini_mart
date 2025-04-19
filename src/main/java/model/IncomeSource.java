package model;

public class IncomeSource {
    private Long id;
    private String name;

    public IncomeSource(Long id, String name) {
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
