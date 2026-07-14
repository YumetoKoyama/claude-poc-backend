package com.example.logisticsmatching.shared.id;

/** 案件の型安全な共通識別子（IMPL-06）。BIGINT 主キー（{@link Long}）を包む値オブジェクト。 */
public record JobId(Long value) {

    public JobId {
        IdValidation.requireNonNull(value, "JobId");
    }
}
