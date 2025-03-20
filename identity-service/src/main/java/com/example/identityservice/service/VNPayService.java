package com.example.identityservice.service;


import com.example.identityservice.client.CourseClient;
import com.example.identityservice.client.VNPayClient;
import com.example.identityservice.constant.VNPayTransactionStatus;
import com.example.identityservice.constant.response.vnpay.VNPayPayResponseCode;
import com.example.identityservice.constant.response.vnpay.VNPayQueryResponseCode;
import com.example.identityservice.constant.response.vnpay.VNPayRefundResponseCode;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.course.DisenrollCourseRequest;
import com.example.identityservice.dto.request.course.EnrollCourseRequest;
import com.example.identityservice.dto.request.vnpay.VNPayQueryRequest;
import com.example.identityservice.dto.request.vnpay.VNPayRefundRequest;
import com.example.identityservice.dto.request.vnpay.VNPaySinglePaymentCreationRequest;
import com.example.identityservice.dto.request.vnpay.VNPayUpgradeAccountRequest;
import com.example.identityservice.dto.response.course.DetailCourseResponse;
import com.example.identityservice.dto.response.userCourse.UserCoursesResponse;
import com.example.identityservice.dto.response.vnpay.*;
import com.example.identityservice.enums.vnpay.VNPayRefundType;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.mapper.VNPayPaymentMapper;
import com.example.identityservice.model.VNPayPayment;
import com.example.identityservice.model.VNPayPaymentCourses;
import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import com.example.identityservice.model.composite.VNPayPaymentCoursesId;
import com.example.identityservice.repository.VNPayPaymentCoursesRepository;
import com.example.identityservice.repository.VNPayPaymentPremiumPackageRepository;
import com.example.identityservice.repository.VNPayPaymentRepository;
import com.example.identityservice.utility.HashUtility;
import com.example.identityservice.utility.ParseUUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequiredArgsConstructor
public class VNPayService {
    private final VNPayClient vnpayClient;
    private final CourseClient courseClient;
    private final VNPayPaymentRepository vnPayPaymentRepository;
    private final VNPayPaymentMapper vnPaymentMapper;
    private final VNPayPaymentCoursesRepository vnPayPaymentCoursesRepository;
    private final VNPayPaymentPremiumPackageRepository vnPayPaymentPremiumPackageRepository;

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

