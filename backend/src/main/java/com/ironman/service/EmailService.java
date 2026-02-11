package com.ironman.service;

import com.ironman.dto.request.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        log.info("Sending simple email to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send HTML email with template
     */
    public void sendHtmlEmail(EmailRequest request) {
        log.info("Sending HTML email to: {}", request.getTo());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());

            // Process template with variables
            Context context = new Context();
            context.setVariables(request.getVariables());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process(request.getTemplateName(), context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", request.getTo());
        } catch (MessagingException e) {
            log.error("Error sending HTML email to {}: {}", request.getTo(), e.getMessage());
        }
    }

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmationEmail(String toEmail, String customerName,
                                           String orderNumber, BigDecimal totalAmount,
                                           String pickupDate) {
        log.info("Sending order confirmation email for order: {}", orderNumber);

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderNumber", orderNumber);
        variables.put("totalAmount", "₹" + totalAmount);
        variables.put("pickupDate", pickupDate);
        variables.put("year", LocalDateTime.now().getYear());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(toEmail)
                .subject("Order Confirmation - " + orderNumber)
                .templateName("email/order-confirmation")
                .variables(variables)
                .build();

        sendHtmlEmail(emailRequest);
    }

    /**
     * Send payment success email
     */
    public void sendPaymentSuccessEmail(String toEmail, String customerName,
                                        String orderNumber, BigDecimal amount) {
        log.info("Sending payment success email for order: {}", orderNumber);

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderNumber", orderNumber);
        variables.put("amount", "₹" + amount);
        variables.put("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        variables.put("year", LocalDateTime.now().getYear());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(toEmail)
                .subject("Payment Received - " + orderNumber)
                .templateName("email/payment-success")
                .variables(variables)
                .build();

        sendHtmlEmail(emailRequest);
    }

    /**
     * Send OTP email
     */
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("Sending OTP email to: {}", toEmail);

        Map<String, Object> variables = new HashMap<>();
        variables.put("otp", otp);
        variables.put("expiryMinutes", "10");
        variables.put("year", LocalDateTime.now().getYear());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(toEmail)
                .subject("Your OTP - " + appName)
                .templateName("email/otp-verification")
                .variables(variables)
                .build();

        sendHtmlEmail(emailRequest);
    }
}