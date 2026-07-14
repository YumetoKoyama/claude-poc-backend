package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/** TC-003: ログイン試行ロック中（401 / ACCOUNT_LOCKED、MSG-016）。 */
public class AccountLockedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AccountLockedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ErrorCode.ACCOUNT_LOCKED);
    }
}
