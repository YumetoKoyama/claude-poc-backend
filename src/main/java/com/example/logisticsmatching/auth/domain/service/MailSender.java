package com.example.logisticsmatching.auth.domain.service;

/**
 * メール送信の抽象化（Port、IMPL-11、EXT-001、R-ARCH-007）。
 *
 * <p>実装（SMTP/AWS SES 等を用いたアダプタ）は {@code auth/infrastructure/} 側に置く
 * （本 Issue では雛形のみを提供し、実装本体は「[backend] 認証APIの実装」Issue で行う）。
 */
@FunctionalInterface
public interface MailSender {

    /**
     * メールを送信する。
     *
     * @param to 宛先メールアドレス
     * @param subject 件名
     * @param body 本文
     */
    void send(String to, String subject, String body);
}
