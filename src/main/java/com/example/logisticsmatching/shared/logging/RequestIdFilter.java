package com.example.logisticsmatching.shared.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * リクエスト ID を MDC に付与する Servlet Filter（IMPL-08、R-SEC-052）。
 *
 * <p>例外ログにリクエスト ID を含め、障害調査での追跡を可能にする。リクエスト処理後は
 * 必ず MDC をクリアし、スレッドプール再利用時に前リクエストの ID が漏れないようにする。
 */
@Component
public class RequestIdFilter implements Filter {

    static final String MDC_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        MDC.put(MDC_KEY, UUID.randomUUID().toString());
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
