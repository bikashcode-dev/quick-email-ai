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
@Document(collection = "users")


public class UserDocument {

    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    private boolean isVerified;
    private String passwordHash;
    private Instant lastLoginAt;
    private Instant createdAt;
}
