package com.example.logisticsmatching.shared.id;

/** ユーザーの型安全な共通識別子（IMPL-06）。BIGINT 主キー（{@link Long}）を包む値オブジェクト。 */
public record UserId(Long value) {

    public UserId {
        IdValidation.requireNonNull(value, "UserId");
    }
}
