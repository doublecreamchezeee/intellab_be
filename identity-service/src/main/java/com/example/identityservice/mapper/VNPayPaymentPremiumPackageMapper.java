package com.example.identityservice.mapper;

import com.example.identityservice.dto.response.auth.PremiumSubscriptionResponse;
import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface VNPayPaymentPremiumPackageMapper {
    @Mapping(target = "planType", source = "packageType")
    @Mapping(target = "durationInDays", source = "duration")
    @Mapping(target = "durationEnums", source = "duration", qualifiedByName = "mapDurationEnums")
    PremiumSubscriptionResponse toPremiumSubscriptionResponse(VNPayPaymentPremiumPackage vNPayPaymentPremiumPackage);

    @Named("mapDurationEnums")
    default String mapDurationEnums(Integer duration) {
        if (duration == 365) {
            return "YEARLY_PACKAGE";
        } else if (duration == 30) {
            return "MONTHLY_PACKAGE";
        } else {
            return "UNKNOWN";
        }
    }
}
