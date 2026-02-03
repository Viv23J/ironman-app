package com.ironman.repository;

import com.ironman.model.LaundryService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaundryServiceRepository extends JpaRepository<LaundryService, Long> {

    List<LaundryService> findByIsActiveTrue();

    List<LaundryService> findByCategoryAndIsActiveTrue(String category);
}