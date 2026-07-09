# バックエンド実装ルール 04 — パッケージ構成ルールと命名

## パッケージ命名ルール

### R-PKG-001: 技術基準の平置き禁止

以下の構成を作ってはならない。

```
❌ controller/
❌ service/
❌ repository/
❌ entity/
```

機能別 top-level package を切り、その中でレイヤーを分ける。

### R-PKG-002: JPA Entity と domain model の分離

`job/domain/model/Job.java`（domain model）と
`job/infrastructure/persistence/JobJpaEntity.java`（JPA entity）は必ず別クラスとして定義する。
同一クラスに `@Entity` と業務メソッドを混在させてはならない。

### R-PKG-003: shared の肥大化禁止

shared へ追加するたびに「全モジュールで本当に必要か」を確認する。
特定機能に閉じた共通処理は、その機能モジュール内のサブパッケージに置く。

### R-PKG-004: OpenAPI 生成クラスは生成パッケージに隔離する

OpenAPI YAML から生成する Request/Response DTO は `com.example.logisticsmatching.generated.openapi.*` に出力される（`target/generated-sources/openapi/` 配下。生成は `bash scripts/gen-api-models.sh`。`backend-00` #16）。

- 生成パッケージ配下を**手で編集・コミットしない**（`mvn generate-sources` で再生成・上書きされる）。`target/` はビルド生成物として追跡対象外。
- 手書き DTO との使い分け: OpenAPI 定義済みのリクエスト/レスポンス → 生成クラス。DB エンティティ → 手書き `@Entity`。OpenAPI 未定義の内部 DTO → 手書き `dto.*`。同一物の二重定義を禁止し、OpenAPI 定義があれば生成クラスを優先する。
- 生成パッケージは静的解析・カバレッジの除外対象（Checkstyle `checkstyle-suppressions.xml` / PMD `pom.xml` の `excludeRoots` / SpotBugs `spotbugs-exclude.xml` / JaCoCo `**/generated/**`）。除外設定はリポジトリ固定配置とし、生成物に対する警告対応は不要。

---

## 書込み系モジュールの標準構成

`job`, `bid`, `deal`, `delivery`, `auth`, `tenant`, `notification` に適用する。

```
[module]/
├─ presentation/
│  ├─ [Module]CommandController.java
│  ├─ request/
│  └─ response/
├─ application/
│  ├─ [Action][Entity]UseCase.java
│  └─ ...
├─ domain/
│  ├─ model/
│  ├─ service/
│  ├─ event/
│  └─ repository/
└─ infrastructure/
   ├─ persistence/
   └─ ...
```

---

## job モジュール構成例

```
job/
├─ presentation/
│  ├─ JobCommandController.java
│  ├─ request/
│  │  ├─ CreateJobRequest.java
│  │  └─ UpdateJobRequest.java
│  └─ response/
│     └─ JobResponse.java
├─ application/
│  ├─ CreateJobUseCase.java
│  ├─ UpdateJobUseCase.java
│  └─ DeleteJobUseCase.java
├─ domain/
│  ├─ model/
│  │  ├─ Job.java
│  │  ├─ JobId.java
│  │  └─ JobStatus.java
│  ├─ service/
│  └─ repository/
│     └─ JobRepository.java
└─ infrastructure/
   └─ persistence/
      ├─ JobJpaEntity.java
      ├─ SpringDataJobRepository.java
      └─ JobRepositoryImpl.java
```

---

## bid モジュール構成例

```
bid/
├─ presentation/
├─ application/
│  ├─ ApplySingleBidUseCase.java
│  ├─ ApplySetBidUseCase.java
│  └─ UpdateBidUseCase.java
├─ domain/
│  ├─ model/
│  │  ├─ Bid.java
│  │  ├─ SetBid.java
│  │  ├─ BidStatus.java
│  │  └─ BidLimitPolicy.java
│  ├─ service/
│  └─ repository/
└─ infrastructure/
   ├─ persistence/
   └─ clock/
```

---

## deal モジュール構成例

```
deal/
├─ presentation/
├─ application/
│  ├─ SendMessageUseCase.java
│  ├─ PresentFinalConditionUseCase.java
│  └─ AgreeDealUseCase.java
├─ domain/
│  ├─ model/
│  │  ├─ Deal.java
│  │  ├─ FinalCondition.java
│  │  ├─ DealSnapshot.java
│  │  └─ MessageThread.java
│  ├─ service/
│  ├─ event/
│  └─ repository/
└─ infrastructure/
   ├─ persistence/
   └─ notification/
```

---

## query モジュール構成例（domain 省略）

```
query/
├─ presentation/
│  ├─ DashboardQueryController.java
│  ├─ OpenJobQueryController.java
│  └─ HistoryQueryController.java
├─ application/
│  ├─ GetDashboardQueryService.java
│  ├─ SearchOpenJobsQueryService.java
│  └─ GetHistoryQueryService.java
└─ infrastructure/
   └─ readmodel/
      ├─ DashboardReadRepository.java
      ├─ OpenJobReadRepository.java
      └─ HistoryReadRepository.java
```

---

## shared モジュール構成例

```
shared/
├─ id/
│  ├─ TenantId.java
│  ├─ UserId.java
│  └─ JobId.java
├─ clock/
│  └─ Clock.java          # interface のみ
├─ exception/
│  └─ DomainException.java
└─ config/
```

---

## V1 で分割しない（先送りする）もの

以下は V1 では細かく分割しない。将来の拡張時に対応する。

- `command` と `query` の Gradle module 分離
- `notification` の非同期基盤抽象化
- `rating` の独立 module 化（`delivery` に内包）
- `negotiation` と `deal` の完全分離（`deal` に内包）
- `set-bid` の独立 top-level package 化（`bid` に内包）
