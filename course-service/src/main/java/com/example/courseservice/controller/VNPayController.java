package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.service.VNPayService;
import com.example.courseservice.utils.HashUtility;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/vnpay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "VN Pay")
@Slf4j
public class VNPayController {
    private VNPayService vnPayService;

    @GetMapping("/create")
    public ApiResponse<String> createPayment(HttpServletRequest request,
                                             @RequestParam long amount,
                                             @RequestParam String bankCode,
                                             @RequestParam String currCode) {
        String ipAddr = HashUtility.getIpAddress(request); //request.getRemoteAddr();

        String paymentUrl = vnPayService.createPaymentUrl(
                amount,
                bankCode,
                ipAddr,
                currCode
        );

        return  ApiResponse.<String>builder()
                .message("success")
                .result(paymentUrl)
                .build();
    }

    @GetMapping("/vnpay-return")
    public Map<String, Object> paymentReturn(@RequestParam Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        if ("00".equals(params.get("vnp_ResponseCode"))) {
            response.put("status", "success");
            response.put("message", "Server intellab: Payment successful");
        } else {
            response.put("status", "failure");
            response.put("message", "Server intellab: Payment failed or canceled");
        }
        return response;
    }

    @GetMapping("/vnpay-trans")
    public ApiResponse<String> getPaymentInfo(
            HttpServletRequest request,
            @RequestParam String orderId,
            @RequestParam String transDate) {
        String ipAddr = HashUtility.getIpAddress(request); //request.getRemoteAddr();


        String url = vnPayService.getPaymentInformation(ipAddr, orderId, transDate);

        return ApiResponse.<String>builder()
                .message("success")
                .result(url)
                .build();
    }

}
