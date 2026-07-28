package com.josemaba.marquesitasapi.repository;

import com.josemaba.marquesitasapi.entity.ProductAddon;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAddonRepository extends JpaRepository<ProductAddon, UUID> {
}
