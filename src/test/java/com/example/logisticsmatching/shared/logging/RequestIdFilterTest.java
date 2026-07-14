// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * IMPL-08: ロギング（MDC リクエスト ID、R-SEC-052）。
 * 例外ログにリクエスト ID を含め、障害調査での追跡を可能にする。
 */
class RequestIdFilterTest {

    private static final String MDC_KEY = "requestId";

    @Test
    @DisplayName("TC-032: リクエスト処理中は MDC にリクエスト ID が設定され、処理後にクリアされる")
    void tc032_setsAndClearsRequestIdInMdc() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = (req, res) -> assertThat(MDC.get(MDC_KEY)).isNotBlank();

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(MDC_KEY)).isNull();
    }
}
