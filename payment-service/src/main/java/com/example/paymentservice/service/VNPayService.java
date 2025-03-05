package com.example.paymentservice.service;

import com.example.paymentservice.client.CourseClient;
import com.example.paymentservice.dto.ApiResponse;
import com.example.paymentservice.dto.request.vnpay.VNPaySinglePaymentCreationRequest;
import com.example.paymentservice.dto.response.course.DetailCourseResponse;
import com.example.paymentservice.dto.response.vnpay.VNPayCallbackResponse;
import com.example.paymentservice.dto.response.vnpay.VNPayIPNReturnResponse;
import com.example.paymentservice.exception.AppException;
import com.example.paymentservice.exception.ErrorCode;
import com.google.gson.JsonObject;
import com.example.paymentservice.utils.HashUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequiredArgsConstructor
public class VNPayService {
    private final CourseClient courseClient;

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.api-url}")
    private String vnp_ApiUrl;

   /* @Value("${vnpay.version}")
    private String vnp_Version;

    @Value("${vnpay.command}")
    private String vnp_Command;

    @Value("${vnpay.curr-code}")
    private String vnp_CurrCode;
    */


    public String createSinglePayment(String ipAddr, VNPaySinglePaymentCreationRequest request, String userId) {
        try {
            ApiResponse<DetailCourseResponse> courseResponse = courseClient.getDetailCourseById(request.getCourseId());
            DetailCourseResponse course = courseResponse.getResult();

            if (course == null) {
                throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
            }

            return createPaymentUrl(
                    ipAddr,
                    (long) course.getPrice(),
                    request.getVNPayBankCode().getCode(),
                    request.getVNPayCurrencyCode().getCode(),
                    userId
            );

        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT);
        }

    }

    public String createPaymentUrl(String ipAddr, long amount, String bankCode, String currCode, String userId) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        String vnp_TxnRef = HashUtility.getRandomNumber(8);
        String orderInfo = "Payment for course(s): " + vnp_TxnRef;

        log.info("bankCode: " + bankCode);
        log.info("currCode: " + currCode);
        log.info("amount: " + amount);

        /*log.info("vnp_HashSecret: " + vnp_HashSecret);
        log.info("vnp_TmnCode: " + vnp_TmnCode);*/

        try {
            Map<String, String> vnpParams = new HashMap<>();

            vnpParams.put("vnp_Version", vnp_Version);
            vnpParams.put("vnp_Command", vnp_Command);
            vnpParams.put("vnp_TmnCode", vnp_TmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // Convert VND to smallest unit
            vnpParams.put("vnp_CurrCode", currCode);

            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParams.put("vnp_BankCode", bankCode);
            }

            vnpParams.put("vnp_TxnRef", vnp_TxnRef); //String.valueOf(System.currentTimeMillis()));
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", orderType);
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnpParams.put("vnp_IpAddr", ipAddr);

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_CreateDate", vnp_CreateDate);

            calendar.add(Calendar.MINUTE, 45);
            String vnp_ExpireDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);

            // Sort parameters
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String value = vnpParams.get(fieldName);
                if (value != null && !value.isEmpty()) {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
                            .append("&");

                    hashData.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
                            .append("&");
                }
            }

            // Remove last "&"
            query.setLength(query.length() - 1);
            hashData.setLength(hashData.length() - 1);

            // Generate secure hash
            String secureHash = HashUtility.hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            log.info(vnp_PayUrl + "?" + query);

            return vnp_PayUrl + "?" + query.toString();
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT);
        }
    }

    public String paymentResultReturn(HttpServletRequest request) {
        try {
            //Begin process return from VNPAY
            Map<String, Object> fields = new HashMap();
            for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");

            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }
            String signValue = HashUtility.hashAllFields(fields, vnp_HashSecret);
            return signValue;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPaymentInformation(String ipAddr, String orderId, String transDate)  {
        try {
            String vnp_RequestId = HashUtility.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_TxnRef = orderId;
            String vnp_OrderInfo = "Check payment for course: " + vnp_TxnRef;
            String vnp_TransDate = transDate;

            Calendar calender = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(calender.getTime());

            String vnp_IpAddr = ipAddr;

            JsonObject  vnp_Params = new JsonObject ();

            vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
            vnp_Params.addProperty("vnp_Version", vnp_Version);
            vnp_Params.addProperty("vnp_Command", vnp_Command);
            vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);
            //vnp_Params.put("vnp_TransactionNo", vnp_TransactionNo);
            vnp_Params.addProperty("vnp_TransactionDate", vnp_TransDate);
            vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);


            String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TxnRef, vnp_TransDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
            String vnp_SecureHash = HashUtility.hmacSHA512(vnp_HashSecret, hash_Data.toString());

            vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

            URL url = new URL(vnp_ApiUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(vnp_Params.toString());
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            System.out.println("nSending 'POST' request to URL : " + url);
            System.out.println("Post Data : " + vnp_Params);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();
            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();

            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public VNPayIPNReturnResponse handleIPNCallback(VNPayCallbackResponse response) {
        try {
            String RspCode = response.getVnp_ResponseCode();
            String vnp_SecureHash = response.getVnp_SecureHash();
            String Message = "Server intellab: Payment successful";
            if (!"00".equals(RspCode)) {
                Message = "Server intellab: Payment failed or canceled";
            }
            Map fields = new HashMap();

            fields.put("vnp_TmnCode", response.getVnp_TmnCode());
            fields.put("vnp_Amount", response.getVnp_Amount());
            fields.put("vnp_BankCode", response.getVnp_BankCode());
            fields.put("vnp_BankTranNo", response.getVnp_BankTranNo());
            fields.put("vnp_CardType", response.getVnp_CardType());
            fields.put("vnp_PayDate", response.getVnp_PayDate());
            fields.put("vnp_OrderInfo", response.getVnp_OrderInfo());
            fields.put("vnp_TransactionNo", response.getVnp_TransactionNo());
            fields.put("vnp_ResponseCode", response.getVnp_ResponseCode());
            fields.put("vnp_TransactionStatus", response.getVnp_TransactionStatus());
            fields.put("vnp_TxnRef", response.getVnp_TxnRef());

            String signingValue = HashUtility.hashAllFields(fields, vnp_HashSecret);
            if (signingValue.equals(vnp_SecureHash)) {
                boolean checkOrderId = true; // Giá trị của vnp_TxnRef tồn tại trong CSDL của merchant
                boolean checkAmount = true; //Kiểm tra số tiền thanh toán do VNPAY phản hồi(vnp_Amount/100) với số tiền của đơn hàng merchant tạo thanh toán: giả sử số tiền kiểm tra là đúng.
                boolean checkOrderStatus = true; // Giả sử PaymnentStatus = 0 (pending) là trạng thái thanh toán của giao dịch khởi tạo chưa có IPN.
                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(response.getVnp_ResponseCode())) {
                                //Xử lý/Cập nhật tình trạng giao dịch thanh toán "Thành công"
                                // out.print("GD Thanh cong");
                            } else {
                                //Xử lý/Cập nhật tình trạng giao dịch thanh toán "Không thành công"
                                //  out.print("GD Khong thanh cong");
                            }
                            return VNPayIPNReturnResponse.builder()
                                    .RspCode("00")
                                    .Message("Confirm Success")
                                    .build();
                        } else {
                            //Trạng thái giao dịch đã được cập nhật trước đó
                            return VNPayIPNReturnResponse.builder()
                                    .RspCode("02")
                                    .Message("Order already confirmed")
                                    .build();
                        }
                    } else {
                        //Số tiền không trùng khớp
                        return VNPayIPNReturnResponse.builder()
                                .RspCode("04")
                                .Message("Invalid Amount")
                                .build();
                    }
                } else {
                    //Mã giao dịch không tồn tại
                    return VNPayIPNReturnResponse.builder()
                            .RspCode("01")
                            .Message("Order not Found")
                            .build();
                }

            } else {
                // Sai checksum
                return VNPayIPNReturnResponse.builder()
                        .RspCode("97")
                        .Message("Invalid Checksum")
                        .build();
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_HANDLE_IPN_CALLBACK);
        }


    }

}
