package com.josemaba.marquesitasapi.repository;

import com.josemaba.marquesitasapi.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByCategoryId(UUID categoryId);
}
