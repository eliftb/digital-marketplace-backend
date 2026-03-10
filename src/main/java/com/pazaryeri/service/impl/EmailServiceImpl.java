package com.pazaryeri.service.impl;

import com.pazaryeri.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String subject = "Şifre Sıfırlama Talebi - Dijital Pazar Yeri";
        String body = buildHtmlEmail(
            "Şifre Sıfırlama",
            "Merhaba " + fullName + ",",
            "Şifrenizi sıfırlamak için aşağıdaki butona tıklayın. Bu bağlantı <strong>2 saat</strong> geçerlidir.",
            resetUrl,
            "Şifremi Sıfırla",
            "Eğer bu talebi siz yapmadıysanız bu e-postayı görmezden gelebilirsiniz."
        );
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String subject = "Hoş Geldiniz! - Dijital Pazar Yeri";
        String body = buildHtmlEmail(
            "Hoş Geldiniz!",
            "Merhaba " + fullName + ",",
            "Dijital Pazar Yeri'ne başarıyla kayıt oldunuz. Yerel üreticilerin ürünlerini keşfetmeye başlayabilirsiniz.",
            frontendUrl,
            "Alışverişe Başla",
            ""
        );
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    @Override
    public void sendProducerApprovalEmail(String toEmail, String storeName) {
        String subject = "Mağazanız Onaylandı! - Dijital Pazar Yeri";
        String body = buildHtmlEmail(
            "Mağazanız Onaylandı 🎉",
            "Tebrikler!",
            "<strong>" + storeName + "</strong> mağazanız yöneticilerimiz tarafından onaylandı. Artık ürün ekleyebilir ve satışa başlayabilirsiniz.",
            frontendUrl + "/producer/dashboard",
            "Mağazama Git",
            ""
        );
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    @Override
    public void sendProducerRejectionEmail(String toEmail, String storeName, String reason) {
        String subject = "Mağaza Başvurunuz Hakkında - Dijital Pazar Yeri";
        String body = buildHtmlEmail(
            "Başvuru Değerlendirmesi",
            "Sayın Başvuru Sahibi,",
            "<strong>" + storeName + "</strong> mağaza başvurunuz şu an için onaylanamamıştır.<br><br>"
            + "<strong>Sebep:</strong> " + reason + "<br><br>"
            + "Eksikliklerinizi giderdikten sonra tekrar başvurabilirsiniz.",
            frontendUrl + "/producer/register",
            "Tekrar Başvur",
            ""
        );
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    @Override
    public void sendOrderConfirmationEmail(String toEmail, String fullName, String orderNumber) {
        String subject = "Sipariş Onayı #" + orderNumber + " - Dijital Pazar Yeri";
        String body = buildHtmlEmail(
            "Siparişiniz Alındı!",
            "Merhaba " + fullName + ",",
            "<strong>#" + orderNumber + "</strong> numaralı siparişiniz başarıyla oluşturuldu. Siparişinizin durumunu takip edebilirsiniz.",
            frontendUrl + "/orders/" + orderNumber,
            "Siparişi Takip Et",
            ""
        );
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    @Override
    public void sendOrderStatusUpdateEmail(String toEmail, String fullName, String orderNumber, String newStatus) {
        String statusTr = translateStatus(newStatus);
        String subject = "Sipariş Durumu Güncellendi #" + orderNumber;
        String body = buildHtmlEmail(
            "Sipariş Durumu Güncellendi",
            "Merhaba " + fullName + ",",
            "<strong>#" + orderNumber + "</strong> numaralı siparişinizin durumu <strong>" + statusTr + "</strong> olarak güncellendi.",
            frontendUrl + "/orders/" + orderNumber,
            "Siparişi Görüntüle",
            ""
        );
        sendHtmlEmail(toEmail, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("E-posta gönderildi: {} -> {}", subject, to);
        } catch (Exception e) {
            log.error("E-posta gönderilemedi: {} -> {} | Hata: {}", subject, to, e.getMessage());
        }
    }

    private String buildHtmlEmail(String title, String greeting, String body,
                                   String actionUrl, String actionText, String footer) {
        return """
            <!DOCTYPE html>
            <html lang="tr">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 30px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <!-- Header -->
                    <tr><td style="background-color: #2e7d32; padding: 24px 32px;">
                      <h1 style="color: #ffffff; margin: 0; font-size: 20px;">🌿 Dijital Pazar Yeri</h1>
                    </td></tr>
                    <!-- Content -->
                    <tr><td style="padding: 32px;">
                      <h2 style="color: #2e7d32; margin-top: 0;">%s</h2>
                      <p style="color: #333; font-size: 15px;">%s</p>
                      <p style="color: #555; font-size: 14px; line-height: 1.6;">%s</p>
                      %s
                      %s
                    </td></tr>
                    <!-- Footer -->
                    <tr><td style="background-color: #f9f9f9; padding: 16px 32px; text-align: center;">
                      <p style="color: #999; font-size: 12px; margin: 0;">© 2025 Dijital Pazar Yeri. Tüm hakları saklıdır.</p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body></html>
            """.formatted(
                title, greeting, body,
                actionUrl.isEmpty() ? "" :
                    "<div style=\"text-align: center; margin: 24px 0;\">" +
                    "<a href=\"" + actionUrl + "\" style=\"background-color: #2e7d32; color: #fff; padding: 12px 28px; " +
                    "text-decoration: none; border-radius: 4px; font-size: 15px; display: inline-block;\">" +
                    actionText + "</a></div>",
                footer.isEmpty() ? "" : "<p style=\"color: #999; font-size: 12px; margin-top: 16px;\">" + footer + "</p>"
            );
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Beklemede";
            case "CONFIRMED" -> "Onaylandı";
            case "PREPARING" -> "Hazırlanıyor";
            case "SHIPPED" -> "Kargoya Verildi";
            case "DELIVERED" -> "Teslim Edildi";
            case "CANCELLED" -> "İptal Edildi";
            case "REFUNDED" -> "İade Edildi";
            default -> status;
        };
    }
}
