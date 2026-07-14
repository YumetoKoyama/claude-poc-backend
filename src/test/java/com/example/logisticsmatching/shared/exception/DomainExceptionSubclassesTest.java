// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * IMPL-01: DomainException 系例外クラス階層と HTTP ステータス・ErrorCode 対応表。
 * 各サブクラスが生成時に自身の httpStatus / errorCode を正しく保持することを検証する。
 * 対応表は docs/design/共通部品設計.md §1.1 を正典とする。
 */
class DomainExceptionSubclassesTest {

    @Test
    @DisplayName("TC-001: ValidationException は httpStatus=400・code=VALIDATION_ERROR を保持する")
    void tc001_validationException() {
        ValidationException ex = new ValidationException("bad request");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("TC-002: AuthenticationFailedException は httpStatus=401・code=LOGIN_FAILED を保持する")
    void tc002_authenticationFailedException() {
        AuthenticationFailedException ex = new AuthenticationFailedException("login failed");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("TC-003: AccountLockedException は httpStatus=401・code=ACCOUNT_LOCKED を保持する")
    void tc003_accountLockedException() {
        AccountLockedException ex = new AccountLockedException("account locked");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    @DisplayName("TC-004: UnauthenticatedException は httpStatus=401・code=UNAUTHENTICATED を保持する")
    void tc004_unauthenticatedException() {
        UnauthenticatedException ex = new UnauthenticatedException("unauthenticated");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED);
    }

    @Test
    @DisplayName("TC-005（権限境界）: ForbiddenException は httpStatus=403・code=FORBIDDEN を保持する（テナント越境の404とは区別）")
    void tc005_forbiddenException() {
        ForbiddenException ex = new ForbiddenException("forbidden");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("TC-006: NotFoundException は httpStatus=404・code=NOT_FOUND を保持する")
    void tc006_notFoundException() {
        NotFoundException ex = new NotFoundException("not found");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("TC-007: DuplicateException は httpStatus=409・code=DUPLICATE を保持する")
    void tc007_duplicateException() {
        DuplicateException ex = new DuplicateException("duplicate");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE);
    }

    @Test
    @DisplayName("TC-008: StateConflictException は httpStatus=409・code=CONFLICT を保持する")
    void tc008_stateConflictException() {
        StateConflictException ex = new StateConflictException("conflict");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("TC-009（境界値）: BidLimitExceededException は httpStatus=409・code=BID_LIMIT_EXCEEDED を保持する")
    void tc009_bidLimitExceededException() {
        BidLimitExceededException ex = new BidLimitExceededException("limit exceeded");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BID_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("TC-010（境界値）: BidDeadlinePassedException は httpStatus=409・code=BID_DEADLINE_PASSED を保持する")
    void tc010_bidDeadlinePassedException() {
        BidDeadlinePassedException ex = new BidDeadlinePassedException("deadline passed");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BID_DEADLINE_PASSED);
    }

    @Test
    @DisplayName("TC-011: SetBidTenantMismatchException は httpStatus=400・code=SET_BID_TENANT_MISMATCH を保持する")
    void tc011_setBidTenantMismatchException() {
        SetBidTenantMismatchException ex = new SetBidTenantMismatchException("tenant mismatch");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SET_BID_TENANT_MISMATCH);
    }

    @Test
    @DisplayName("TC-012（境界値）: ResetTokenExpiredException は httpStatus=400・code=RESET_TOKEN_EXPIRED を保持する"
            + "（期限ちょうど/直後の判定ロジック自体は本例外を投げる呼び出し元 Issue 側で検証する）")
    void tc012_resetTokenExpiredException() {
        ResetTokenExpiredException ex = new ResetTokenExpiredException("token expired");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESET_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("TC-013（境界値）: RateLimitedException は httpStatus=429・code=RATE_LIMITED を保持する"
            + "（上限ちょうど/超過の判定ロジック自体は本例外を投げる呼び出し元 Issue 側で検証する）")
    void tc013_rateLimitedException() {
        RateLimitedException ex = new RateLimitedException("rate limited");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RATE_LIMITED);
    }
}
