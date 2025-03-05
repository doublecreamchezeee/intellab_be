package com.example.paymentservice.controller;


import com.example.paymentservice.dto.ApiResponse;
import com.example.paymentservice.dto.request.vnpay.VNPaySinglePaymentCreationRequest;
import com.example.paymentservice.dto.response.vnpay.VNPayCallbackResponse;
import com.example.paymentservice.dto.response.vnpay.VNPayIPNReturnResponse;
import com.example.paymentservice.service.VNPayService;
import com.example.paymentservice.utils.HashUtility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @Operation(
            summary = "Create order",
            description = "Create order for payment, using payment url",
            tags = "VN Pay"
    )
    @PostMapping("/create-single-course-payment")
    public ApiResponse<String> createOrder(
            HttpServletRequest request,
            @RequestBody VNPaySinglePaymentCreationRequest vnPaySinglePaymentCreationRequest,
            @RequestHeader("X-UserId") String userId
     ) {
        userId = userId.split(",")[0];

        String ipAddr = HashUtility.getIpAddress(request); //request.getRemoteAddr();

        String paymentUrl = vnPayService.createSinglePayment(
                ipAddr,
                vnPaySinglePaymentCreationRequest,
                userId
        );

        return  ApiResponse.<String>builder()
                .message("success")
                .result(paymentUrl)
                .build();
    }

    @Operation(
            summary = "BE only",
            description = "Return final payment result from VNPay server",
            tags = "VN Pay"
    )
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

    @GetMapping("/vnpay-payment-info")
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

    @Operation(
            summary = "BE only",
            description = "callback IPN of VNPay server",
            tags = "VN Pay"
    )
    @GetMapping("/vnpay-ipn")
    public VNPayIPNReturnResponse ipnCallback(@RequestParam VNPayCallbackResponse params) {
        log.info("IPN callback: {}", params);
        return vnPayService.handleIPNCallback(params);
    }

    @PostMapping("/test")
    public ApiResponse<String> test(
            HttpServletRequest request

    ) {

        String ipAddr = HashUtility.getIpAddress(request); //request.getRemoteAddr();

        String paymentUrl = vnPayService.createPaymentUrl(
                ipAddr,
                1000000L,
                "VNBANK",
                "VND",
                "http://localhost:8080/vnpay/vnpay-return"
        );

        return  ApiResponse.<String>builder()
                .message("success")
                .result(paymentUrl)
                .build();
    }
}
