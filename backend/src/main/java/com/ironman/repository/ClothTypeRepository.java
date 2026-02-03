package com.ironman.repository;

import com.ironman.model.ClothType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothTypeRepository extends JpaRepository<ClothType, Long> {

    List<ClothType> findByIsActiveTrue();

    List<ClothType> findByCategoryAndIsActiveTrue(String category);
}