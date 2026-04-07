package com.email.emailgen.controller;

import com.email.emailgen.dto.auth.AccountStatusRequest;
import com.email.emailgen.dto.auth.AccountStatusResponse;
import com.email.emailgen.dto.auth.AuthResponse;
import com.email.emailgen.dto.auth.MessageResponse;
import com.email.emailgen.dto.auth.PasswordLoginRequest;
import com.email.emailgen.dto.auth.SendOtpRequest;
import com.email.emailgen.dto.auth.SetPasswordRequest;
import com.email.emailgen.dto.auth.VerifyOtpRequest;
import com.email.emailgen.service.auth.OtpAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpAuthService otpAuthService;

    @PostMapping("/send-otp")
    public MessageResponse sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpAuthService.sendOtp(request.getEmail());
        return new MessageResponse("OTP sent successfully.");
    }

    @PostMapping("/account-status")
    public AccountStatusResponse accountStatus(@Valid @RequestBody AccountStatusRequest request) {
        return otpAuthService.getAccountStatus(request.getEmail());
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return otpAuthService.verifyOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody PasswordLoginRequest request) {
        return otpAuthService.loginWithPassword(request.getEmail(), request.getPassword());
    }

    @PostMapping("/set-password")
    public MessageResponse setPassword(@Valid @RequestBody SetPasswordRequest request, Authentication authentication) {
        return otpAuthService.setPassword(authentication.getName(), request.getPassword());
    }
}
