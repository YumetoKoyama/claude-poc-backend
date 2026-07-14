// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.logisticsmatching.generated.openapi.PageMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-09: ページング応答共通化（PageMeta）。docs/design/api/_common.yaml の PageMeta を正典とする。
 * 一覧系 operationId（listJobs, listMyBids, listNotifications 等）が共通で使う算出ロジック。
 */
class PageMetaFactoryTest {

    @Test
    @DisplayName("TC-033: page/size/totalElements から totalPages を含む PageMeta が仕様どおりに算出される")
    void tc033_calculatesPageMeta() {
        PageMeta meta = PageMetaFactory.of(1, 20, 45);

        assertThat(meta.getPage()).isEqualTo(1);
        assertThat(meta.getSize()).isEqualTo(20);
        assertThat(meta.getTotalElements()).isEqualTo(45);
        assertThat(meta.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("TC-034（境界値）: 該当件数 0 件は totalPages=0 として算出される")
    void tc034_zeroElements() {
        PageMeta meta = PageMetaFactory.of(1, 20, 0);

        assertThat(meta.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("TC-034（境界値）: ちょうど1ページ分の件数は totalPages=1 として算出される")
    void tc034_exactlyOnePage() {
        PageMeta meta = PageMetaFactory.of(1, 20, 20);

        assertThat(meta.getTotalPages()).isEqualTo(1);
    }
}
