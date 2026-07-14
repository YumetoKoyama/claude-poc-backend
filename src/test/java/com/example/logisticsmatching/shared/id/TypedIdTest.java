// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-06: 型安全な共通識別子（TenantId, UserId, JobId 等）。
 * 各 ID は BIGINT 主キー（Long）を包むため、null 検証のみを行う（空文字は該当しない）。
 */
class TypedIdTest {

    @Test
    @DisplayName("TC-023: TenantId は null で生成すると IllegalArgumentException を投げる")
    void tc023_tenantId_rejectsNull() {
        assertThatThrownBy(() -> new TenantId(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TC-023: UserId は null で生成すると IllegalArgumentException を投げる")
    void tc023_userId_rejectsNull() {
        assertThatThrownBy(() -> new UserId(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TC-023: JobId は null で生成すると IllegalArgumentException を投げる")
    void tc023_jobId_rejectsNull() {
        assertThatThrownBy(() -> new JobId(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TC-024（境界値）: 同一値を保持する TenantId 同士は equals/hashCode が値として一致する")
    void tc024_tenantId_equalsByValue() {
        TenantId a = new TenantId(42L);
        TenantId b = new TenantId(42L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("TC-024（境界値）: 同一値を保持する JobId 同士は equals/hashCode が値として一致する")
    void tc024_jobId_equalsByValue() {
        JobId a = new JobId(7L);
        JobId b = new JobId(7L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.value()).isEqualTo(7L);
    }

    @Test
    @DisplayName("TC-025（権限境界）: 同一の Long 値でも TenantId と UserId は型が異なるため一致しない（誤用防止）")
    void tc025_differentIdTypesAreNeverEqual() {
        TenantId tenantId = new TenantId(1L);
        UserId userId = new UserId(1L);

        assertThat(tenantId).isNotEqualTo(userId);
        // TenantId 型の変数へ UserId を代入することはコンパイルエラーとなり、
        // 呼び出し側での ID 種別取り違えを型システムで防止する（本テストは実行時の等価性のみ検証）。
    }
}
