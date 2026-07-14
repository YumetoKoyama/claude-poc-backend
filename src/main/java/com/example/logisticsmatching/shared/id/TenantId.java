package com.example.logisticsmatching.shared.id;

/** テナント（企業）の型安全な共通識別子（IMPL-06）。BIGINT 主キー（{@link Long}）を包む値オブジェクト。 */
public record TenantId(Long value) {

    public TenantId {
        IdValidation.requireNonNull(value, "TenantId");
    }
}
