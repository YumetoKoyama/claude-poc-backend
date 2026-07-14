package com.example.logisticsmatching.shared.logging;

import java.util.Set;

/**
 * 機微情報のログマスク（IMPL-08、R-SEC-050）。
 *
 * <p>パスワード・トークン・氏名・電話番号・メールアドレスはログへ平文出力せずマスクする。
 * 対象フィールド名は {@code api/_common.yaml} の {@code x-sensitive: true} フィールドに対応する。
 */
public final class SensitiveDataMasker {

    private static final String MASK = "***";

    private static final Set<String> SENSITIVE_FIELD_NAMES =
            Set.of("password", "token", "name", "phone", "email");

    private SensitiveDataMasker() { }

    public static String mask(String fieldName, String value) {
        if (fieldName != null && SENSITIVE_FIELD_NAMES.contains(fieldName)) {
            return MASK;
        }
        return value;
    }
}
