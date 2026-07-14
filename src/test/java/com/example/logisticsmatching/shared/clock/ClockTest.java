// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-05: {@code Clock} インターフェース経由の時刻取得統一（R-ARCH-010、LocalDateTime.now() 直接呼び出し禁止）。
 */
class ClockTest {

    @Test
    @DisplayName("TC-022: テスト用固定 Clock 実装に差し替えると、呼び出し元は固定時刻で動作する")
    void tc022_fixedClockSubstitution() {
        LocalDateTime fixed = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        Clock fixedClock = () -> fixed;

        assertThat(fixedClock.now()).isEqualTo(fixed);
        // Clock 経由以外（LocalDateTime.now() 直接呼び出し）で時刻取得していないことは、
        // 呼び出し元クラスが Clock を注入されている構成（コンストラクタインジェクション等）で
        // 各モジュールの実装 Issue 側のテストにて担保する。
    }
}
