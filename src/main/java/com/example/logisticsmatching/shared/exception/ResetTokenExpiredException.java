package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/**
 * TC-012（境界値）: パスワード再設定リンク期限切れ（400 / RESET_TOKEN_EXPIRED、MSG-019）。
 * 期限ちょうど/直後の判定ロジック自体は本例外を投げる呼び出し元 Issue 側で検証する。
 */
public class ResetTokenExpiredException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResetTokenExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.RESET_TOKEN_EXPIRED);
    }
}
