// TDD: pre-write — 実装前テスト。/implement-from-issue 実行後に全テストが通ることを確認する
package com.example.logisticsmatching.auth.domain.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * IMPL-11: MailSender（Port）インターフェース雛形（EXT-001）。
 * 実装本体（SMTP 送信）は「[backend] 認証APIの実装」Issue（auth.yaml）側で行うため、
 * 本 Issue ではシグネチャに準拠したスタブ（モック）呼び出しのみを検証する。
 */
class MailSenderTest {

    @Test
    @DisplayName("TC-036: MailSender Port のシグネチャどおりに呼び出し可能である（実装は auth Issue 側で検証）")
    void tc036_callableViaPortSignature() {
        MailSender mailSender = mock(MailSender.class);

        mailSender.send("user@example.com", "パスワード再設定", "本文");

        verify(mailSender).send("user@example.com", "パスワード再設定", "本文");
    }
}
