package com.email.emailgen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otp")
public class OtpDocument {
    @Id
    private String id;
    @Indexed
    private String email;
    private String otp;
    private Instant expiryTime;
    private boolean isUsed;
    private Instant createdAt;
}
