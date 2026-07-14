// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-07: EnumMapper（ドメイン enum ↔ OpenAPI 生成 inline enum の変換）。
 * 各モジュールの Controller 境界で使う想定だが、本テストでは名前一致方式の契約をローカル定義の enum で検証する。
 */
class EnumMapperTest {

    private enum SampleDomainStatus {
        ACTIVE,
        INACTIVE
    }

    private enum SampleApiStatus {
        ACTIVE,
        INACTIVE
    }

    private enum SampleApiStatusMissingValue {
        ACTIVE
        // INACTIVE が存在しない = ドメイン側との不整合を再現するための定義
    }

    @Test
    @DisplayName("TC-026: ドメイン enum → OpenAPI 生成 inline enum へ名前一致で 1:1 変換される")
    void tc026_mapsMatchingEnumByName() {
        SampleApiStatus mapped = EnumMapper.map(SampleDomainStatus.ACTIVE, SampleApiStatus.class);

        assertThat(mapped).isEqualTo(SampleApiStatus.ACTIVE);
    }

    @Test
    @DisplayName("TC-027: マッピング未定義の値を変換しようとすると例外を送出し、無変換で通さない")
    void tc027_throwsWhenTargetEnumHasNoMatchingConstant() {
        assertThatThrownBy(() -> EnumMapper.map(SampleDomainStatus.INACTIVE, SampleApiStatusMissingValue.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TC-028（境界値）: null を入力すると NPE を発生させず null 安全に処理される")
    void tc028_nullSafe() {
        SampleApiStatus mapped = EnumMapper.map(null, SampleApiStatus.class);

        assertThat(mapped).isNull();
    }
}
