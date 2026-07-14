// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.shared.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.logisticsmatching.shared.GlobalExceptionHandler;
import com.example.logisticsmatching.shared.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * IMPL-08: ロギング方式（レベル使い分け）。docs/design/共通部品設計.md §4 を正典とする。
 * WARN: 業務例外（400/404/409 系）。ERROR: 未捕捉例外。
 */
class GlobalExceptionHandlerLoggingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("TC-029: 業務例外（404 系）発生時、WARN レベルでログ出力される")
    void tc029_businessExceptionLogsAtWarn() {
        handler.handleDomainException(new NotFoundException("not found"));

        assertThat(appender.list).anyMatch(event -> event.getLevel() == Level.WARN);
    }

    @Test
    @DisplayName("TC-030: 未捕捉例外発生時、ERROR レベルでログ出力される")
    void tc030_unexpectedExceptionLogsAtError() {
        handler.handleUnexpectedException(new RuntimeException("boom"));

        assertThat(appender.list).anyMatch(event -> event.getLevel() == Level.ERROR);
    }
}
