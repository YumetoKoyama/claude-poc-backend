package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/** TC-009（境界値）: 応募上限超過（409 / BID_LIMIT_EXCEEDED、MSG-004, BR-009）。 */
public class BidLimitExceededException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BidLimitExceededException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.BID_LIMIT_EXCEEDED);
    }
}
