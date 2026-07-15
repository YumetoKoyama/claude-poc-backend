// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import com.example.logisticsmatching.generated.openapi.ErrorResponse;
import com.example.logisticsmatching.shared.exception.ForbiddenException;
import com.example.logisticsmatching.shared.exception.NotFoundException;
import com.example.logisticsmatching.shared.exception.ValidationError;
import com.example.logisticsmatching.shared.exception.ValidationException;
import jakarta.persistence.OptimisticLockException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * IMPL-02〜IMPL-04: {@code @RestControllerAdvice} による DomainException / バリデーション例外 /
 * 未捕捉例外の {@code ErrorResponse} への一括マッピング。
 * 対応方式は docs/design/共通部品設計.md §1.2 を正典とする。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("TC-014: DomainException サブクラスを catch し、対応する httpStatus・ErrorResponse を返す")
    void tc014_handleDomainException() {
        NotFoundException ex = new NotFoundException("not found");

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("TC-015: ValidationException（details 保持あり）を catch すると details に field/reason が設定される")
    void tc015_handleDomainException_withValidationDetails() {
        ValidationException ex =
                new ValidationException("invalid", List.of(new ValidationError("companyName", "must not be blank")));

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isNotEmpty();
        assertThat(response.getBody().getDetails().get(0).getField()).isEqualTo("companyName");
        assertThat(response.getBody().getDetails().get(0).getReason()).isEqualTo("must not be blank");
    }

    @Test
    @DisplayName("TC-016（権限境界）: ForbiddenException を catch すると 403・code=FORBIDDEN を返す")
    void tc016_handleDomainException_forbidden() {
        ForbiddenException ex = new ForbiddenException("forbidden");

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("TC-037: OptimisticLockingFailureException を catch すると 409・code=CONFLICT を返す（StateConflictException 相当）")
    void tc037_handleStateConflictException_optimisticLockingFailureException() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("version mismatch");

        ResponseEntity<ErrorResponse> response = handler.handleStateConflictException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("TC-037: jakarta.persistence.OptimisticLockException を catch すると 409・code=CONFLICT を返す（StateConflictException 相当）")
    void tc037_handleStateConflictException_jpaOptimisticLockException() {
        OptimisticLockException ex = new OptimisticLockException("version mismatch");

        ResponseEntity<ErrorResponse> response = handler.handleStateConflictException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("TC-037: DataIntegrityViolationException を catch すると 409・code=CONFLICT を返す（StateConflictException 相当）")
    void tc037_handleStateConflictException_dataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("unique constraint violated");

        ResponseEntity<ErrorResponse> response = handler.handleStateConflictException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("TC-017: Bean Validation 違反（必須項目欠落）は 400・VALIDATION_ERROR・details[] を返す")
    void tc017_handleValidationException_singleField() {
        MapBindingResult bindingResult = new MapBindingResult(new java.util.HashMap<>(), "request");
        bindingResult.addError(new FieldError("request", "companyName", "must not be blank"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(response.getBody().getDetails()).hasSize(1);
    }

    @Test
    @DisplayName("TC-018（境界値）: 複数フィールドが同時に違反した場合、details 配列に違反数分の要素が含まれる")
    void tc018_handleValidationException_multipleFields() {
        MapBindingResult bindingResult = new MapBindingResult(new java.util.HashMap<>(), "request");
        bindingResult.addError(new FieldError("request", "companyName", "must not be blank"));
        bindingResult.addError(new FieldError("request", "address", "must not be blank"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).hasSize(2);
    }

    @Test
    @DisplayName("TC-019: 未捕捉の RuntimeException は 500・INTERNAL_ERROR を返し、内部情報を含まない")
    void tc019_handleUnexpectedException() {
        RuntimeException ex = new RuntimeException("db connection refused: secret-detail");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("secret-detail");
    }

    @Test
    @DisplayName("TC-020: 未捕捉例外発生時、レスポンスにはスタックトレースを含めない（ログ側にのみ記録する契約）")
    void tc020_handleUnexpectedException_noStackTraceInResponse() {
        RuntimeException ex = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).doesNotContain("at com.example");
    }
}
