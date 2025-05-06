package com.example.identityservice.service;

import com.example.identityservice.dto.response.chart.ChartResponse;
import com.example.identityservice.payload.response.DashboardMetricResponse;
import com.example.identityservice.repository.VNPayPaymentCoursesRepository;
import com.example.identityservice.repository.VNPayPaymentPremiumPackageRepository;
import com.example.identityservice.repository.VNPayPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final VNPayPaymentRepository vnpayPaymentRepository;
    private final VNPayPaymentPremiumPackageRepository premiumPackageRepository;
    private final VNPayPaymentCoursesRepository coursesRepository;

    public List<DashboardMetricResponse> getSystemOverview() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        int lastMonth = (currentMonth == 1) ? 12 : currentMonth - 1;
        int lastMonthYear = (currentMonth == 1) ? currentYear - 1 : currentYear;

        // --- Total Revenue ---
        Float totalRevenue = vnpayPaymentRepository.sumSuccessfulPayments();
        Float lastMonthRevenue = vnpayPaymentRepository.sumSuccessfulPaymentsByMonth(lastMonth, lastMonthYear);
        String revenueChange = calculateChange(totalRevenue, lastMonthRevenue);

        // --- Subscriptions ---
        int currentSubscriptions = premiumPackageRepository.countSuccessfulSubscriptions();
        int newThisMonth = premiumPackageRepository.countNewSubscriptionsByMonth(currentMonth, currentYear);

        // --- Courses Purchased ---
        int totalCoursesPurchased = coursesRepository.countSuccessfulCoursePurchases();
        int lastMonthCourses = coursesRepository.countSuccessfulCoursePurchasesByMonth(lastMonth, lastMonthYear);
        String courseChange = formatChange(totalCoursesPurchased, lastMonthCourses);

        return List.of(
                DashboardMetricResponse.builder()
                        .title("Total Revenue")
                        .value(totalRevenue)
                        .change(revenueChange)
                        .changeNote("from last month")
                        .changeType(getChangeType(revenueChange))
                        .build(),

                DashboardMetricResponse.builder()
                        .title("Subscriptions")
                        .value(currentSubscriptions)
                        .change("+" + newThisMonth)
                        .changeNote("new users this month")
                        .changeType("increase")
                        .build(),

                DashboardMetricResponse.builder()
                        .title("Courses Purchased")
                        .value(totalCoursesPurchased)
                        .change(courseChange)
                        .changeNote("since last month")
                        .changeType(getChangeType(totalCoursesPurchased, lastMonthCourses))
                        .build());
    }

    private String calculateChange(Float current, Float previous) {
        if (previous == null || previous == 0)
            return "+∞";
        float percent = ((current - previous) / previous) * 100;
        return String.format("%.2f%%", percent);
    }

    private String formatChange(int current, int previous) {
        int diff = current - previous;
        String sign = diff >= 0 ? "+" : "-";
        return sign + Math.abs(diff);
    }

    private String getChangeType(String changeStr) {
        return changeStr.startsWith("-") ? "decrease" : "increase";
    }

    private String getChangeType(int current, int previous) {
        return current < previous ? "decrease" : "increase";
    }

    public ChartResponse getSubscriptionGrowth(String type, LocalDate startDate, LocalDate endDate) {
        String unit = switch (type) {
            case "hourly" -> "hour";
            case "daily" -> "day";
            case "weekly" -> "week";
            case "monthly" -> "month";
            case "custom" -> "day"; // use day granularity for custom
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        // if custom, use provided dates; else auto range
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now().plusDays(1);

        List<Object[]> raw = premiumPackageRepository.countSubscriptionsByRange(
                unit,
                start.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<ChartDataPoint> data = raw.stream()
                .map(row -> {
                    Instant instant = (Instant) row[0];
                    Integer count = ((Number) row[1]).intValue();
                    String label = formatLabel(instant, unit);
                    return new ChartDataPoint(label, count);
                })
                .toList();

        return ChartResponse.builder()
                .type(type)
                .data(data)
                .build();
    }

    public ChartResponse getRevenue(String type, LocalDate startDate, LocalDate endDate) {
        String unit = switch (type) {
            case "hourly" -> "hour";
            case "daily" -> "day";
            case "weekly" -> "week";
            case "monthly" -> "month";
            case "custom" -> "day";
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now().plusDays(1);

        List<Object[]> raw = paymentRepo.sumRevenueByRange(
                unit,
                start.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<ChartDataPoint> data = raw.stream()
                .map(row -> {
                    Instant instant = (Instant) row[0];
                    Long amount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    String label = formatLabel(instant, unit);
                    return new ChartDataPoint(label, amount);
                })
                .toList();

        return ChartResponse.builder()
                .type(type)
                .data(data)
                .build();
    }

    private String formatLabel(Instant timestamp, String unit) {
        ZonedDateTime zdt = timestamp.atZone(ZoneId.systemDefault());
        return switch (unit) {
            case "hour" -> zdt.format(DateTimeFormatter.ofPattern("HH:mm dd/MM"));
            case "day" -> zdt.format(DateTimeFormatter.ofPattern("dd MMM"));
            case "week" -> "W" + zdt.get(WeekFields.ISO.weekOfWeekBasedYear()) + " " + zdt.getYear();
            case "month" -> zdt.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            default -> zdt.toString();
        };
    }
}