package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/**
 * TC-006: リソース未存在／テナント越境（404 / NOT_FOUND）。
 * テナント越境は存在秘匿のため 404 に統一する（自テナント内の権限不足は {@link ForbiddenException} の 403）。
 */
public class NotFoundException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND);
    }
}
