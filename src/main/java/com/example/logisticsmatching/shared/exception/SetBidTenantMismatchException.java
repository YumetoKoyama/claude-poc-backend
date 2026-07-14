package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/** TC-011: セット応募対象が異なる配送依頼企業（400 / SET_BID_TENANT_MISMATCH、MSG-003, BR-005）。 */
public class SetBidTenantMismatchException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SetBidTenantMismatchException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.SET_BID_TENANT_MISMATCH);
    }
}
