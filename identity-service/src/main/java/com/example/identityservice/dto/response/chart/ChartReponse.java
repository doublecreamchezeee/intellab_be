package com.example.identityservice.dto.response.chart;

import com.example.identityservice.dto.response.chart.ChartDataPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChartResponse {
  private String type;
  private List<ChartDataPoint> data;
}