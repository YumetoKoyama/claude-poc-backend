package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/**
 * TC-013（境界値）: パスワードリセット申請等のレート制限超過（429 / RATE_LIMITED、MSG-026）。
 * 上限ちょうど/超過の判定ロジック自体は本例外を投げる呼び出し元 Issue 側で検証する。
 */
public class RateLimitedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RateLimitedException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RATE_LIMITED);
    }
}
