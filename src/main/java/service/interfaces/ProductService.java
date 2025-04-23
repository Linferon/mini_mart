package service.interfaces;

import model.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(Long id);

    List<Product> getProductsByCategory(Long categoryId);

    List<Product> getProductsByName(String name);

    Long addProduct(Product product);

    boolean updateProduct(Product product);

    boolean deleteProduct(Long id);
}
