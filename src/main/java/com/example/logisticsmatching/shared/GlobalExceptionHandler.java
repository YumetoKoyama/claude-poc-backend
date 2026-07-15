package com.example.logisticsmatching.shared;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import com.example.logisticsmatching.generated.openapi.ErrorResponse;
import com.example.logisticsmatching.generated.openapi.ErrorResponseDetailsInner;
import com.example.logisticsmatching.shared.exception.DomainException;
import com.example.logisticsmatching.shared.exception.ValidationError;
import jakarta.persistence.OptimisticLockException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 共通例外ハンドラ（IMPL-02〜IMPL-04）。
 *
 * <p>{@code DomainException} サブクラス・Bean Validation 違反・未捕捉例外を、それぞれ
 * {@code ErrorResponse}（{@code api/_common.yaml} 正典）へ一括マッピングする。個別
 * {@code @ExceptionHandler} を業務例外の種類ごとに増殖させず、{@code DomainException} 1 ハンドラで
 * 型に依存せず処理する（{@code docs/design/共通部品設計.md} §1 参照）。
 *
 * <p>{@code OptimisticLockException} / {@code OptimisticLockingFailureException}（Spring が
 * リポジトリ境界・トランザクションコミット時に JPA 例外を変換したもの）・
 * {@code DataIntegrityViolationException}（DB 一意制約違反等）は、{@code StateConflictException}
 * 相当として 409 / {@code CONFLICT} にマッピングする（RC-01）。
 *
 * <p>ログレベルは業務例外（400/401/403/404/409/429 系）を WARN、未捕捉例外を ERROR とする
 * （§4）。未捕捉例外はスタックトレース等の内部情報をレスポンスへ含めず、ログにのみ記録する
 * （R-SEC-040）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_MESSAGE = "サーバー内部でエラーが発生しました。しばらくしてから再度お試しください。";
    private static final String STATE_CONFLICT_MESSAGE =
            "相手の状態が変化したため、この操作は完了できません。最新の状態を確認してください。";

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        LOGGER.warn("Domain exception handled: code={}, status={}", ex.getErrorCode(), ex.getHttpStatus(), ex);

        ErrorResponse body = new ErrorResponse();
        body.setCode(ex.getErrorCode());
        body.setMessage(ex.getMessage());
        body.setDetails(toDetails(ex.getDetails()));

        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler({
        OptimisticLockingFailureException.class,
        OptimisticLockException.class,
        DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleStateConflictException(Exception ex) {
        LOGGER.warn("State conflict detected: type={}", ex.getClass().getSimpleName(), ex);

        ErrorResponse body = new ErrorResponse();
        body.setCode(ErrorCode.CONFLICT);
        body.setMessage(STATE_CONFLICT_MESSAGE);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorResponseDetailsInner> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();

        LOGGER.warn("Bean validation failed: fields={}", details.size());

        ErrorResponse body = new ErrorResponse();
        body.setCode(ErrorCode.VALIDATION_ERROR);
        body.setMessage("入力内容に誤りがあります。");
        body.setDetails(details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        LOGGER.error("Unexpected exception occurred", ex);

        ErrorResponse body = new ErrorResponse();
        body.setCode(ErrorCode.INTERNAL_ERROR);
        body.setMessage(INTERNAL_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private List<ErrorResponseDetailsInner> toDetails(List<ValidationError> details) {
        return details.stream()
                .map(detail -> {
                    ErrorResponseDetailsInner inner = new ErrorResponseDetailsInner();
                    inner.setField(detail.field());
                    inner.setReason(detail.reason());
                    return inner;
                })
                .toList();
    }

    private ErrorResponseDetailsInner toDetail(FieldError fieldError) {
        ErrorResponseDetailsInner inner = new ErrorResponseDetailsInner();
        inner.setField(fieldError.getField());
        inner.setReason(fieldError.getDefaultMessage());
        return inner;
    }
}
