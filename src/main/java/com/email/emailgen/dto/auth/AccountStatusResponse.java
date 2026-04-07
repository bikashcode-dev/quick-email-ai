package com.email.emailgen.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountStatusResponse {
    boolean exists;
    boolean verified;
    boolean hasPassword;
}
