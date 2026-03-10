package com.pazaryeri.service;

import com.pazaryeri.dto.request.PaymentRequest;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.PaymentResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponse processPayment(String userEmail, PaymentRequest request);
    PaymentResponse getPaymentByOrderId(Long orderId, String userEmail);
    PageResponse<PaymentResponse> getAllPayments(Pageable pageable);
    PaymentResponse refundPayment(Long paymentId, String adminEmail);
}
