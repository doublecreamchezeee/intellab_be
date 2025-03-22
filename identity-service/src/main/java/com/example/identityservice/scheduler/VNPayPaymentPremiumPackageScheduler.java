package com.example.identityservice.scheduler;

import com.example.identityservice.configuration.SchedulerConfig;
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

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VNPayPaymentPremiumPackageScheduler {
    VNPayPaymentPremiumPackageRepository vnPayPaymentPremiumPackageRepository;
    SchedulerConfig schedulerConfig;

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

    @Scheduled(cron = "0 2 23 * * ?")
    public void checkPremiumPackageEndDateEleven() {
        log.info("Running scheduled task at 11:00 PM to check VNPayPaymentPremiumPackage end dates");
        checkPremiumPackageEndDate();
    }

    @Scheduled(cron = "#{@schedulerConfig.customCronExpression}")
    public void checkPremiumPackageAtCustomTime() {
        log.info("Running scheduled task to check premium package end date");
        checkPremiumPackageEndDate();
    }

    private void checkPremiumPackageEndDate() {
        Instant now = Instant.now();
        List<VNPayPaymentPremiumPackage> expiredPackages = vnPayPaymentPremiumPackageRepository.findByEndDateBefore(now);

        for (VNPayPaymentPremiumPackage premiumPackage : expiredPackages) {
            // Perform the necessary actions for expired packages
            log.info("Premium package expired: {}", premiumPackage);
            // Example: Update status to "expired"
            premiumPackage.setStatus("expired");
            vnPayPaymentPremiumPackageRepository.save(premiumPackage);
        }
    }

}
