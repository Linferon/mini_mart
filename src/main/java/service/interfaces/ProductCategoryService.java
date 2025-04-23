package service.interfaces;

import model.ProductCategory;

import java.util.List;

public interface ProductCategoryService {
    List<ProductCategory> getAllCategories();

    ProductCategory getCategoryById(Long id);

    List<ProductCategory> getCategoryByName(String name);
}
