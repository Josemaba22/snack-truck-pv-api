package com.josemaba.marquesitasapi.repository;

import com.josemaba.marquesitasapi.entity.ProductDetail;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, UUID> {

    boolean existsByProductIdAndAddonId(UUID productId, UUID addonId);

    Optional<ProductDetail> findByProductIdAndAddonId(UUID productId, UUID addonId);

    List<ProductDetail> findByProductId(UUID productId);
}
