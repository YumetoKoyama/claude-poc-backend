package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import java.util.List;
import org.springframework.http.HttpStatus;

/** TC-001: 入力検証エラー（400 / VALIDATION_ERROR）。 */
public class ValidationException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
    }

    public ValidationException(String message, List<ValidationError> details) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, details);
    }
}
