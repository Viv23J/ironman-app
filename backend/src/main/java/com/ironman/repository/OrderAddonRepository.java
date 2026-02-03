package com.ironman.repository;

import com.ironman.model.OrderAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAddonRepository extends JpaRepository<OrderAddon, Long> {

    List<OrderAddon> findByOrderId(Long orderId);
}