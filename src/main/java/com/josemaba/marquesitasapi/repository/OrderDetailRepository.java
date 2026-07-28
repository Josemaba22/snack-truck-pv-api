package com.josemaba.marquesitasapi.repository;

import com.josemaba.marquesitasapi.entity.OrderDetail;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {
}
