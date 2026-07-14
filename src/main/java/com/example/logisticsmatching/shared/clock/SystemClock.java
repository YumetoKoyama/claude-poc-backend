package com.example.logisticsmatching.shared.clock;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/** {@link Clock} の本番実装（IMPL-05）。実時刻（{@code LocalDateTime.now()}）を返す。 */
@Component
public class SystemClock implements Clock {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
