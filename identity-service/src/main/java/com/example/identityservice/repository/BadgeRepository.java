package com.example.identityservice.repository;

import com.example.identityservice.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Integer> {
    // Additional query methods can be defined here if needed
}
