package com.example.logisticsmatching.shared.logging;

import java.util.Set;

/**
 * 機微情報のログマスク（IMPL-08、R-SEC-050）。
 *
 * <p>パスワード・トークン・氏名・電話番号・メールアドレスはログへ平文出力せずマスクする。
 * 対象フィールド名は {@code api/_common.yaml} の {@code x-sensitive: true} フィールドに対応する。
 *
 * <p><b>本 Issue（#3）時点での利用範囲</b>: 本クラスは、業務モジュールが request/response の
 * フィールド値を構造化ログ（{@code log.info("field={}", SensitiveDataMasker.mask(name, value))} 等）
 * に出力する際に呼び出す想定のユーティリティである。生成 DTO（{@code generated.openapi.*}）自体の
 * {@code toString()} マスクは、別経路（{@code pojo.mustache} カスタムテンプレート、`[MASKED]` 出力）で
 * 独立して担保されており、本クラスと責務が重複するものではない。本 Issue は共通部品の土台のみを
 * 提供する回であり、実際に機微値をログ出力する業務ロジック（認証 API 等）はまだ存在しないため、
 * 現時点で本クラスの呼び出し元は無い（未使用は意図した状態であり、後続の業務 Issue で配線される）。
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
