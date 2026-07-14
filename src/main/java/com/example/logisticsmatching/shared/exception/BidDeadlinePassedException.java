package com.example.logisticsmatching.shared.exception;

import com.example.logisticsmatching.generated.openapi.ErrorCode;
import java.io.Serial;
import org.springframework.http.HttpStatus;

/** TC-010（境界値）: 応募自動締切超過（409 / BID_DEADLINE_PASSED、MSG-005, BR-010）。 */
public class BidDeadlinePassedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BidDeadlinePassedException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.BID_DEADLINE_PASSED);
    }
}
