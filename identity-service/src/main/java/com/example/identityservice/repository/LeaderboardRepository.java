package com.example.identityservice.repository;

import com.example.identityservice.model.Leaderboard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, UUID> {
    Optional<Leaderboard> findByUserIdAndType(UUID userId, String type);

    List<Leaderboard> findByTypeOrderByScoreDesc(String type, Pageable pageable);

    Optional<Leaderboard> findByUserId(UUID userUUID);
}
