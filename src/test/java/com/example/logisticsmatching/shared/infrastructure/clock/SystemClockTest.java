// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared.infrastructure.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-05: {@code SystemClock}（{@code Clock} の本番実装）。
 */
class SystemClockTest {

    @Test
    @DisplayName("TC-021: SystemClock.now() は LocalDateTime.now() 相当の値を返す")
    void tc021_now() {
        SystemClock clock = new SystemClock();

        LocalDateTime before = LocalDateTime.now();
        LocalDateTime actual = clock.now();
        LocalDateTime after = LocalDateTime.now();

        assertThat(actual).isBetween(before.minus(1, ChronoUnit.SECONDS), after.plus(1, ChronoUnit.SECONDS));
    }
}
