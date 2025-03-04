package com.example.courseservice.service;

import com.example.courseservice.utils.HashUtility;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import com.google.gson.JsonObject;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class VNPayService {
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

    public String createPaymentUrl(long amount, String bankCode,String ipAddr, String currCode) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        String vnp_TxnRef = HashUtility.getRandomNumber(8);
        String orderInfo = "Payment for course: " + vnp_TxnRef;

        try {
            Map<String, String> vnpParams = new HashMap<>();

            vnpParams.put("vnp_Version", vnp_Version);
            vnpParams.put("vnp_Command", vnp_Command);
            vnpParams.put("vnp_TmnCode", vnp_TmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(amount * 100000)); // Convert VND to smallest unit
            vnpParams.put("vnp_CurrCode", currCode);
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

            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParams.put("vnp_BankCode", bankCode);
            }

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

            return vnp_PayUrl + "?" + query.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay payment URL", e);
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

}
