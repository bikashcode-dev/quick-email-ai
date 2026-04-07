package com.email.emailgen.service.auth;

import com.email.emailgen.config.AuthProperties;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpAuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final JavaMailSender javaMailSender;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;
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

        SimpleMailMessage message = new SimpleMailMessage();
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
            throw new IllegalStateException("OTP email could not be sent. Enable mail config or turn on log-only mode.", exception);
        }
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
