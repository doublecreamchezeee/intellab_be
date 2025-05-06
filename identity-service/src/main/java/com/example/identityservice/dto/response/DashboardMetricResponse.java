package com.example.identityservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardMetricResponse {
  String title;
  Number value;
  String change;
  String changeNote;
  String changeType; // "increase" | "decrease" | "none"
}