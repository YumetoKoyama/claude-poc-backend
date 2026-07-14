package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/** TC-007: 重複（409 / DUPLICATE、法人重複 AC-201/MSG-024、応募重複 MSG-017 等）。 */
public class DuplicateException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.DUPLICATE);
    }
}
