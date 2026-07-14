package com.example.logisticsmatching.shared.clock;

import java.time.LocalDateTime;

/**
 * 現在時刻取得の抽象化（IMPL-05、R-ARCH-010）。
 *
 * <p>{@code LocalDateTime.now()} の直接呼び出しを禁止し、本インターフェース経由での取得に統一する。
 * テストでは固定時刻を返すラムダ実装に差し替えることで、時刻依存ロジックを決定的に検証できる。
 */
@FunctionalInterface
public interface Clock {

    LocalDateTime now();
}
