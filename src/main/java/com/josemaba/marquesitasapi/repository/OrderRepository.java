package com.josemaba.marquesitasapi.repository;

import com.josemaba.marquesitasapi.entity.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByClientOrderId(UUID clientOrderId);
}
