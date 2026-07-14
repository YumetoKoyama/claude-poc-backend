package com.example.logisticsmatching.shared.exception;

/**
 * バリデーションエラーの単一項目（field / reason）。
 *
 * <p>{@code ErrorResponse.details[]}（{@code api/_common.yaml} 正典）へマッピングされる
 * ドメイン側の中間表現。フィールド名は camelCase（画面入力項目名）を保持する。
 */
public record ValidationError(String field, String reason) {}
