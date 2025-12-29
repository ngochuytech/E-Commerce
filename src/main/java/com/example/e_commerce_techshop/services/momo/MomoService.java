package com.example.e_commerce_techshop.services.momo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MomoService implements IMomoService {
    @Value("${momo.partner-code}")
    private String PARTNER_CODE;
    
    @Value("${momo.access-key}")
    private String ACCESS_KEY;

    @Value("${momo.secret-key}")
    private String SECRET_KEY;

    // URL redirect người dùng về frontend sau khi thanh toán xong
    @Value("${momo.return-url}")
    private String REDIRECT_URL;
    
    @Value("${momo.ipn-url}")
    private String IPN_URL;

    private static final String REQUEST_TYPE = "payWithMethod";
    
    // MoMo API Endpoints
    private static final String MOMO_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api";
    private static final String CREATE_PAYMENT_URL = MOMO_ENDPOINT + "/create";
    private static final String QUERY_STATUS_URL = MOMO_ENDPOINT + "/query";
    private static final String REFUND_URL = MOMO_ENDPOINT + "/refund";
    private static final String REFUND_QUERY_URL = MOMO_ENDPOINT + "/refund/query";

    @Override
    public String createPaymentRequest(String orderId, Long amount) {
        try {
            // Generate unique requestId for MoMo (khác với orderId của hệ thống)
            String requestId = PARTNER_CODE + "_" + orderId + "_" + new Date().getTime();
            // Sử dụng orderId từ hệ thống để liên kết với đơn hàng
            String orderInfo = "SN Mobile - Thanh toán đơn hàng #" + orderId;
            String extraData = "";

            // Generate raw signature theo đúng format của MoMo
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    ACCESS_KEY, amount, extraData, IPN_URL, orderId, orderInfo, PARTNER_CODE, REDIRECT_URL,
                    requestId, REQUEST_TYPE);

            // Sign with HMAC SHA256
            String signature = signHmacSHA256(rawSignature, SECRET_KEY);
            System.out.println("Generated Signature: " + signature);
            System.out.println("Raw Signature String: " + rawSignature);

            // Prepare request body theo đúng format của MoMo
            JSONObject requestBody = new JSONObject();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("accessKey", ACCESS_KEY);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount); // Phải là số, không phải string
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", REDIRECT_URL);
            requestBody.put("ipnUrl", IPN_URL);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", REQUEST_TYPE);
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");
            requestBody.put("autoCapture", true); // Bắt buộc với requestType = "payWithMethod"

            System.out.println("Request Body: " + requestBody.toString());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(CREATE_PAYMENT_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                System.out.println("Response from MoMo: " + result.toString());
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to create payment request: " + e.getMessage() + "\"}";
        }
    }

    // HMAC SHA256 signing method
    private static String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String checkPaymentStatus(String orderId) {
        try {
            // Generate requestId
            String requestId = PARTNER_CODE + new Date().getTime();

            // Generate raw signature for the status check
            String rawSignature = String.format(
                    "accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s",
                    ACCESS_KEY, orderId, PARTNER_CODE, requestId);

            // Sign with HMAC SHA256
            String signature = signHmacSHA256(rawSignature, SECRET_KEY);
            System.out.println("Generated Signature for Status Check: " + signature);

            // Prepare request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("accessKey", ACCESS_KEY);
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("signature", signature);
            requestBody.put("lang", "en");

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(QUERY_STATUS_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                System.out.println("Response from MoMo (Status Check): " + result.toString());
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to check payment status: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Hoàn tiền giao dịch MoMo (Partial hoặc Full Refund)
     * @param transId Mã giao dịch MoMo từ response thanh toán thành công
     * @param amount Số tiền hoàn (Min: 1,000 VND, Max: 50,000,000 VND)
     * @param description Mô tả lý do hoàn tiền
     * @return JSON response từ MoMo
     */
    @Override
    public String refundPayment(Long transId, Long amount, String description) {
        try {
            // Generate unique requestId and orderId for refund transaction
            String requestId = PARTNER_CODE + "_REFUND_" + new Date().getTime();
            String orderId = "RF_" + new Date().getTime(); // OrderId của giao dịch hoàn tiền (phải khác orderId gốc)

            // Generate raw signature theo format MoMo yêu cầu
            // Format: accessKey=$accessKey&amount=$amount&description=$description&orderId=$orderId
            //         &partnerCode=$partnerCode&requestId=$requestId&transId=$transId
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&description=%s&orderId=%s&partnerCode=%s&requestId=%s&transId=%s",
                    ACCESS_KEY, amount, description, orderId, PARTNER_CODE, requestId, transId);

            // Sign with HMAC SHA256
            String signature = signHmacSHA256(rawSignature, SECRET_KEY);
            System.out.println("Generated Signature for Refund: " + signature);
            System.out.println("Raw Signature: " + rawSignature);

            // Prepare request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("orderId", orderId);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("transId", transId);
            requestBody.put("lang", "vi");
            requestBody.put("description", description);
            requestBody.put("signature", signature);

            System.out.println("Refund Request Body: " + requestBody.toString());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(REFUND_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                System.out.println("Response from MoMo (Refund): " + result.toString());
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to refund payment: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Kiểm tra trạng thái hoàn tiền
     * @param orderId Mã đơn hàng của giao dịch hoàn tiền (RF_xxx)
     * @return JSON response từ MoMo
     */
    @Override
    public String checkRefundStatus(String orderId) {
        try {
            // Generate requestId
            String requestId = PARTNER_CODE + "_REFUND_QUERY_" + new Date().getTime();

            // Generate raw signature theo format MoMo yêu cầu
            // Format: accessKey=$accessKey&orderId=$orderId&partnerCode=$partnerCode&requestId=$requestId
            String rawSignature = String.format(
                    "accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s",
                    ACCESS_KEY, orderId, PARTNER_CODE, requestId);

            // Sign with HMAC SHA256
            String signature = signHmacSHA256(rawSignature, SECRET_KEY);
            System.out.println("Generated Signature for Refund Query: " + signature);

            // Prepare request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("lang", "vi");
            requestBody.put("signature", signature);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(REFUND_QUERY_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                System.out.println("Response from MoMo (Refund Query): " + result.toString());
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to check refund status: " + e.getMessage() + "\"}";
        }
    }

}
