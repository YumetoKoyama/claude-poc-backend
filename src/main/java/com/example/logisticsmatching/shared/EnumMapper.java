package com.example.logisticsmatching.shared;

/**
 * ドメイン enum ↔ OpenAPI 生成 inline enum の変換（IMPL-07）。
 *
 * <p>各モジュールの Controller 境界で、ドメイン層の enum を OpenAPI 生成モデルの inline enum へ
 * 名前一致（{@link Enum#name()}）で変換する。マッピング未定義（対応する定数が変換先に存在しない）の場合は
 * 無変換で通さず例外を送出する。
 */
public final class EnumMapper {

    private EnumMapper() { }

    public static <T extends Enum<T>> T map(Enum<?> source, Class<T> targetEnumClass) {
        if (source == null) {
            return null;
        }
        for (T candidate : targetEnumClass.getEnumConstants()) {
            if (candidate.name().equals(source.name())) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(
                "No matching constant '" + source.name() + "' in " + targetEnumClass.getName());
    }
}
