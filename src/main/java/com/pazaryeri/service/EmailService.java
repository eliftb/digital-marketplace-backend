package com.pazaryeri.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String fullName, String resetToken);
    void sendWelcomeEmail(String toEmail, String fullName);
    void sendProducerApprovalEmail(String toEmail, String storeName);
    void sendProducerRejectionEmail(String toEmail, String storeName, String reason);
    void sendOrderConfirmationEmail(String toEmail, String fullName, String orderNumber);
    void sendOrderStatusUpdateEmail(String toEmail, String fullName, String orderNumber, String newStatus);
}
