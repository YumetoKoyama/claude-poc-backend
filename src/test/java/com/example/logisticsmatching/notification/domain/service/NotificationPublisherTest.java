// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.notification.domain.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.logisticsmatching.shared.id.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-10: NotificationPublisher（Port）の抽象化。通知宛先粒度はテナント単位（共通部品設計.md §6）。
 * 実装（アダプタ）は notification/infrastructure/ 側で別 Issue にて行うため、本 Issue では
 * Port インターフェースの形状のみをモックで検証する。
 */
class NotificationPublisherTest {

    @Test
    @DisplayName("TC-035: 通知発火を Port 経由で呼び出すと、テナント ID を宛先として通知が発行される（Port はモック）")
    void tc035_publishesViaPort() {
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        TenantId recipient = new TenantId(1L);

        publisher.publish(recipient, "MSG-020");

        verify(publisher).publish(recipient, "MSG-020");
    }
}
