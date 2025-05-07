package com.example.identityservice.dto.response.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChartResponse {
  private String type;
  private List<ChartDataPoint> data;
}