// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-08: ロギング（機微情報マスク、R-SEC-050）。
 * パスワード・トークン・氏名・電話番号・メールアドレスを含む項目はログへ出力しない（マスクする）。
 * `_common.yaml` の `x-sensitive: true` フィールドと対応するキー名を対象とする。
 */
class SensitiveDataMaskerTest {

    @Test
    @DisplayName("TC-031: password / token / name / phone / email はマスクされ、平文でログへ出力されない")
    void tc031_masksSensitiveFields() {
        assertThat(SensitiveDataMasker.mask("password", "P@ssw0rd!")).isEqualTo("***");
        assertThat(SensitiveDataMasker.mask("token", "abc.def.ghi")).isEqualTo("***");
        assertThat(SensitiveDataMasker.mask("name", "山田太郎")).isEqualTo("***");
        assertThat(SensitiveDataMasker.mask("phone", "090-1234-5678")).isEqualTo("***");
        assertThat(SensitiveDataMasker.mask("email", "user@example.com")).isEqualTo("***");
    }

    @Test
    @DisplayName("TC-031: 機微情報でないフィールドはマスクされずそのまま出力される")
    void tc031_doesNotMaskNonSensitiveFields() {
        assertThat(SensitiveDataMasker.mask("companyName", "株式会社サンプル")).isEqualTo("株式会社サンプル");
    }

    @Test
    @DisplayName("TC-031（境界値）: フィールド名が null の場合は非機微情報として値をそのまま返す")
    void tc031_nullFieldNameIsNotMasked() {
        assertThat(SensitiveDataMasker.mask(null, "value")).isEqualTo("value");
    }
}
