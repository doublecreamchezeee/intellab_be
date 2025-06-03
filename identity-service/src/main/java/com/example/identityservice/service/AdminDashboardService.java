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
import java.time.temporal.ChronoUnit;
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

    private String resolveUnit(String type, LocalDate startDate, LocalDate endDate) {
        return switch (type) {
            case "monthly" -> "week"; // show 4 weeks
            case "yearly" -> "month"; // show 12 months
            case "custom" -> {
                if (startDate != null && endDate != null) {
                    long days = ChronoUnit.DAYS.between(startDate, endDate);
                    if (days < 7) yield "day";
                    else if (days <= 30) yield "week";
                    else yield "month";
                } else {
                    yield "day"; // default fallback
                }
            }
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
    }

    private String formatLabel(Instant instant, String unit) {
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        return switch (unit) {
            case "week" -> "W" + zdt.get(WeekFields.ISO.weekOfWeekBasedYear()) + " " + zdt.getYear();
            case "month" -> zdt.format(DateTimeFormatter.ofPattern("MMM"));
            case "day" -> zdt.format(DateTimeFormatter.ofPattern("dd-MMM"));
//            case "custom" -> zdt.format(DateTimeFormatter.ofPattern("dd-MM")); // Optional
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }

    private List<String> generateTimeline(String unit, LocalDate start, LocalDate end) {
        List<String> timeline = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            Instant instant = current.atStartOfDay(ZoneId.systemDefault()).toInstant();
            timeline.add(formatLabel(instant, unit));

            switch (unit) {
                case "day" -> current = current.plusDays(1);
                case "week" -> current = current.plusWeeks(1);
                case "month" -> current = current.plusMonths(1);
                default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
            }
        }

        return timeline;
    }

    private List<LocalDate> resolveDateRange(String unit, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return List.of(startDate, endDate);
        }

        LocalDate now = LocalDate.now();

        return switch (unit) {
            case "day" -> List.of(
                    now.with(DayOfWeek.MONDAY),
                    now.with(DayOfWeek.MONDAY).plusDays(6)
            );
            case "week" -> List.of(
                    now.withDayOfMonth(1),
                    now.withDayOfMonth(1).plusWeeks(4).minusDays(1)
            );
            case "month" -> List.of(
                    now.withDayOfYear(1),
                    now.withMonth(12).withDayOfMonth(31)
            );
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }

    public ChartResponse getSubscriptionGrowth(String type, LocalDate startDate, LocalDate endDate) {
        String unit = resolveUnit(type, startDate, endDate);

        List<LocalDate> range = resolveDateRange(unit, startDate, endDate);
        LocalDate start = range.get(0);
        LocalDate end = range.get(1);

        List<Object[]> raw = premiumPackageRepository.countSubscriptionsByRange(
                unit,
                start.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Map<String, Integer> rawMap = raw.stream().collect(Collectors.toMap(
                row -> formatLabel((Instant) row[0], unit),
                row -> ((Number) row[1]).intValue()
        ));

        List<ChartDataPoint> data = generateTimeline(unit, start, end).stream()
                .map(label -> new ChartDataPoint(label, rawMap.getOrDefault(label, 0)))
                .toList();

        return ChartResponse.builder()
                .type(type)
                .data(data)
                .build();
    }

    public ChartResponse getRevenue(String type, LocalDate startDate, LocalDate endDate) {
        String unit = resolveUnit(type, startDate, endDate);

        List<LocalDate> range = resolveDateRange(unit, startDate, endDate);
        LocalDate start = range.get(0);
        LocalDate end = range.get(1);

        List<Object[]> raw = vnpayPaymentRepository.sumRevenueByRange(
                unit,
                start.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Map<String, Integer> rawMap = raw.stream().collect(Collectors.toMap(
                row -> formatLabel((Instant) row[0], unit),
                row -> ((row[1] != null) ? ((Number) row[1]).intValue() : 0)
        ));

        List<ChartDataPoint> data = generateTimeline(unit, start, end).stream()
                .map(label -> new ChartDataPoint(label, rawMap.getOrDefault(label, 0)))
                .toList();

        return ChartResponse.builder()
                .type(type)
                .data(data)
                .build();
    }

    public ChartResponse getCourseCompletionRate(String type, LocalDate startDate, LocalDate endDate) {
        String unit = resolveUnit(type, startDate, endDate);

        List<LocalDate> range = resolveDateRange(unit, startDate, endDate);
        LocalDate start = range.get(0);
        LocalDate end = range.get(1);

        List<Object[]> rawData = Objects.requireNonNull(courseClient.getCourseCompleteRate(type, start, end).block()).getResult();

        Map<String, Integer> rawMap = rawData.stream().collect(Collectors.toMap(
                row -> formatLabel(Instant.parse(row[0].toString()), unit),
                row -> ((Double) row[1]).intValue()
        ));

        List<ChartDataPoint> data = generateTimeline(unit, start, end).stream()
                .map(label -> new ChartDataPoint(label, rawMap.getOrDefault(label, 0)))
                .toList();

        return ChartResponse.builder()
                .type(type)
                .data(data)
                .build();
    }

    public ChartResponse getUserGrowth(String type, LocalDate startDate, LocalDate endDate) throws FirebaseAuthException {
        String unit = resolveUnit(type, startDate, endDate);

        List<LocalDate> range = resolveDateRange(unit, startDate, endDate);
        LocalDate start = range.get(0);
        LocalDate end = range.get(1);

        ZoneId zoneId = ZoneId.systemDefault();
        Instant startInstant = start.atStartOfDay(zoneId).toInstant();
        Instant endInstant = end.atStartOfDay(zoneId).toInstant();

        List<ExportedUserRecord> allUsers = new ArrayList<>();
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        while (page != null) {
            page.getValues().forEach(allUsers::add);
            page = page.getNextPage();
        }

        Map<String, Integer> groupedCounts = new HashMap<>();
        for (ExportedUserRecord user : allUsers) {
            Instant creation = Instant.ofEpochMilli(user.getUserMetadata().getCreationTimestamp());
            if (!creation.isBefore(startInstant) && creation.isBefore(endInstant)) {
                String label = formatLabel(creation, unit);
                groupedCounts.put(label, groupedCounts.getOrDefault(label, 0) + 1);
            }
        }

        List<ChartDataPoint> data = generateTimeline(unit, start, end).stream()
                .map(label -> new ChartDataPoint(label, groupedCounts.getOrDefault(label, 0)))
                .toList();

        return ChartResponse.builder()
                .type(type)
                .data(data)
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