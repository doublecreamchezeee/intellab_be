package com.example.identityservice.service;


import com.example.identityservice.client.CourseClient;
import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.client.VNPayClient;
import com.example.identityservice.constant.VNPayTransactionStatus;
import com.example.identityservice.constant.response.vnpay.VNPayPayResponseCode;
import com.example.identityservice.constant.response.vnpay.VNPayQueryResponseCode;
import com.example.identityservice.constant.response.vnpay.VNPayRefundResponseCode;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.course.DisenrollCourseRequest;
import com.example.identityservice.dto.request.course.DisenrollCoursesEnrolledUsingSubscriptionPlanRequest;
import com.example.identityservice.dto.request.course.EnrollCourseRequest;
import com.example.identityservice.dto.request.course.ReEnrollCoursesUsingSubscriptionPlanRequest;
import com.example.identityservice.dto.request.profile.DetailsDiscountPercentResponse;
import com.example.identityservice.dto.request.vnpay.VNPayQueryRequest;
import com.example.identityservice.dto.request.vnpay.VNPayRefundRequest;
import com.example.identityservice.dto.request.vnpay.VNPaySinglePaymentCreationRequest;
import com.example.identityservice.dto.request.vnpay.VNPayUpgradeAccountRequest;
import com.example.identityservice.dto.response.course.CourseAndFirstLessonResponse;
import com.example.identityservice.dto.response.course.DetailCourseResponse;
import com.example.identityservice.dto.response.userCourse.UserCoursesResponse;
import com.example.identityservice.dto.response.vnpay.*;
import com.example.identityservice.enums.account.*;
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
import com.example.identityservice.specification.VNPayPaymentSpecification;
import com.example.identityservice.utility.HashUtility;
import com.example.identityservice.utility.ParseUUID;
import com.example.identityservice.utility.StringUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
    private final FirebaseAuthClient firebaseAuthClient;

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
            String orderDescription = StringUtility.convertToAscii(
                        course.getCourseName()
                ); // null;

            /*if (request.getLanguage().getCode().equals("vi")) {
                orderDescription = StringUtility.convertToAscii(course.getCourseName());
            } else {
                orderDescription = "Payment for courses ";
            }*/

            // Create payment url
            VNPayPaymentUrlResponse paymentUrlResponse =  createPaymentUrl(
                    ipAddr,
                    (long) course.getPrice(),
                    request.getVNPayBankCode().getCode(),
                    request.getVNPayCurrencyCode().getCode(),
                    request.getLanguage().getCode(),
                    orderDescription,
                    userUid
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
                    .orderDescription(orderDescription)
                    .paymentFor(PaymentFor.COURSE.getCode())
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
            if (request.getIsChangePlan()==null || !request.getIsChangePlan()) {
                vnPayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                        userUid,
                        PremiumPackageStatus.ACTIVE.getCode()
                ).ifPresent(premiumPackage -> {
                            throw new AppException(ErrorCode.USER_ALREADY_HAS_SUBSCRIPTION);
                        }
                );
            }

            Long discount = 0L;
            int previousPlanDuration = 0;

            if (request.getIsChangePlan()) {
                VNPayPaymentPremiumPackage previousPackage = vnPayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                        userUid,
                        PremiumPackageStatus.ACTIVE.getCode()
                ).orElse(null);

                if (previousPackage != null) {
                    previousPlanDuration = previousPackage.getDuration();

                    long currentPlanPrice = switch (previousPackage.getPackageType()) {
                        case "PREMIUM_PLAN" -> PremiumPackage.PREMIUM_PLAN.getPrice();
                        case "COURSE_PLAN" -> PremiumPackage.COURSE_PLAN.getPrice();
                        case "ALGORITHM_PLAN" -> PremiumPackage.ALGORITHM_PLAN.getPrice();
                        default -> throw new AppException(ErrorCode.SUBSCRIPTION_PLAN_NOT_EXISTED);
                    };

                    if (previousPlanDuration == PremiumDuration.MONTHLY_PACKAGE.getDuration()) {
                        currentPlanPrice = currentPlanPrice + 1000L;
                    }

                    long currentTime = System.currentTimeMillis();
                    long startTime = previousPackage.getStartDate().toEpochMilli();
                    long usedTimeInDays = (long) Math.ceil((currentTime - startTime) / (1000.0 * 60 * 60 * 24));

                    int currentPlanDuration = previousPackage.getDuration();

                    if (usedTimeInDays < Math.ceil(currentPlanDuration * 0.25)) {
                        log.info("Discount 70% for user {} with plan {}", userUid, previousPackage.getPackageType());
                        discount = (long) Math.ceil(currentPlanPrice * 0.7);
                    } else if (usedTimeInDays < Math.ceil(currentPlanDuration * 0.5)) {
                        log.info("Discount 50% for user {} with plan {}", userUid, previousPackage.getPackageType());
                        discount = (long) Math.ceil(currentPlanPrice * 0.5);
                    } else if (usedTimeInDays < Math.ceil(currentPlanDuration * 0.75)) {
                        log.info("Discount 30% for user {} with plan {}", userUid, previousPackage.getPackageType());
                        discount = (long) Math.ceil(currentPlanPrice * 0.2);
                    } else {
                        discount = 0L;
                    }
                }

            }


            // create description for order
            String orderDescription = request.getPremiumPackage().getName(); // null;

            /*if (request.getLanguage().getCode().equals("vi")) {
                orderDescription = "Nang cap tai khoan " ;
            } else {
                orderDescription = "Payment for upgrade account ";
            }*/

            //orderDescription += request.getPremiumPackage().getCode() + " package ";

            Long price =
                    Objects.equals(
                            request.getPremiumDuration().getDuration(),
                            PremiumDuration.MONTHLY_PACKAGE.getDuration()
                    )

                ? request.getPremiumPackage().getPrice()
                : (long) ((request.getPremiumPackage().getPrice()-100000L) * 12);   // cost only 11 months for yearly package

            int addingDuration = 0;

            if (discount > 0 && previousPlanDuration > 0) {
                if (request.getPremiumDuration().getDuration().equals(previousPlanDuration)
                        || request.getPremiumDuration().getDuration() > previousPlanDuration
                ) {
                    price -= discount;
                } else if (request.getPremiumDuration().getDuration() < previousPlanDuration) {
                    throw new AppException(ErrorCode.CANNOT_CHANGE_PLAN);
                    /*if (price - discount > 0) {
                        price -= discount;
                    } else {
                        addingDuration = (int) Math.floor((discount - price) / );
                        price = 0L;
                    }*/
                }
            }

            if (price <= 0) {
                log.error("Price is negative, some cases were missing: {}", price);
                //price = 0L;
                throw new AppException(ErrorCode.PRICE_IS_NOT_VALID);
            }

            // Create payment url
            VNPayPaymentUrlResponse paymentUrlResponse =  createPaymentUrl(
                    ipAddr,
                    price,
                    request.getVNPayBankCode().getCode(),
                    request.getVNPayCurrencyCode().getCode(),
                    request.getLanguage().getCode(),
                    orderDescription,
                    userUid
            );

            // Save payment of premium package to database
            VNPayPaymentPremiumPackage paymentPremiumPackage = VNPayPaymentPremiumPackage.builder()
                    .userUid(userUid)
                    .userUuid(ParseUUID.normalizeUID(userUid))
                    //.payment(payment)
                    .packageType(request.getPremiumPackage().getCode())
                    .startDate(null)
                    .endDate(null)
                    .status("Pending transaction") // 01: Pending transaction
                    .duration(
                            request.getPremiumDuration().getCode()
                                    .equals(
                                            PremiumDuration.MONTHLY_PACKAGE
                                                    .getCode()
                                    )
                                    ? 30
                                    : 365
                    )
                    .build();

            paymentPremiumPackage = vnPayPaymentPremiumPackageRepository.save(paymentPremiumPackage);

            // save payment to database
            VNPayPayment payment = VNPayPayment.builder()
                    .userUid(userUid)
                    .userUuid(ParseUUID.normalizeUID(userUid))
                    .transactionStatus("01") // Pending transaction
                    .totalPaymentAmount(Float.valueOf(price))
                    .currency(request.getVNPayCurrencyCode().getCode())
                    .paidAmount(0.0f)
                    .bankCode(request.getVNPayBankCode().getCode())
                    .transactionReference(paymentUrlResponse.getTransactionReference())
                    .createdAt(paymentUrlResponse.getCurrentDate().toInstant())
                    .vnPayPaymentPremiumPackage(paymentPremiumPackage)
                    .orderDescription(orderDescription)
                    .paymentFor(PaymentFor.SUBSCRIPTION.getCode())
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

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT);
        }
    }

    public VNPayPaymentUrlResponse createPaymentUrl(
            String ipAddr, long amount,
            String bankCode, String currCode,
            String locale, String orderDescription,
            String userUid
    ) {
        Boolean isEmailVerified = firebaseAuthClient
                .isEmailVerifiedByUserUid(userUid);

        if (!isEmailVerified) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        String vnp_TxnRef = HashUtility.getRandomNumber(8);
        String orderInfo = orderDescription != null && !orderDescription.isEmpty()
                ? orderDescription
                : "Payment for something";

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
                                            ).block();
                                            log.info("Enroll course response from course service: {}", userCoursesResponse);
                                        } catch (Exception e) {
                                            throw new AppException(ErrorCode.CANNOT_ENROLL_COURSE);
                                        }
                                    }
                                }

                                if (payment.getVnPayPaymentPremiumPackage()!=null) {
                                    // if user has already had a subscription, then set the current subscription to inactive

                                    List<VNPayPaymentPremiumPackage> premiumPackages = vnPayPaymentPremiumPackageRepository.findAllByUserUidAndStatus(
                                            payment.getUserUid(),
                                            PremiumPackageStatus.ACTIVE.getCode()
                                    );

                                    premiumPackages.forEach(premiumPackage -> {
                                        premiumPackage.setStatus(PremiumPackageStatus.INACTIVE.getCode());
                                        vnPayPaymentPremiumPackageRepository.save(premiumPackage);
                                    });

                                    VNPayPaymentPremiumPackage paymentPremiumPackage = payment.getVnPayPaymentPremiumPackage();
                                    paymentPremiumPackage.setStartDate(calendar.getTime().toInstant());
                                    paymentPremiumPackage.setEndDate(calendar.getTime().toInstant().plusSeconds(paymentPremiumPackage.getDuration() * 24 * 60 * 60));
                                    paymentPremiumPackage.setStatus(PremiumPackageStatus.ACTIVE.getCode());
                                    vnPayPaymentPremiumPackageRepository.save(paymentPremiumPackage);
                                    log.info("Upgrade account successfully with premium package: {}", paymentPremiumPackage.getPackageType());

                                    if (paymentPremiumPackage.getPackageType().equals(PremiumPackage.ALGORITHM_PLAN.getCode())
                                    ) {
                                        boolean changeCourseOrPremiumPlanToProblemPlan = false;

                                        for (VNPayPaymentPremiumPackage premiumPackage : premiumPackages) {
                                           if (premiumPackage.getPackageType().equals(PremiumPackage.COURSE_PLAN.getCode())
                                                    || premiumPackage.getPackageType().equals(PremiumPackage.PREMIUM_PLAN.getCode())
                                           ) {
                                               changeCourseOrPremiumPlanToProblemPlan = true;
                                               break;
                                           }
                                        }

                                        if (changeCourseOrPremiumPlanToProblemPlan) {
                                            disenrollCourses(List.of(payment.getUserUuid()));
                                        }
                                    }

                                    if (paymentPremiumPackage.getPackageType().equals(PremiumPackage.COURSE_PLAN.getCode())
                                            || paymentPremiumPackage.getPackageType().equals(PremiumPackage.PREMIUM_PLAN.getCode())
                                    ) {
                                        try {
                                            courseClient.reEnrollCoursesEnrolledUsingSubscriptionPlan(
                                                    ReEnrollCoursesUsingSubscriptionPlanRequest
                                                            .builder()
                                                            .userUuid(payment.getUserUuid())
                                                            .subscriptionPlan(paymentPremiumPackage.getPackageType())
                                                            .build()
                                            ).block();
                                        } catch (Exception e) {
                                            log.error("Error calling course service while re-enroll courses for users", e);
                                        }
                                    }

                                }
                                // Update payment status in course-service

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

    private void disenrollCourses(List<UUID> uuids) {
        try {
            courseClient.disenrollCoursesEnrolledUsingSubscriptionPlan(
                    DisenrollCoursesEnrolledUsingSubscriptionPlanRequest
                            .builder()
                            .listUserUuid(uuids)
                            .build()
            ).block();
        } catch (Exception e) {
            log.error("Error calling course service while disenrolling courses for users with expired premium packages", e);
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
    public VNPayDetailsPaymentForCourseResponse getPaymentDetailsByPaymentId(UUID paymentId) {
        VNPayPayment payment = vnPayPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        VNPayDetailsPaymentForCourseResponse response = vnPaymentMapper.toVNPayDetailsPaymentForCourseResponse(payment);

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

    public Page<VNPayDetailsPaymentForCourseResponse> getListDetailsPaymentForCourseByUserUid(String userUid, Pageable pageable, PaymentFor paymentFor) {
        Specification<VNPayPayment> specification = VNPayPaymentSpecification.hasPaymentFor(paymentFor.getCode())
                .and(VNPayPaymentSpecification.hasUserUid(userUid));

        Page<VNPayPayment> payments = vnPayPaymentRepository.findAll(specification, pageable);
        return payments.map(vnPaymentMapper::toVNPayDetailsPaymentForCourseResponse);
    }

    public UUID getPaymentIdByTransactionReference(String transactionReference) {
        VNPayPayment payment = vnPayPaymentRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return payment.getPaymentId();
    }

    public CourseAndFirstLessonResponse getCourseAndFirstLessonByPaymentId(UUID paymentId) {
        VNPayPayment payment = vnPayPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentCourses() == null || payment.getPaymentCourses().isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_NOT_FOR_COURSE);
        }

        CourseAndFirstLessonResponse courseAndFirstLessonResponse = null;

        try {
            courseAndFirstLessonResponse = Objects.requireNonNull(courseClient.getCourseAndFirstLessonByCourseId(
                    payment.getPaymentCourses().get(0).getId().getCourseId() // first course in cart
            ).block()).getResult();
        } catch (Exception e) {
            throw new AppException(ErrorCode.SERVER_CANNOT_GET_COURSE);
        }

        if (courseAndFirstLessonResponse == null) {
            throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
        }

        return courseAndFirstLessonResponse;
    }

    public Boolean setSubscriptionPlanEndDateToOverdue(String userUid) {
        vnPayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                userUid,
                PremiumPackageStatus.ACTIVE.getCode()
        ).ifPresent(premiumPackage -> {
            Instant lastDayMidnight = LocalDate.now()
                    .minusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();

            premiumPackage.setEndDate(lastDayMidnight);
            vnPayPaymentPremiumPackageRepository.save(premiumPackage);
        });

        return true;
    }

    public Boolean setSubscriptionPlanStartDate(String userUid, PremiumPackageDiscountPercentByTime discountPercentByTime) {
        vnPayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                userUid,
                PremiumPackageStatus.ACTIVE.getCode()
        ).ifPresent(premiumPackage -> {

            long daysToSubtract = (long) Math.ceil(premiumPackage.getDuration() * discountPercentByTime.getDurationPercent());
            log.info("Days to subtract: {}", daysToSubtract);

            // Set start date to 5 minutes before the current time
            Instant newStartDate = Instant.now()
                    .minus(Duration.ofDays(daysToSubtract))
                    .plus(Duration.ofDays(1L))
                    .plusSeconds(300L)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            premiumPackage.setStartDate(newStartDate);
            vnPayPaymentPremiumPackageRepository.save(premiumPackage);
        });

        return true;
    }

    public DetailsDiscountPercentResponse getDiscountPercentByUserUid(
            String userUid
    ) {
        DetailsDiscountPercentResponse response = new DetailsDiscountPercentResponse();

        vnPayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                userUid,
                PremiumPackageStatus.ACTIVE.getCode()
        ).ifPresent(premiumPackage -> {
            long currentPlanPrice = switch (premiumPackage.getPackageType()) {
                case "PREMIUM_PLAN" -> PremiumPackage.PREMIUM_PLAN.getPrice();
                case "COURSE_PLAN" -> PremiumPackage.COURSE_PLAN.getPrice();
                case "ALGORITHM_PLAN" -> PremiumPackage.ALGORITHM_PLAN.getPrice();
                default -> throw new AppException(ErrorCode.SUBSCRIPTION_PLAN_NOT_EXISTED);
            };

            if (Objects.equals(premiumPackage.getDuration(), PremiumDuration.MONTHLY_PACKAGE.getDuration())) {
                currentPlanPrice = currentPlanPrice + 1000L;
            }

            long currentTime = System.currentTimeMillis();
            long startTime = premiumPackage.getStartDate().toEpochMilli();
            long usedTimeInDays = (long) Math.ceil((currentTime - startTime) / (1000.0 * 60 * 60 * 24));
            log.info("Used time in days: {}", usedTimeInDays);

            int currentPlanDuration = premiumPackage.getDuration();

            if (usedTimeInDays < Math.ceil(currentPlanDuration * 0.25)) {
                response.setDiscountPercent(0.7f);
                response.setDiscountValue(
                        (long) Math.ceil(currentPlanPrice * 0.7)
                );
            } else if (usedTimeInDays < Math.ceil(currentPlanDuration * 0.5)) {
                response.setDiscountPercent(0.5f);
                response.setDiscountValue(
                        (long) Math.ceil(currentPlanPrice * 0.5)
                );
            } else if (usedTimeInDays < Math.ceil(currentPlanDuration * 0.75)) {

                response.setDiscountPercent(0.2f);
                response.setDiscountValue(
                        (long) Math.ceil(currentPlanPrice * 0.2)
                );
            } else {
                response.setDiscountPercent(0.0f);
                response.setDiscountValue(0L);
            }
        });

        return response;
    }

}
