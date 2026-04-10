package com.email.emailgen.service.auth;

import com.email.emailgen.config.AuthProperties;
import com.email.emailgen.config.ResendProperties;
import com.email.emailgen.dto.auth.AccountStatusResponse;
import com.email.emailgen.dto.auth.AuthResponse;
import com.email.emailgen.dto.auth.MessageResponse;
import com.email.emailgen.model.OtpDocument;
import com.email.emailgen.model.UserDocument;
import com.email.emailgen.repository.OtpRepository;
import com.email.emailgen.repository.UserRepository;
import com.email.emailgen.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpAuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final JavaMailSender javaMailSender;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final ResendProperties resendProperties;
    private final WebClient.Builder webClientBuilder;
    private final PasswordEncoder passwordEncoder;
    @Value("${spring.mail.username:}")
    private String mailUsername;
    @Value("${spring.mail.password:}")
    private String mailPassword;
    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;
    private final SecureRandom secureRandom = new SecureRandom();

    public void sendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        invalidateExistingOtps(normalizedEmail);

        OtpDocument otpDocument = OtpDocument.builder()
                .email(normalizedEmail)
                .otp(generateOtp())
                .expiryTime(Instant.now().plus(authProperties.getOtp().getExpiry()))
                .isUsed(false)
                .createdAt(Instant.now())
                .build();

        otpRepository.save(otpDocument);
        sendOtpEmail(otpDocument);
    }

    public AuthResponse verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        OtpDocument otpDocument = otpRepository.findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found. Please request a new OTP."));

        if (!otpDocument.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Incorrect OTP. Please check and try again.");
        }

        if (otpDocument.isUsed()) {
            throw new IllegalArgumentException("OTP has already been used. Please request a new OTP.");
        }

        if (otpDocument.getExpiryTime().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        otpDocument.setUsed(true);
        otpRepository.save(otpDocument);

        UserDocument user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> createUserIfMissing(normalizedEmail));

        user.setVerified(true);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(normalizedEmail);
        return AuthResponse.builder()
                .token(token)
                .email(normalizedEmail)
                .verified(true)
                .message("OTP verified successfully.")
                .build();
    }

    public AccountStatusResponse getAccountStatus(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .map(user -> AccountStatusResponse.builder()
                        .exists(true)
                        .verified(user.isVerified())
                        .hasPassword(user.getPasswordHash() != null && !user.getPasswordHash().isBlank())
                        .build())
                .orElseGet(() -> AccountStatusResponse.builder()
                        .exists(false)
                        .verified(false)
                        .hasPassword(false)
                        .build());
    }

    public AuthResponse loginWithPassword(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        UserDocument user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Account not found. Please verify with OTP first."));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("Please verify your email with OTP first.");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password is not set yet. Sign in with OTP first.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(normalizedEmail);
        return AuthResponse.builder()
                .token(token)
                .email(normalizedEmail)
                .verified(true)
                .message("Login successful.")
                .build();
    }

    public MessageResponse setPassword(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        UserDocument user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("Please verify your email first.");
        }

        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        return new MessageResponse("Password saved successfully.");
    }

    private void invalidateExistingOtps(String email) {
        List<OtpDocument> activeOtps = otpRepository.findByEmailAndIsUsedFalse(email);
        activeOtps.forEach(otp -> otp.setUsed(true));
        if (!activeOtps.isEmpty()) {
            otpRepository.saveAll(activeOtps);
        }
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private void sendOtpEmail(OtpDocument otpDocument) {
        if (authProperties.getMail().isLogOnly()) {
            log.warn("QuickMail local OTP for {} is {} (expires in {} minutes)",
                    otpDocument.getEmail(),
                    otpDocument.getOtp(),
                    authProperties.getOtp().getExpiry().toMinutes());
            return;
        }

        if (resendProperties.canSend()) {
            sendOtpEmailWithResend(otpDocument);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(mailUsername)) {
            message.setFrom(mailUsername);
            message.setReplyTo(mailUsername);
        }
        message.setTo(otpDocument.getEmail());
        message.setSubject(authProperties.getMail().getSubject());
        message.setText("""
                Your QuickMail OTP is: %s

                This OTP will expire in %d minutes.

                If you did not request this OTP, you can safely ignore this email.
                """.formatted(otpDocument
                .getOtp(), authProperties.getOtp().getExpiry().toMinutes()));
        try {
            javaMailSender.send(message);
        } catch (MailException exception) {
            log.warn("SMTP OTP email send failed for {} using sender {} via {}: {}",
                    otpDocument.getEmail(),
                    StringUtils.hasText(mailUsername) ? mailUsername : "<empty>",
                    mailHost,
                    exception.getMessage());

            if (canUseGmailSslFallback()) {
                try {
                    buildSslFallbackSender().send(message);
                    log.info("OTP email sent successfully for {} using Gmail SSL fallback on port 465.", otpDocument.getEmail());
                    return;
                } catch (MailException fallbackException) {
                    otpRepository.delete(otpDocument);
                    log.error("Failed to send OTP email to {} using sender {}. SMTP 587 error: {}. SSL 465 fallback error: {}",
                            otpDocument.getEmail(),
                            StringUtils.hasText(mailUsername) ? mailUsername : "<empty>",
                            exception.getMessage(),
                            fallbackException.getMessage(),
                            fallbackException);
                    throw new IllegalStateException("OTP email could not be sent. Please check the mail setup and try again.", fallbackException);
                }
            }

            otpRepository.delete(otpDocument);
            log.error("Failed to send OTP email to {} using sender {} after SMTP attempt",
                    otpDocument.getEmail(),
                    StringUtils.hasText(mailUsername) ? mailUsername : "<empty>", exception);
            throw new IllegalStateException("OTP email could not be sent. Please check the mail setup and try again.", exception);
        }
    }

    private void sendOtpEmailWithResend(OtpDocument otpDocument) {
        Map<String, Object> payload = Map.of(
                "from", resendProperties.getFromEmail(),
                "to", List.of(otpDocument.getEmail()),
                "subject", authProperties.getMail().getSubject(),
                "html", """
                        <p>Your QuickMail OTP is: <strong>%s</strong></p>
                        <p>This OTP will expire in %d minutes.</p>
                        <p>If you did not request this OTP, you can safely ignore this email.</p>
                        """.formatted(otpDocument.getOtp(), authProperties.getOtp().getExpiry().toMinutes())
        );

        try {
            webClientBuilder
                    .baseUrl(resendProperties.getApiUrl())
                    .build()
                    .post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + resendProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block(resendProperties.getTimeout());
            log.info("OTP email sent successfully for {} using Resend API.", otpDocument.getEmail());
        } catch (WebClientResponseException exception) {
            otpRepository.delete(otpDocument);
            log.error("Resend API rejected OTP email for {} with status {} and body {}",
                    otpDocument.getEmail(),
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception);
            throw new IllegalStateException("OTP email could not be sent. Resend rejected the request.", exception);
        } catch (Exception exception) {
            otpRepository.delete(otpDocument);
            log.error("Failed to send OTP email to {} using Resend API: {}",
                    otpDocument.getEmail(),
                    exception.getMessage(),
                    exception);
            throw new IllegalStateException("OTP email could not be sent. Please check the mail setup and try again.", exception);
        }
    }

    private boolean canUseGmailSslFallback() {
        return StringUtils.hasText(mailUsername)
                && StringUtils.hasText(mailPassword)
                && "smtp.gmail.com".equalsIgnoreCase(mailHost);
    }

    private JavaMailSender buildSslFallbackSender() {
        JavaMailSenderImpl fallbackSender = new JavaMailSenderImpl();
        fallbackSender.setHost(mailHost);
        fallbackSender.setPort(465);
        fallbackSender.setUsername(mailUsername);
        fallbackSender.setPassword(mailPassword);
        fallbackSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties properties = fallbackSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.starttls.enable", "false");
        properties.put("mail.smtp.connectiontimeout", "15000");
        properties.put("mail.smtp.timeout", "15000");
        properties.put("mail.smtp.writetimeout", "15000");
        properties.put("mail.smtp.ssl.trust", mailHost);
        properties.put("mail.debug", "false");
        return fallbackSender;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private UserDocument createUserIfMissing(String email) {
        try {
            return userRepository.save(UserDocument.builder()
                    .email(email)
                    .isVerified(false)
                    .createdAt(Instant.now())
                    .build());
        } catch (DuplicateKeyException exception) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> exception);
        }
    }
}
