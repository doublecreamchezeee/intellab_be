package com.example.identityservice.repository;

import com.example.identityservice.model.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreakRepository extends JpaRepository<Streak, String> {
}
