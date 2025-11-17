package com.example.e_commerce_techshop.services.vnpay;

import java.io.IOException;

import com.example.e_commerce_techshop.dtos.vnpay.PaymentDTO;
import com.example.e_commerce_techshop.dtos.vnpay.PaymentQueryDTO;
import com.example.e_commerce_techshop.dtos.vnpay.PaymentRefundDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface IVNPayService {
    String createPaymentUrl(PaymentDTO paymentRequest, HttpServletRequest request);
    String queryTransaction(PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) throws IOException;
    String refundTransaction(PaymentRefundDTO refundDTO) throws IOException;
}
