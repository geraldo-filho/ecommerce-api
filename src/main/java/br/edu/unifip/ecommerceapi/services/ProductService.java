package br.edu.unifip.ecommerceapi.services;

import br.edu.unifip.ecommerceapi.models.Category;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
@Service
public class ProductService {

    final ProductRepository productRepository;
    final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService, CategoryService categoryService1){
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Transactional

    public Product save(Product product, UUID categoryId){
        if (categoryId != null){
            Category category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found."));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Product product) {
        productRepository.delete(product);
    }

    public Product partialUpdate(Product product, Map<Object, Object> objectMap) {
        if (objectMap.containsKey("category")){
            UUID categoryId = (UUID) objectMap.get("category");
            Category category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
            objectMap.remove("category");
        }
        objectMap.forEach((key, valeu) -> {
            Field field = ReflectionUtils.findField(Product.class, (String) key);
            field.setAccessible(true);

            try {
                valeu = BigDecimal.valueOf((double) valeu);
            } catch (ClassCastException ignored) {
            }
            ReflectionUtils.setField(field, product, valeu);
        });
        return productRepository.save(product);
    }

    public List<Product> findByActiveTrue(){
        return productRepository.findByActiveTrue();
    }

    public List<Product> findByName(String name){
        return productRepository.findByName();
    }

    public List<Product> findByCategoryName(String name){
        return productRepository.findByCategoryName(name);
    }

}
