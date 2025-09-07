package com.example.server.controller;

import com.example.server.modal.Product;
import com.example.server.repository.AuthRepository;
import com.example.server.repository.ProductRepository;
import com.example.server.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final AuthRepository authRepository;

    public ProductController(ProductRepository productRepository, CloudinaryService cloudinaryService, AuthRepository authRepository) {
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
        this.authRepository = authRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("availabilityStatus") String availabilityStatus,
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("📦 Получены данные:");
            System.out.println("name = " + name);
            System.out.println("description = " + description);
            System.out.println("price = " + price);
            System.out.println("category = " + category);
            System.out.println("availabilityStatus = " + availabilityStatus);
            System.out.println("file = " + (file != null ? file.getOriginalFilename() : "null"));

            String imageUrl = cloudinaryService.uploadFile(file);

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            product.setAvailabilityStatus(availabilityStatus);
            product.setImageUrl(imageUrl);

            Product savedProduct = productRepository.save(product);

            return ResponseEntity.ok(savedProduct);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при добавлении продукта: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("availabilityStatus") String availabilityStatus,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Продукт не найден"));

            existingProduct.setName(name);
            existingProduct.setDescription(description);
            existingProduct.setPrice(price);
            existingProduct.setCategory(category);
            existingProduct.setAvailabilityStatus(availabilityStatus);

            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(file);
                existingProduct.setImageUrl(imageUrl);
            }

            Product updatedProduct = productRepository.save(existingProduct);

            return ResponseEntity.ok(updatedProduct);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обновлении продукта: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Продукт не найден");
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Продукт удалён");
    }

    @PostMapping("/pay")
    public ResponseEntity<String> processPayment() {
        System.out.println("💳 Платеж успешно обработан: ");
        return ResponseEntity.ok("Платеж успешно обработан");
    }
}
