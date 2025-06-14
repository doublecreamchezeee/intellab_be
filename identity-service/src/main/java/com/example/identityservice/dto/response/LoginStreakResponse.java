package com.example.identityservice.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginStreakResponse {
    Integer streakLogin;
    Instant lastLogin;
    String userUid;
    Boolean isLostStreak = false;
    Boolean isUpStreak = false;
}
