package com.example.logisticsmatching.generated.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-08 / R-SEC-051: {@code pojo.mustache} カスタムテンプレートが生成する
 * {@code x-sensitive}・{@code writeOnly} フィールドのマスク挙動（round-3 review の coverage 指摘対応）。
 *
 * <p>生成パッケージ（{@code generated.openapi.*}）は JaCoCo 計測除外対象（R-PKG-004）だが、
 * カスタムテンプレートが実際に意図した挙動（toString の [MASKED] 化、WRITE_ONLY によるレスポンス除外）を
 * 生んでいることは機能テストとして検証する。
 */
class GeneratedModelMaskingTest {

    @Test
    @DisplayName("生成された LoginRequest.toString() は password を [MASKED] とし平文を含まない")
    void loginRequestToString_masksPassword() {
        LoginRequest request = new LoginRequest("user01", "S3cr3tPassw0rd!");

        String actual = request.toString();

        assertThat(actual).contains("[MASKED]");
        assertThat(actual).doesNotContain("S3cr3tPassw0rd!");
    }

    @Test
    @DisplayName("生成された LoginRequest を JSON シリアライズすると writeOnly の password は出力されない")
    void loginRequestJsonSerialization_excludesWriteOnlyPassword() throws Exception {
        LoginRequest request = new LoginRequest("user01", "S3cr3tPassw0rd!");
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(request);

        assertThat(json).contains("\"loginId\"");
        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("S3cr3tPassw0rd!");
    }

    @Test
    @DisplayName("JSON からのデシリアライズでは writeOnly の password を受け取れる（書き込み専用）")
    void loginRequestJsonDeserialization_acceptsWriteOnlyPassword() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"loginId\":\"user01\",\"password\":\"S3cr3tPassw0rd!\"}";

        LoginRequest request = mapper.readValue(json, LoginRequest.class);

        assertThat(request.getPassword()).isEqualTo("S3cr3tPassw0rd!");
    }
}
