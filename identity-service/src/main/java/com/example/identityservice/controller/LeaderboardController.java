package com.example.identityservice.controller;

import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.dto.LeaderboardUpdateRequest;
import com.example.identityservice.dto.response.LeaderboardResponse;
import com.example.identityservice.service.LeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leaderboard")
@Tag(name = "Leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    @GetMapping
    @PublicEndpoint
    public ResponseEntity<Page<LeaderboardResponse>> getLeaderboard(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(leaderboardService.getLeaderboard(pageable, filter));
    }
    @PublicEndpoint
    @PostMapping("/update")
    public ResponseEntity<Void> updateLeaderboard(@RequestBody LeaderboardUpdateRequest request) {

        leaderboardService.updateLeaderboard(
                request.getUserId(),
                request.getType(),
                request.getAdditionalScore(),
                request.getProblemStat(),
                request.getCourseStat()
        );
        return ResponseEntity.ok().build();
    }
}
