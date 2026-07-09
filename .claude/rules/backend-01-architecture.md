# バックエンド実装ルール 01 — アーキテクチャ基本方針

## 採用構成

Spring Boot 単一アプリケーションを業務モジュールごとのモジュラモノリスとして実装する。
内部はオニオンアーキテクチャを基本とし、Application 層に UseCase 境界を明示する。
永続化・通知・メール・時刻・外部 API は Port / Adapter で分離する。

---

## 禁止構成

### 単純レイヤード構成（禁止）

`Controller → Service → Repository` の 3 層のみで実装してはならない。
業務ロジックを Service クラスに直接記述してはならない。

### マイクロサービス（禁止）

サービス分割を行ってはならない。
単一 Spring Boot アプリケーションとして実装する。

### クリーンアーキテクチャ全面適用（禁止）

全機能に `InputPort / OutputPort / Interactor` を適用してはならない。
UseCase 境界の概念のみを Application 層で採用する。

---

## 全体ルール

### R-ARCH-001: 機能別パッケージ分割（必須）

パッケージは業務機能単位で切る。以下の平置き構成を作ってはならない。

```
❌ controller/
❌ service/
❌ repository/
❌ entity/
```

正しい構成:

```
auth/ bid/ deal/ delivery/ job/ notification/ query/ shared/ tenant/
```

### R-ARCH-002: UseCase の一元化（必須）

Web API・バッチ起動・非同期処理のすべてを Application 層の UseCase 経由にする。
UseCase をバイパスして Infrastructure に直接アクセスする経路を作ってはならない。

### R-ARCH-003: バッチの DB 直更新禁止（必須）

バッチ（別リポジトリ）が DB を直接業務更新してドメインルールを迂回してはならない。
バッチは公開 API またはメッセージキュー経由で UseCase を呼び出す。

### R-ARCH-004: ドメインモデルの非公開（必須）

内部の Entity / Value Object / Repository interface を外部リポジトリに公開してはならない。
外部への契約は Response DTO または公開 API のレスポンス型のみとする。

### R-ARCH-005: Controller は薄く保つ（必須）

Controller の責務は UseCase の呼び出しと戻り値の DTO 変換のみ。
業務判断・条件分岐・バリデーションの本体を Controller に書いてはならない。

### R-ARCH-006: JPA Entity の API 露出禁止（必須）

JPA Entity をそのまま API レスポンスとして返してはならない。
必ず presentation 層の Response DTO に変換して返す。

### R-ARCH-007: Port / Adapter による外部分離（必須）

以下の外部依存は interface（Port）と実装クラス（Adapter）に分離する。

- `Repository`（永続化）
- `NotificationPublisher`（通知送信）
- `MailSender`（メール送信）
- `Clock`（現在時刻取得）
- 外部 API クライアント

### R-ARCH-008: 通知の直接送信禁止（必須）

通知送信ロジックを UseCase や Domain Service に直接書いてはならない。
Domain Event または Application Event 経由で発火する。

### R-ARCH-009: Entity の状態変更はメソッド経由（必須）

Entity の状態変更は setter ではなく、業務的意味を持つメソッド経由でのみ行う。

```java
// ❌ 禁止
job.setStatus(JobStatus.AGREED);

// ✅ 正しい
job.agree();
```

### R-ARCH-010: 時刻判定は Clock 経由（必須）

`LocalDateTime.now()` を直接呼んではならない。
`Clock` インターフェース経由で現在時刻を取得する。

---

## 重点ルール適用箇所

以下は業務ルールが複雑でアーキテクチャ違反が発生しやすい箇所である。
実装時は特に上記ルールを厳守する。

- 案件ステータス遷移制約
- 応募上限 20 社と from 時刻 2 時間前の自動締切
- セット応募のオール・オア・ナッシング
- 成約時のスナップショット保存と他応募クローズの連動
- 成約後の編集不可（不変条件）
- 論理削除 → 物理削除の遷移判定
- 同時応募時の枠消費競合
- 合意競合時の確定処理
