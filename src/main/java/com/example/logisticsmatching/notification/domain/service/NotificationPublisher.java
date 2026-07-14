package com.example.logisticsmatching.notification.domain.service;

import com.example.logisticsmatching.shared.id.TenantId;

/**
 * 通知発火の抽象化（Port、IMPL-10、R-ARCH-008）。
 *
 * <p>通知送信ロジックを UseCase や Domain Service に直接書かず、本 Port（Domain Event 経由）で発火する。
 * 通知宛先粒度はテナント単位とする（{@code 共通部品設計.md} §6）。実装（アダプタ）は
 * {@code notification/infrastructure/} 側に別 Issue で置く。
 */
@FunctionalInterface
public interface NotificationPublisher {

    /**
     * テナント宛に通知を発行する。
     *
     * @param recipient 通知先テナント ID
     * @param messageCode メッセージコード（{@code MSG-XXX}、メッセージ一覧.md 参照）
     */
    void publish(TenantId recipient, String messageCode);
}
