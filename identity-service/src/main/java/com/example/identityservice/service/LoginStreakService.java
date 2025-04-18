package com.example.identityservice.service;


import com.example.identityservice.dto.response.LoginStreakResponse;
import com.example.identityservice.model.Streak;
import com.example.identityservice.repository.StreakRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LoginStreakService {
    private final StreakRepository streakRepository;

    public LoginStreakResponse loginStreak(String userUid) {
        System.out.println(userUid);
        Streak streak = streakRepository.findById(userUid).orElse(null);
        if (streak == null) {
            streak = new Streak();
            streak.setUserUid(userUid);
            streak.setStreakScore(1);
            streak.setLastAccess(Instant.now());
            streak = streakRepository.save(streak);
            return LoginStreakResponse.builder()
                    .streakLogin(streak.getStreakScore())
                    .lastLogin(streak.getLastAccess())
                    .userUid(userUid)
                    .isLostStreak(false)
                    .build();
        }

        LocalDate lastLoginDate = streak.getLastAccess().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate now = LocalDate.now();

        if (lastLoginDate.isEqual(now))
        {
            // same day ->
            return null;
        }
        else if (now.isEqual(lastLoginDate.plusDays(1)))
        {
            streak.setStreakScore(streak.getStreakScore() + 1);
            streak.setLastAccess(Instant.now());

            streak = streakRepository.save(streak);

        }
        else {
            streak.setStreakScore(1);
            streak.setLastAccess(Instant.now());
            streak = streakRepository.save(streak);
        }
        return LoginStreakResponse.builder()
                .streakLogin(streak.getStreakScore())
                .lastLogin(streak.getLastAccess())
                .userUid(userUid)
                .isLostStreak(streak.getStreakScore() == 1)
                .build();
    }
}
