package com.example.identityservice.scheduler;

import com.example.identityservice.client.CourseClient;
import com.example.identityservice.configuration.SchedulerConfig;
import com.example.identityservice.dto.request.course.DisenrollCoursesEnrolledUsingSubscriptionPlanRequest;
import com.example.identityservice.enums.account.PremiumPackage;
import com.example.identityservice.enums.account.PremiumPackageStatus;
import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import com.example.identityservice.repository.VNPayPaymentPremiumPackageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VNPayPaymentPremiumPackageScheduler {
    VNPayPaymentPremiumPackageRepository vnPayPaymentPremiumPackageRepository;
    SchedulerConfig schedulerConfig;
    CourseClient courseClient;

    @Scheduled(cron = "0 0 12 * * ?")
    public void checkPremiumPackageEndDateNoon() {
        log.info("Running scheduled task at 12:00 PM to check VNPayPaymentPremiumPackage end dates");
        checkPremiumPackageEndDate();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkPremiumPackageEndDateMidnight() {
        log.info("Running scheduled task at 12:00 AM to check VNPayPaymentPremiumPackage end dates");
        checkPremiumPackageEndDate();
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void checkPremiumPackageEndDateEleven() {
        log.info("Running scheduled task at 11:00 PM to check VNPayPaymentPremiumPackage end dates");
        checkPremiumPackageEndDate();
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void runHourlyTask() {
        log.info("Running scheduled task every hour");
        checkPremiumPackageEndDate();
    }

    @Scheduled(cron = "#{@schedulerConfig.customCronExpression}")
    public void checkPremiumPackageAtCustomTime() {
        log.info("Running scheduled task to check premium package end date");
        checkPremiumPackageEndDate();
    }

    public void checkPremiumPackageEndDate() {
        Instant now = Instant.now();
        List<VNPayPaymentPremiumPackage> expiredPackages = vnPayPaymentPremiumPackageRepository.findAllByEndDateBeforeAndStatus(now, PremiumPackageStatus.ACTIVE.getCode());

        for (VNPayPaymentPremiumPackage premiumPackage : expiredPackages) {
            // Perform the necessary actions for expired packages
            //log.info("Premium package expired: {}", premiumPackage);
            // Example: Update status to "expired"
            premiumPackage.setStatus(PremiumPackageStatus.EXPIRED.getCode());
            vnPayPaymentPremiumPackageRepository.save(premiumPackage);
        }

        try {
            // Disenroll users from courses if their course plan or premium plan has expired
            List<UUID> uuids = expiredPackages
                    .stream()
                    .filter(premiumPackage ->
                            premiumPackage.getPackageType() != null &&
                                    (
                                            premiumPackage.getPackageType().equals(PremiumPackage.COURSE_PLAN.getCode()) ||
                                                    premiumPackage.getPackageType().equals(PremiumPackage.PREMIUM_PLAN.getCode())
                                    )
                    )
                    .map(VNPayPaymentPremiumPackage::getUserUuid)
                    .toList();

            log.info("Disenrolling users uuid with expired premium packages from courses: {}", uuids);

            if (uuids.isEmpty()) {
                return;
            }

            courseClient.disenrollCoursesEnrolledUsingSubscriptionPlan(
                    DisenrollCoursesEnrolledUsingSubscriptionPlanRequest
                            .builder()
                            .listUserUuid(uuids)
                            .build()
            ).block();

        } catch (Exception e) {
            log.error("Error calling course service while disenrolling courses for users with expired premium packages", e);
        }
    }

}
