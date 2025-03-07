package com.example.paymentservice.controller;


import com.example.paymentservice.configuration.RedirectUrlConfig;
import com.example.paymentservice.dto.ApiResponse;
import com.example.paymentservice.dto.request.vnpay.VNPaySinglePaymentCreationRequest;
import com.example.paymentservice.dto.response.vnpay.*;
import com.example.paymentservice.service.VNPayService;
import com.example.paymentservice.utils.HashUtility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/vnpay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "VN Pay")
@Slf4j
public class VNPayController {
    private VNPayService vnPayService;
    private RedirectUrlConfig redirectUrlConfig;

    @Operation(
            summary = "Create payment",
            description = "Create payment, using payment url (deprecated)",
            tags = "VN Pay"
    )
    @PostMapping("/create-single-course-payment")
    public ApiResponse<VNPayPaymentCreationResponse> createOrder(
            HttpServletRequest request,
            @RequestBody VNPaySinglePaymentCreationRequest vnPaySinglePaymentCreationRequest,
            @RequestHeader("X-UserId") String userId
     ) {
        userId = userId.split(",")[0];

        String ipAddr = HashUtility.getIpAddress(request); //request.getRemoteAddr();

        VNPayPaymentCreationResponse response = vnPayService.createSinglePayment(
                ipAddr,
                vnPaySinglePaymentCreationRequest,
                userId
        );

        return  ApiResponse.<VNPayPaymentCreationResponse>builder()
                .message("success")
                .result(response)
                .build();
    }

    @Operation(
            summary = "BE only",
            description = "Return final payment result from VNPay server",
            tags = "VN Pay",
            hidden = true
    )
    @GetMapping("/vnpay-return")
    public RedirectView paymentResultCallback(@ModelAttribute VNPayCallbackResponse params) {

        // Because vnpay server doesn't call ipn endpoint,
        // so we need to handle the payment result here
        VNPayIPNReturnResponse handlingResponse = vnPayService.handleIPNCallback(params);
        log.info("handlingResponse ipn: {}", handlingResponse);
        log.info("params return from vnpay: {}", params);
        UUID paymentId = vnPayService.getPaymentIdByTransactionReference(params.getVnp_TxnRef());
        return new RedirectView(redirectUrlConfig.getRedirectUrl() + paymentId);
    }

    @Operation(
            summary = "get payment information from VNPay server",
            description = "VNPay Query payment",
            tags = "VN Pay"
    )
    @GetMapping("/vnpay-payment-info/{paymentId}")
    public ApiResponse<VNPayQueryResponse> getPaymentInfo(
            HttpServletRequest request,
            @PathVariable UUID paymentId
    ) {
        String ipAddr = HashUtility.getIpAddress(request);

        VNPayQueryResponse response = vnPayService.getPaymentInformationInVNPayServer(ipAddr, paymentId);

        return ApiResponse.<VNPayQueryResponse>builder()
                .message("success")
                .result(response)
                .build();
    }

    @Operation(
            summary = "VNPay refund payment",
            description = "VNPay refund payment",
            tags = "VN Pay"
    )
    @PostMapping("/vnpay-refund")
    public ApiResponse<VNPayRefundResponse> refundPayment(
            HttpServletRequest request,
            @RequestParam UUID paymentId
    ) {
        String ipAddr = HashUtility.getIpAddress(request);

        VNPayRefundResponse response = vnPayService.refundPayment(
                ipAddr,
                paymentId
        );

        return ApiResponse.<VNPayRefundResponse>builder()
                .message("success")
                .result(response)
                .build();
    }

    @Operation(
            summary = "BE only",
            description = "callback IPN of VNPay server",
            tags = "VN Pay",
            hidden = true
    )
    @GetMapping("/vnpay-ipn")
    public VNPayIPNReturnResponse ipnCallback(@ModelAttribute VNPayCallbackResponse params) {
        log.info("IPN callback: {}", params);
        return vnPayService.handleIPNCallback(params);
    }

    @Operation(
            summary = "BE only",
            description = "callback IPN of VNPay server",
            tags = "VN Pay",
            hidden = true
    )
    @GetMapping("/vnpay-test")
    public Map<String, Object> testIpnCallback(@RequestParam Map<String, Object> params) {
        return params;
    }

    @Operation(
            summary = "get a payment in intellab database, using payment id",
            tags = "VN Pay"
    )
    @GetMapping("/get-payment/{paymentId}")
    public ApiResponse<VNPayDetailsPaymentResponse> getPaymentDetails(
            @PathVariable UUID paymentId
    ) {
        VNPayDetailsPaymentResponse response = vnPayService.getPaymentDetailsByPaymentId(paymentId);

        return ApiResponse.<VNPayDetailsPaymentResponse>builder()
                .message("success")
                .result(response)
                .build();
    }

    @Operation(
            summary = "get list payment in intellab database of a user, using user id",
            tags = "VN Pay"
    )
    @GetMapping("/get-payments/me")
    public ApiResponse<Page<VNPayDetailsPaymentResponse>> getPayments(
            @ParameterObject Pageable pageable,
            @RequestHeader(value = "X-UserId", required = true) String userId
    ) {
        userId = userId.split(",")[0];

        Page<VNPayDetailsPaymentResponse> response = vnPayService.getListPaymentDetailsByUserUid(userId, pageable);

        return ApiResponse.<Page<VNPayDetailsPaymentResponse>>builder()
                .message("success")
                .result(response)
                .build();
    }

    @Operation(
            summary = "Update redirect URL",
            description = "Update the redirect URL for payment result",
            tags = "VN Pay"
    )
    @PostMapping("/update-redirect-url")
    public ApiResponse<String> updateRedirectUrl(@RequestParam String newUrl) {
        redirectUrlConfig.setRedirectUrl(redirectUrlConfig.getFeUrl() + newUrl);
        return ApiResponse.<String>builder()
                .message("Redirect URL updated successfully")
                .result(newUrl)
                .build();
    }

    /*@PostMapping("/test")
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
    }*/
}
