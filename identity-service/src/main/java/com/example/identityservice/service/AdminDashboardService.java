package com.example.identityservice.service;

import com.example.identityservice.client.CourseClient;
import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.dto.response.DashboardTableResponse;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import com.example.identityservice.dto.response.chart.ChartDataPoint;
import com.example.identityservice.dto.response.chart.ChartResponse;
import com.example.identityservice.dto.response.DashboardMetricResponse;
import com.example.identityservice.model.VNPayPayment;
import com.example.identityservice.repository.VNPayPaymentCoursesRepository;
import com.example.identityservice.repository.VNPayPaymentPremiumPackageRepository;
import com.example.identityservice.repository.VNPayPaymentRepository;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final VNPayPaymentRepository vnpayPaymentRepository;
    private final VNPayPaymentPremiumPackageRepository premiumPackageRepository;
    private final VNPayPaymentCoursesRepository coursesRepository;
    private final CourseClient courseClient;
    private final FirebaseAuthClient firebaseAuthClient;

    public List<DashboardMetricResponse> getSystemOverview() throws FirebaseAuthException {
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

        // --- New user ---
        int currentMonthNewUsers = countUsersByMonth(currentMonth, currentYear);
        int lastMonthNewUsers = countUsersByMonth(lastMonth, lastMonthYear);
        String userChange = formatChange(currentMonthNewUsers, lastMonthNewUsers);

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
                        .title("New Users")
                        .value(currentMonthNewUsers)
                        .change(userChange)
                        .changeNote("new users this month")
                        .changeType(getChangeType(userChange))
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

        List<Object[]> raw = vnpayPaymentRepository.sumRevenueByRange(
                unit,
                start.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<ChartDataPoint> data = raw.stream()
                .map(row -> {
                    Instant instant = (Instant) row[0];
                    Long amount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    String label = formatLabel(instant, unit);
                    return new ChartDataPoint(label, Math.toIntExact(amount));
                })
                .toList();

        return ChartResponse.builder()
                .type(type)
                .data(data)
                .build();
    }

    public ChartResponse getCourseCompletionRate(String type, LocalDate startDate, LocalDate endDate) {
        String unit = switch (type) {
            case "hourly" -> "hour";
            case "daily" -> "day";
            case "weekly" -> "week";
            case "monthly" -> "month";
            case "custom" -> "day";
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        // Get the data from courseClient
        List<Object[]> rawData = Objects.requireNonNull(courseClient.getCourseCompleteRate(type, startDate, endDate).block()).getResult();

        // Process the raw data into ChartDataPoint format
        List<ChartDataPoint> data = rawData.stream()
                .map(row -> {
                    Instant timestamp = Instant.parse(row[0].toString());
                    Double completionRate = (Double) row[1];  // Assuming the completion rate is in the second column
                    return new ChartDataPoint(formatLabel(timestamp, unit), completionRate.intValue());
                })
                .toList();

        // Return the chart response
        return ChartResponse.builder()
                .type(unit)
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

    public ChartResponse getUserGrowth(String type, LocalDate startDate, LocalDate endDate) throws FirebaseAuthException {
        String unit = switch (type) {
            case "hourly" -> "hour";
            case "daily" -> "day";
            case "weekly" -> "week";
            case "monthly" -> "month";
            case "custom" -> "day";
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };  

        List<ExportedUserRecord> allUsers = new ArrayList<>();
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);

        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                allUsers.add(user);
            }
            page = page.getNextPage();
        }

        ZoneId zoneId = ZoneId.systemDefault();
        Instant startInstant = (startDate != null) ? startDate.atStartOfDay(zoneId).toInstant() : Instant.EPOCH;
        Instant endInstant = (endDate != null) ? endDate.plusDays(1).atStartOfDay(zoneId).toInstant() : Instant.now();

        Map<String, Integer> groupedCounts = new TreeMap<>();

        for (ExportedUserRecord user : allUsers) {
            Instant creation = Instant.ofEpochMilli(user.getUserMetadata().getCreationTimestamp());

            if (!creation.isBefore(startInstant) && creation.isBefore(endInstant)) {
                String label = formatLabel(creation, unit);
                groupedCounts.put(label, groupedCounts.getOrDefault(label, 0) + 1);
            }
        }

        List<ChartDataPoint> chartData = groupedCounts.entrySet().stream()
                .map(entry -> new ChartDataPoint(entry.getKey(), entry.getValue()))
                .toList();

        return ChartResponse.builder()
                .type(unit)
                .data(chartData)
                .build();
    }

    public int countUsersByMonth(int month, int year) throws FirebaseAuthException {
        int count = 0;
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);

        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                long creationMillis = user.getUserMetadata().getCreationTimestamp();
                LocalDateTime creationDate = Instant.ofEpochMilli(creationMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                if (creationDate.getYear() == year && creationDate.getMonthValue() == month) {
                    count++;
                }
            }
            page = page.getNextPage();
        }
        return count;
    }

    public List<DashboardTableResponse> getRecentTransaction() {
        List<VNPayPayment> recentPayments = vnpayPaymentRepository
                .findTop10ByOrderByCreatedAtDesc();

        List<DashboardTableResponse> responses = new ArrayList<>();

        for (VNPayPayment payment : recentPayments) {
            UserInfoResponse user = firebaseAuthClient.getUserInfo(payment.getUserUid(), "");

            DashboardTableResponse response = DashboardTableResponse.builder()
                    .user(user)
                    .date(Date.from(payment.getCreatedAt()))
                    .amount(payment.getTotalPaymentAmount().doubleValue())
                    .status(payment.getTransactionStatus())
                    .type(resolveType(payment))
                    .build();

            responses.add(response);
        }

        return responses;
    }

    private String resolveType(VNPayPayment payment) {
        if (payment.getVnPayPaymentPremiumPackage() != null) {
            return "Premium";
        } else if (!payment.getPaymentCourses().isEmpty()) {
            return "Course";
        }
        return "Unknown";
    }

    public List<DashboardTableResponse> getTopPurchases() {
        List<VNPayPayment> payments = vnpayPaymentRepository.findAll();

        // Group by userUuid and sum total payment amount
        Map<String, Double> totalAmountPerUser = payments.stream()
                .filter(p -> p.getTransactionStatus().equalsIgnoreCase("00")) // Optional: only successful ones
                .collect(Collectors.groupingBy(
                        VNPayPayment::getUserUid,
                        Collectors.summingDouble(p -> p.getTotalPaymentAmount() != null ? p.getTotalPaymentAmount() : 0)
                ));

        List<DashboardTableResponse> topPurchases = new ArrayList<>();

        for (Map.Entry<String, Double> entry : totalAmountPerUser.entrySet()) {
            UserInfoResponse user = firebaseAuthClient.getUserInfo(entry.getKey(), "");

            topPurchases.add(DashboardTableResponse.builder()
                    .user(user)
                    .amount(entry.getValue())
                    .build());

        }

        // Sort descending by totalAmount
        return topPurchases.stream()
                .sorted(Comparator.comparing(DashboardTableResponse::getAmount).reversed())
                .limit(10) // Optional: top 10
                .collect(Collectors.toList());
    }

}