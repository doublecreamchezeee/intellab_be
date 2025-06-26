package com.example.identityservice.repository;

import com.example.identityservice.model.composite.AchievementId;
import com.example.identityservice.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, AchievementId> {
    List<Achievement> findAllById_UserId(UUID userId);
}
