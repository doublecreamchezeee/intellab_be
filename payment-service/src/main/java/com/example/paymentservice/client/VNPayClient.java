package com.example.paymentservice.client;


import com.example.paymentservice.dto.request.vnpay.VNPayQueryRequest;
import com.example.paymentservice.dto.request.vnpay.VNPayRefundRequest;
import com.example.paymentservice.dto.response.vnpay.VNPayQueryResponse;
import com.example.paymentservice.dto.response.vnpay.VNPayRefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "vnpay-service", url = "${vnpay.api-url}")
public interface VNPayClient {
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    VNPayQueryResponse queryPayment(@RequestBody VNPayQueryRequest request);

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    VNPayRefundResponse refundPayment(@RequestBody VNPayRefundRequest request);

}
