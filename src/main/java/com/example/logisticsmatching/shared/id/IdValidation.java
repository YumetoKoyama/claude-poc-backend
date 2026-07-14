package com.example.logisticsmatching.shared.id;

/** 型安全な共通識別子（IMPL-06）の値検証を集約する内部ヘルパー。各 {@code *Id} レコードから利用する。 */
final class IdValidation {

    private IdValidation() { }

    static void requireNonNull(Long value, String idTypeName) {
        if (value == null) {
            throw new IllegalArgumentException(idTypeName + " must not be null");
        }
    }
}
