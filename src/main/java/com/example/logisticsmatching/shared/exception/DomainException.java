package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * 業務例外の抽象基底クラス（IMPL-01）。
 *
 * <p>各サブクラスは自身に対応する {@link HttpStatus} と {@link ErrorCode} を保持し、
 * {@code shared.GlobalExceptionHandler} が型を問わず共通のマッピングロジックで
 * {@code ErrorResponse} へ変換できるようにする（個別 {@code @ExceptionHandler} の増殖を防ぐ）。
 * 対応表は {@code docs/design/共通部品設計.md} §1.1 を正典とする。
 */
public abstract class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;
    private final List<ValidationError> details;

    protected DomainException(String message, HttpStatus httpStatus, ErrorCode errorCode) {
        this(message, httpStatus, errorCode, List.of());
    }

    protected DomainException(
            String message, HttpStatus httpStatus, ErrorCode errorCode, List<ValidationError> details) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ValidationError> getDetails() {
        return details;
    }
}
