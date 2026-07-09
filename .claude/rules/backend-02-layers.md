# バックエンド実装ルール 02 — レイヤー責務と依存ルール

## レイヤー構成

各モジュールは以下の 4 レイヤーで構成する。
参照系が中心の `query` モジュールは `domain/` を省略してよい。

```
[module]/
├─ presentation/    # REST API 入口
├─ application/     # ユースケース実行
├─ domain/          # 業務ルール本体
└─ infrastructure/  # 技術依存の実装
```

---

## 依存方向ルール（必須）

以下の方向にのみ依存を許可する。逆方向の依存を作ってはならない。

```
presentation → application → domain
                    ↓
              port (interface)
                    ↑
              infrastructure
```

1. `presentation` は `application` にのみ依存する
2. `application` は `domain` と `port`（interface）に依存する
3. `domain` は Spring を含む外部フレームワークに依存しない
4. `infrastructure` は `domain` / `application` の interface を実装する
5. `query` は書込み系の `domain` に深入りしない
6. `shared` に業務ロジックを置かない

---

## presentation レイヤー

### 配置するもの

- `Controller`（REST エンドポイント）
- `Request DTO` / `Response DTO`（**OpenAPI YAML からの生成クラス `generated.openapi.*` を使用する。手書き DTO は作らない**。OpenAPI 未定義の内部 DTO のみ手書き可）
- 例外ハンドリングの入口側変換

### 禁止事項

- `Repository` の直接呼び出し
- 業務ロジック・業務判断の記述
- `Entity`（domain model / JPA entity 両方）の直接返却
- `@Transactional` の宣言
- **生成クラス（`generated.openapi.*`）を直接編集すること**（`mvn generate-sources` で上書きされる）

### 命名規則

```
JobCommandController.java
JobQueryController.java
CreateJobRequest.java
JobResponse.java
```

> 命名は OpenAPI の `operationId` / スキーマ名から生成される。生成クラス名はそのまま使用し、別名のラッパーを手書きしない。

### 生成 DTO の使用と要件補完（必須）

presentation 層の Request/Response DTO は OpenAPI YAML から生成したクラス（`com.example.logisticsmatching.generated.openapi.*`）を使用する。生成・再生成は `bash scripts/gen-api-models.sh` で行う（`backend-00` #16）。

1. Controller の `@RequestBody` 型・戻り値型に生成クラスを直接使用する。
2. ドメイン enum と生成 inline enum の変換は Controller 境界の `EnumMapper` で行い、ドメイン enum はドメイン層に残す。
3. 生成クラスへ直接付与できない要件は、生成クラスを変更せず別アーティファクトで補完する（再生成で消えるため）:
   - 認可（`@PreAuthorize`）→ Controller メソッド（`backend-07` R-SEC-030）
   - テナント越境防止（取得後のテナント ID 照合・404 隠蔽）→ UseCase（R-SEC-010 / R-SEC-011）
   - 相対条件・相関制約のカスタムバリデーション → UseCase 内 `Validator`（R-SEC-020）
   - Mass Assignment 防止（テナント ID / ユーザー ID / ロールをリクエストから受け取らない）→ UseCase（R-SEC-060 / R-SEC-061）
4. YAML ベンダー拡張で自動付与できる Bean Validation・マスク注釈は YAML 側で宣言する（`backend-07` ベンダー拡張連動）。コード側へ手で付け足さない。
5. 生成クラスと手書き実装の差異が YAML 不整合に起因する場合（フィールド名・型・enum 値の不一致等）は、実装者が YAML を直接修正せず `claude-poc-docs` に Issue を起票して実装を中断する（YAML は設計フェーズの正典で FE も同じ YAML から生成するため）。

---

## application レイヤー

### 配置するもの

- `UseCase`（1 クラス 1 ユースケース）
- `Command` / `Query` 入力オブジェクト
- `@Transactional`（トランザクション境界）
- Domain Event の発火調停
- 外部 Port の呼び出し調停

### ルール

- UseCase は 1 クラス 1 責務とする（例: `CreateJobUseCase`, `DeleteJobUseCase`）
- Web API からも、バッチからも、この層を経由させる
- `@Transactional` はこの層に置き、domain 層には置かない

### 禁止事項

- Spring Security / JPA など Spring 固有の依存を直接使う
- 業務ルールの本体をここに書く（Entity / Domain Service に委譲する）
- 通知送信を UseCase 本文に直接書く

### 命名規則

```
CreateJobUseCase.java
DeleteJobUseCase.java
ApplySingleBidUseCase.java
ApplySetBidUseCase.java
AgreeDealUseCase.java
```

---

## domain レイヤー

### 配置するもの

- `Entity`（業務上の実体）
- `Value Object`（不変の値型）
- `Domain Service`（複数 Entity にまたがる業務ルール）
- `Repository interface`（永続化の抽象）
- `Domain Event`（業務イベント表現）

### ルール

- Entity の状態変更はパブリック setter ではなく業務メソッドで行う
- Repository は interface のみ定義し、実装は infrastructure に置く
- Spring / JPA / Jackson などのフレームワーク依存を持たない

### 禁止事項

- `@Entity`, `@Transactional`, `@Autowired` などの Spring / JPA アノテーション
- `LocalDateTime.now()` の直接呼び出し
- 外部通知・メール送信の直接呼び出し
- JPA Entity と domain model の混用（別クラスとして定義する）

### 命名規則

```
Job.java, JobId.java, JobStatus.java    # Entity / Value Object
Bid.java, SetBid.java, BidLimitPolicy.java
DealSnapshot.java, FinalCondition.java
JobRepository.java                      # interface
```

---

## infrastructure レイヤー

### 配置するもの

- JPA Entity（`@Entity` を持つ永続化クラス）
- Spring Data Repository（`JpaRepository` 継承）
- `Repository` interface 実装クラス
- 通知・メール・外部 API・Clock の実装

### ルール

- Spring Boot 依存はすべてこの層に閉じ込める
- JPA Entity と domain model は別クラスとして定義する
- `Repository` 実装は domain の `Repository` interface を `implements` する

### 禁止事項

- domain model に JPA アノテーションを付与する
- infrastructure の実装クラスを application / domain から直接 import する

### 命名規則

```
JobJpaEntity.java
SpringDataJobRepository.java
JobRepositoryImpl.java
AppNotificationPublisher.java
SystemClock.java
```

---

## レイヤー違反チェックリスト

実装・レビュー時に以下を確認する。

| 違反パターン | 確認レイヤー |
|------------|------------|
| Controller に業務ロジックがある | presentation |
| Controller が Repository を直接 import している | presentation |
| UseCase の中に `@Entity` や JPA クエリがある | application |
| Domain クラスに `@Autowired` や `@Transactional` がある | domain |
| Domain クラスが `LocalDateTime.now()` を直接呼んでいる | domain |
| JPA Entity が API レスポンスとして返されている | presentation |
| 通知送信が UseCase 本文に直接書かれている | application |
| Batch が Domain を通さず Repository を直接呼んでいる | infrastructure |