    public VNPayPaymentCreationResponse createSingleCoursePayment(
            String ipAddr, VNPaySinglePaymentCreationRequest request, String userUid) {
        try {
            // Get course information and check if course is existed
            ApiResponse<DetailCourseResponse> courseResponse = courseClient.getDetailCourseById(request.getCourseId()).block();
            DetailCourseResponse course = courseResponse.getResult();

            if (course == null) {
                throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
            }

            // create description for order
            String orderDescription = null;

            if (request.getLanguage().getCode().equals("vi")) {
                orderDescription = "Thanh toan cho khoa hoc ";
            } else {
                orderDescription = "Payment for courses ";
            }

            // Create payment url
            VNPayPaymentUrlResponse paymentUrlResponse =  createPaymentUrl(
                    ipAddr,
                    (long) course.getPrice(),
                    request.getVNPayBankCode().getCode(),
                    request.getVNPayCurrencyCode().getCode(),
                    request.getLanguage().getCode(),
                    orderDescription
            );

            // save payment to database
            VNPayPayment payment = VNPayPayment.builder()
                    .userUid(userUid)
                    .userUuid(ParseUUID.normalizeUID(userUid))
                    .transactionStatus("01") // Pending transaction
                    .totalPaymentAmount(course.getPrice())
                    .currency(request.getVNPayCurrencyCode().getCode())
                    .paidAmount(0.0f)
                    .bankCode(request.getVNPayBankCode().getCode())
                    .transactionReference(paymentUrlResponse.getTransactionReference())
                    .createdAt(paymentUrlResponse.getCurrentDate().toInstant())
                    .build();

            payment = vnPayPaymentRepository.save(payment);

            // Save payment of courses to database
            VNPayPaymentCoursesId id = VNPayPaymentCoursesId.builder()
                    .courseId(request.getCourseId())
                    .userUid(userUid)
                    .build();

            VNPayPaymentCourses paymentCourses = VNPayPaymentCourses.builder()
                    .id(id)
                    .payment(payment)
                    .build();

            vnPayPaymentCoursesRepository.save(paymentCourses);

            // Create response
            VNPayPaymentCreationResponse response = vnPaymentMapper.toVNPayPaymentCreationResponse(payment);

            response.setPaymentUrl(paymentUrlResponse.getPaymentUrl());

            response.setTransactionStatusDescription(
                    VNPayTransactionStatus
                            .getDescription(
                                    payment.getTransactionStatus()
                            )
            );

            return response;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT);
        }

    }

    public VNPayPaymentCreationResponse createUpgradeAccountPayment(
            String ipAddr, VNPayUpgradeAccountRequest request, String userUid
    ) {
        try {
            // create description for order
            String orderDescription = null;

            if (request.getLanguage().getCode().equals("vi")) {
                orderDescription = "Nang cap tai khoan " ;
            } else {
                orderDescription = "Payment for courses ";
            }

            orderDescription += request.getPremiumPackage().getCode() + " package ";

            // Create payment url
            VNPayPaymentUrlResponse paymentUrlResponse =  createPaymentUrl(
                    ipAddr,
                    (long) request.getPremiumPackage().getPrice(),
                    request.getVNPayBankCode().getCode(),
                    request.getVNPayCurrencyCode().getCode(),
                    request.getLanguage().getCode(),
                    orderDescription
            );

            // Save payment of premium package to database
            VNPayPaymentPremiumPackage paymentPremiumPackage = VNPayPaymentPremiumPackage.builder()
                    .userUid(userUid)
                    .userUuid(ParseUUID.normalizeUID(userUid))
                    //.payment(payment)
                    .packageType(request.getPremiumPackage().getCode())
                    .startDate(null)
                    .build();

            paymentPremiumPackage = vnPayPaymentPremiumPackageRepository.save(paymentPremiumPackage);

            // save payment to database
            VNPayPayment payment = VNPayPayment.builder()
                    .userUid(userUid)
                    .userUuid(ParseUUID.normalizeUID(userUid))
                    .transactionStatus("01") // Pending transaction
                    .totalPaymentAmount(Float.valueOf(request.getPremiumPackage().getPrice()))
                    .currency(request.getVNPayCurrencyCode().getCode())
                    .paidAmount(0.0f)
                    .bankCode(request.getVNPayBankCode().getCode())
                    .transactionReference(paymentUrlResponse.getTransactionReference())
                    .createdAt(paymentUrlResponse.getCurrentDate().toInstant())
                    .vnPayPaymentPremiumPackage(paymentPremiumPackage)
                    .build();

            payment = vnPayPaymentRepository.save(payment);

            // create response
            VNPayPaymentCreationResponse response = vnPaymentMapper.toVNPayPaymentCreationResponse(payment);

            response.setPaymentUrl(paymentUrlResponse.getPaymentUrl());

            response.setTransactionStatusDescription(
                    VNPayTransactionStatus
                            .getDescription(
                                    payment.getTransactionStatus()
                            )
            );

            return response;

        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT);
        }
    }

    public VNPayPaymentUrlResponse createPaymentUrl(
            String ipAddr, long amount,
            String bankCode, String currCode,
            String locale, String orderDescription
    ) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        String vnp_TxnRef = HashUtility.getRandomNumber(8);
        String orderInfo = orderDescription != null && !orderDescription.isEmpty()
                ? orderDescription + vnp_TxnRef
                : "Payment for something " + vnp_TxnRef;

        /*if (locale.equals("vi")) {
            orderInfo = "Thanh toan cho khoa hoc " + vnp_TxnRef;
        } else {
            orderInfo = "Payment for courses " + vnp_TxnRef;
        }*/

        /*log.info("vnp_HashSecret: " + vnp_HashSecret);
        log.info("vnp_TmnCode: " + vnp_TmnCode);
        log.info("bankCode: " + bankCode);
        log.info("currCode: " + currCode);
        log.info("amount: " + amount);*/

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
            vnpParams.put("vnp_Locale", locale);
            vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnpParams.put("vnp_IpAddr", ipAddr);

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date currentDate = calendar.getTime();
            String vnp_CreateDate = formatter.format(currentDate);
            vnpParams.put("vnp_CreateDate", vnp_CreateDate);


            calendar.add(Calendar.MINUTE, 30);
            String vnp_ExpireDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);

            log.info("Current vnp_CreateDate: {}", vnp_CreateDate);
            log.info("Current vnp_ExpireDate: {}", vnp_ExpireDate);

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

            //log.info("Secure hash: {}", secureHash);


            // log.info(vnp_PayUrl + "?" + query);
            return VNPayPaymentUrlResponse.builder()
                    .paymentUrl(vnp_PayUrl + "?" + query.toString())
                    .transactionReference(vnp_TxnRef)
                    .currentDate(currentDate)
                    .build();
            //return vnp_PayUrl + "?" + query;
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

    public VNPayQueryResponse getPaymentInformationInVNPayServer(
            String ipAddr, UUID paymentId
    )  {

        VNPayPayment payment = vnPayPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        try {
            Calendar calender = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

            String vnp_RequestId = HashUtility.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_TxnRef = payment.getTransactionReference();
            String vnp_OrderInfo = "Check result of transaction of order Id: " + vnp_TxnRef;
            String vnp_TransDate = formatter.format(
                    payment.getReceivedAt() != null
                            ?  Date.from(payment.getReceivedAt())
                            : Date.from(payment.getCreatedAt())
            );

            String vnp_CreateDate = formatter.format(calender.getTime());

            String vnp_IpAddr = ipAddr;

            String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TxnRef, vnp_TransDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
            String vnp_SecureHash = HashUtility.hmacSHA512(vnp_HashSecret, hash_Data.toString());

            /*JsonObject vnp_Params = new JsonObject ();
            vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
            vnp_Params.addProperty("vnp_Version", vnp_Version);
            vnp_Params.addProperty("vnp_Command", vnp_Command);
            vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);
            //vnp_Params.put("vnp_TransactionNo", vnp_TransactionNo);
            vnp_Params.addProperty("vnp_TransactionDate", vnp_TransDate);
            vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);*/

            VNPayQueryRequest request = VNPayQueryRequest
                    .builder()
                    .vnp_RequestId(vnp_RequestId)
                    .vnp_Version(vnp_Version)
                    .vnp_Command(vnp_Command)
                    .vnp_TmnCode(vnp_TmnCode)
                    .vnp_TxnRef(vnp_TxnRef)
                    .vnp_OrderInfo(vnp_OrderInfo)
                    .vnp_TransactionDate(vnp_TransDate)
                    .vnp_CreateDate(vnp_CreateDate)
                    .vnp_IpAddr(vnp_IpAddr)
                    .vnp_SecureHash(vnp_SecureHash)
                    .build();

            VNPayQueryResponse response = vnpayClient.queryPayment(request);

            response.setVnp_TransactionStatusDescription(
                    VNPayTransactionStatus
                            .getDescription(
                                    response.getVnp_TransactionStatus()
                            )
            );

            response.setVnp_ResponseCodeDescription(
                    VNPayQueryResponseCode
                            .getDescription(
                                    response.getVnp_ResponseCode()
                            )
            );
            //vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);
            /*URL url = new URL(vnp_ApiUrl);
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
            StringBuffer response2 = new StringBuffer();
            while ((output = in.readLine()) != null) {
                response2.append(output);
            }
            in.close();
            log.info("Response: {}", response2.toString());*/

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public VNPayIPNReturnResponse handleIPNCallback(VNPayCallbackResponse response) {
        try {
            String RspCode = response.getVnp_ResponseCode();
            String vnp_SecureHash = response.getVnp_SecureHash();

            Map fields = new HashMap();

            fields.put("vnp_Amount", response.getVnp_Amount());
            fields.put("vnp_BankCode", response.getVnp_BankCode());
            fields.put("vnp_BankTranNo", response.getVnp_BankTranNo());
            fields.put("vnp_CardType", response.getVnp_CardType());
            fields.put("vnp_OrderInfo", response.getVnp_OrderInfo());
            fields.put("vnp_PayDate", response.getVnp_PayDate());
            fields.put("vnp_ResponseCode", response.getVnp_ResponseCode());
            fields.put("vnp_TmnCode", response.getVnp_TmnCode());
            fields.put("vnp_TransactionNo", response.getVnp_TransactionNo());
            fields.put("vnp_TransactionStatus", response.getVnp_TransactionStatus());
            fields.put("vnp_TxnRef", response.getVnp_TxnRef());

            String signingValue = HashUtility.hashAllFields(fields, vnp_HashSecret);

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String formattedDate = formatter.format(calendar.getTime());

            /*log.info("Received date: {}", formattedDate);

            log.info("vnp_SecureHash: {}", vnp_SecureHash);
            log.info("signingValue: {}", signingValue);*/

            if (signingValue.equals(vnp_SecureHash)) {
                boolean checkOrderId = vnPayPaymentRepository.existsByTransactionReference(response.getVnp_TxnRef()); // Giá trị của vnp_TxnRef tồn tại trong CSDL của merchant
                boolean checkOrderStatus = true; // Giả sử PaymentStatus = 0 (pending) là trạng thái thanh toán của giao dịch khởi tạo chưa có IPN.
                if (checkOrderId) {

                    VNPayPayment payment = vnPayPaymentRepository.findByTransactionReference(response.getVnp_TxnRef())
                            .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

                    // check if payment is already confirmed
                    if (payment.getReceivedAt() != null) {
                        return VNPayIPNReturnResponse.builder()
                                .RspCode("02")
                                .Message("Order already confirmed")
                                .build();
                    }

                    // Update payment received date status
                    payment.setReceivedAt(calendar.getTime().toInstant());

                    //Kiểm tra số tiền thanh toán do VNPAY phản hồi(vnp_Amount/100) với số tiền của đơn hàng merchant tạo thanh toán: giả sử số tiền kiểm tra là đúng.
                    boolean checkAmount = payment.getTotalPaymentAmount() == (float) (Long.parseLong(response.getVnp_Amount()) / 100);

                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(response.getVnp_ResponseCode())) {
                                //Xử lý/Cập nhật tình trạng giao dịch thanh toán "Thành công"
                                // out.print("GD Thanh cong");
                                payment.setBankTransactionNo(response.getVnp_BankTranNo());
                                payment.setTransactionNo(response.getVnp_TransactionNo());
                                payment.setResponseCode(response.getVnp_ResponseCode());
                                payment.setTransactionStatus(response.getVnp_TransactionStatus());
                                payment.setPaidAmount((float) (Long.parseLong(response.getVnp_Amount()) / 100));
                                payment = vnPayPaymentRepository.save(payment);

                                // Update payment status in course-service
                                if (payment.getPaymentCourses()!=null && !payment.getPaymentCourses().isEmpty()) {
                                    for (VNPayPaymentCourses paymentCourses : payment.getPaymentCourses()) {
                                        try {
                                            ApiResponse<UserCoursesResponse> userCoursesResponse = courseClient.enrollPaidCourse(
                                                    EnrollCourseRequest.builder()
                                                            .courseId(paymentCourses.getId().getCourseId())
                                                            .userUid(payment.getUserUid())
                                                            .build()
                                            );
                                            log.info("Enroll course response from course service: {}", userCoursesResponse);
                                        } catch (Exception e) {
                                            throw new AppException(ErrorCode.CANNOT_ENROLL_COURSE);
                                        }
                                    }
                                }

                                if (payment.getVnPayPaymentPremiumPackage()!=null) {
                                    VNPayPaymentPremiumPackage paymentPremiumPackage = payment.getVnPayPaymentPremiumPackage();
                                    paymentPremiumPackage.setStartDate(calendar.getTime().toInstant());
                                    vnPayPaymentPremiumPackageRepository.save(paymentPremiumPackage);
                                    log.info("Upgrade account successfully with premium package: {}", paymentPremiumPackage.getPackageType());
                                }
                                // Update payment status in course-service
                                /*VNPayPaymentCourses paymentCourses = vnPayPaymentCoursesRepository
                                        .findByPayment_paymentId(
                                                payment.getPaymentId()
                                        )
                                        .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

                                try {
                                    ApiResponse<UserCoursesResponse> userCoursesResponse = courseClient.enrollPaidCourse(
                                            EnrollCourseRequest.builder()
                                                    .courseId(paymentCourses.getId().getCourseId())
                                                    .userUid(payment.getUserUid())
                                                    .build()
                                    ).block();
                                    log.info("Enroll course response from course service: {}", userCoursesResponse);
                                } catch (Exception e) {
                                    throw new AppException(ErrorCode.CANNOT_ENROLL_COURSE);
                                }*/

                            } else {
                                //Xử lý/Cập nhật tình trạng giao dịch thanh toán "Không thành công"
                                //  out.print("GD Khong thanh cong");
                                payment.setBankTransactionNo(response.getVnp_BankTranNo());
                                payment.setTransactionNo(response.getVnp_TransactionNo());
                                payment.setResponseCode(response.getVnp_ResponseCode());
                                payment.setTransactionStatus("02"); // Transaction error
                                vnPayPaymentRepository.save(payment);
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

    @Transactional
    public VNPayRefundResponse refundPayment(
            String ipAddr, UUID paymentId
    ) {
        VNPayPayment payment = vnPayPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getTransactionStatus().equals("00") || payment.getPaidAmount() == 0.0f) {
            throw new AppException(ErrorCode.PAYMENT_NOT_SUCCESSFUL);
        }


        try {
            Calendar calender = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

            String vnp_RequestId = HashUtility.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "refund";
            String vnp_TxnRef = payment.getTransactionReference();
            String vnp_TransactionType = VNPayRefundType.FULL_REFUND.getCode();
            String vnp_Amount = String.valueOf(payment.getPaidAmount() * 100);
            String vnp_TransactionNo = payment.getBankTransactionNo();
            String vnp_OrderInfo = "Refund for order Id: " + vnp_TxnRef;
            String vnp_TransactionDate = formatter.format(
                    payment.getReceivedAt() != null
                            ?  Date.from(payment.getReceivedAt())
                            : Date.from(payment.getCreatedAt())
            );
            String vnp_CreateBy = "IntellabAdmin";


            String vnp_CreateDate = formatter.format(calender.getTime());

            String vnp_IpAddr = ipAddr;

            String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                    vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate,
                    vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
            String vnp_SecureHash = HashUtility.hmacSHA512(vnp_HashSecret, hash_Data.toString());

            VNPayRefundRequest request = VNPayRefundRequest
                    .builder()
                    .vnp_RequestId(vnp_RequestId)
                    .vnp_Version(vnp_Version)
                    .vnp_Command(vnp_Command)
                    .vnp_TmnCode(vnp_TmnCode)
                    .vnp_TransactionType(vnp_TransactionType)
                    .vnp_TxnRef(vnp_TxnRef)
                    .vnp_Amount(vnp_Amount)
                    .vnp_OrderInfo(vnp_OrderInfo)
                    .vnp_TransactionNo(vnp_TransactionNo)
                    .vnp_TransactionDate(vnp_TransactionDate)
                    .vnp_CreateBy(vnp_CreateBy)
                    .vnp_CreateDate(vnp_CreateDate)
                    .vnp_IpAddr(vnp_IpAddr)
                    .vnp_SecureHash(vnp_SecureHash)
                    .build();

            VNPayRefundResponse response = vnpayClient.refundPayment(request);

            response.setVnp_TransactionStatusDescription(
                    VNPayTransactionStatus
                            .getDescription(
                                    response.getVnp_TransactionStatus()
                            )
            );

            response.setVnp_ResponseCodeDescription(
                    VNPayRefundResponseCode
                            .getDescription(
                                    response.getVnp_ResponseCode()
                            )
            );

            if (response.getVnp_ResponseCode().equals("00")) {
                payment.setTransactionStatus("05"); // VNPAY has sent a refund request to the bank  (Refund transaction)
                vnPayPaymentRepository.save(payment);

                for (VNPayPaymentCourses paymentCourses : payment.getPaymentCourses()) {
                    ApiResponse<Boolean> result = courseClient.disenrollCourse(
                            DisenrollCourseRequest.builder()
                                    .courseId(paymentCourses.getId().getCourseId())
                                    .userUid(payment.getUserUid())
                                    .build()
                    ).block();

                    if (!result.getResult()) {
                        log.info("Cannot disenroll course: {}", paymentCourses.getId().getCourseId());
                    }
                }
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public VNPayDetailsPaymentResponse getPaymentDetailsByPaymentId(UUID paymentId) {
        VNPayPayment payment = vnPayPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        VNPayDetailsPaymentResponse response = vnPaymentMapper.toVNPayDetailsPaymentResponse(payment);

        response.setTransactionStatusDescription(
                VNPayTransactionStatus
                        .getDescription(
                                payment.getTransactionStatus()
                        )
        );

        response.setResponseCodeDescription(
                VNPayPayResponseCode
                        .getDescription(
                                payment.getResponseCode()
                        )
        );
        return response;
    }

    public Page<VNPayDetailsPaymentResponse> getListPaymentDetailsByUserUid(String userUid, Pageable pageable) {
        Page<VNPayPayment> payments = vnPayPaymentRepository.findAllByUserUid(userUid, pageable);
        return payments.map(vnPaymentMapper::toVNPayDetailsPaymentResponse);
    }

    public UUID getPaymentIdByTransactionReference(String transactionReference) {
        VNPayPayment payment = vnPayPaymentRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return payment.getPaymentId();
    }
}
