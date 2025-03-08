package com.example.identityservice.mapper;

import com.example.identityservice.dto.response.vnpay.VNPayDetailsPaymentResponse;
import com.example.identityservice.dto.response.vnpay.VNPayPaymentCreationResponse;
import com.example.identityservice.model.VNPayPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.identityservice.constant.VNPayTransactionStatus;
import com.example.identityservice.constant.response.vnpay.VNPayPayResponseCode;
@Mapper(componentModel = "spring")
public interface VNPayPaymentMapper {
    @Mapping(target = "paymentUrl", ignore = true)
    @Mapping(target = "transactionStatusDescription", ignore = true)
    VNPayPaymentCreationResponse toVNPayPaymentCreationResponse(VNPayPayment VNPayPayment);

    @Mapping(target = "transactionStatusDescription", expression = "java(com.example.identityservice.constant.VNPayTransactionStatus.getDescription(VNPayPayment.getTransactionStatus()))")
    @Mapping(target = "responseCodeDescription", expression = "java(com.example.identityservice.constant.response.vnpay.VNPayPayResponseCode.getDescription(VNPayPayment.getResponseCode()))")
    VNPayDetailsPaymentResponse toVNPayDetailsPaymentResponse(VNPayPayment VNPayPayment);

}
