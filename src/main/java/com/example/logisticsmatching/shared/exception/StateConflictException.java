package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/**
 * TC-008: 状態競合／楽観ロック競合（409 / CONFLICT、MSG-009, MSG-010, MSG-011, MSG-021, MSG-022 等）。
 * {@code OptimisticLockException} / {@code DataIntegrityViolationException} も本例外相当として
 * {@code GlobalExceptionHandler} で 409 にマッピングする（RC-01）。
 */
public class StateConflictException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public StateConflictException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.CONFLICT);
    }
}
