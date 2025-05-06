package com.example.identityservice.service;

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
                .build()
        );
    }

    private String calculateChange(Float current, Float previous) {
        if (previous == null || previous == 0) return "+∞";
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
}