package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/** TC-004: 未認証・トークン無効/期限切れ（401 / UNAUTHENTICATED）。 */
public class UnauthenticatedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthenticatedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED);
    }
}
