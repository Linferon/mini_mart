package model;

public class ProductCategory {
    private Long id;
    private String name;

    public ProductCategory(Long id ,String name) {
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
