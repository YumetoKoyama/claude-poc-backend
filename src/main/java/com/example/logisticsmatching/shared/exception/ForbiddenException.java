package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/**
 * TC-005（権限境界）: 自テナント内の権限不足（403 / FORBIDDEN、MSG-023）。
 * テナント越境は {@link NotFoundException}（404）とし、本例外とは区別する。
 */
public class ForbiddenException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }
}
