package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/** TC-002: ログイン ID/パスワード誤り（401 / LOGIN_FAILED、MSG-015）。 */
public class AuthenticationFailedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationFailedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ErrorCode.LOGIN_FAILED);
    }
}
