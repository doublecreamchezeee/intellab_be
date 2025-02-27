package com.example.identityservice.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressLanguageResponse {
    private ProgressLanguageResponse.LanguageStatistics top1;
    private ProgressLanguageResponse.LanguageStatistics top2;
    private ProgressLanguageResponse.LanguageStatistics top3;
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LanguageStatistics {
        private int solved;
        private String name;
    }
}
