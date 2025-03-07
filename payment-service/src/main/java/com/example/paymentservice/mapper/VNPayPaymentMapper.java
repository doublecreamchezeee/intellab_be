package com.example.paymentservice.mapper;

import com.example.paymentservice.dto.response.vnpay.VNPayDetailsPaymentResponse;
import com.example.paymentservice.dto.response.vnpay.VNPayPaymentCreationResponse;
import com.example.paymentservice.model.VNPayPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VNPayPaymentMapper {
    @Mapping(target = "paymentUrl", ignore = true)
    @Mapping(target = "transactionStatusDescription", ignore = true)
    VNPayPaymentCreationResponse toVNPayPaymentCreationResponse(VNPayPayment VNPayPayment);

    @Mapping(target = "transactionStatusDescription", expression = "java(com.example.paymentservice.constant.VNPayTransactionStatus.getDescription(VNPayPayment.getTransactionStatus()))")
    @Mapping(target = "responseCodeDescription", expression = "java(com.example.paymentservice.constant.VNPayTransactionStatus.getDescription(VNPayPayment.getResponseCode()))")
    VNPayDetailsPaymentResponse toVNPayDetailsPaymentResponse(VNPayPayment VNPayPayment);

}
