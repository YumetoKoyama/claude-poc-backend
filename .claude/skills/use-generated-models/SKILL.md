---
name: use-generated-models
description: OpenAPI 生成クラス（generated.openapi.*）を presentation 層に使用するフロー。新規実装・既存置き換えの両方に対応。Request/Response DTO・エラーレスポンス・ページネーション・ダッシュボード複合型が対象。
context: fork
argument-hint: <ISSUE-NUMBER または feature パス>
---

# 生成モデルの使用（新規実装・置き換え共通フロー）

`com.example.logisticsmatching.generated.openapi.*` クラスを presentation 層の DTO として使用する。手書き DTO は作らない。

対象 Issue / フィーチャ: $ARGUMENTS

## 適用場面

| 場面 | 説明 | 手順 9 |
|---|---|---|
| **新規実装** | 手書き DTO を作らず、生成クラスを最初から使う | 不要 |
| **既存置き換え** | 手書き DTO（`*.presentation.request.*` / `*.presentation.response.*`）を生成クラスへ移行する | 実施する |

手順 1〜8 は両場面共通。

## 前提条件

- `bash scripts/gen-api-models.sh` が実行可能で `target/generated-sources/openapi/` が存在すること
- `docs/design/api/*.yaml` が最新であること（YAML が Source of Truth）

## 手順

### 1. 生成クラスの最新化

```bash
bash scripts/gen-api-models.sh
```

### 2. 対象クラスの特定

使用する（または置き換える）クラスを 5 カテゴリで整理する。生成クラスが存在しないカテゴリは手書きを温存する。

| カテゴリ | 生成クラス | 新規実装での使い方 |
|---|---|---|
| Request DTO | `*Request` | `@RequestBody` の型として直接使用 |
| Response DTO | `*Response` | Controller 戻り値 / UseCase 戻り値として使用 |
| エラーレスポンス | `*4xxResponse` / `*DetailsInner` | `GlobalExceptionHandler` / `@Operation` に使用 |
| ページネーション / 一覧 | `*ListResponse` + `*PageInfo` | 一覧 API の戻り値として使用 |
| ダッシュボード複合型 | `CarrierDashboard` / `ShipperDashboard` | ダッシュボード API の戻り値として使用 |

### 3. 差異分析（YAML 不整合の検出）

手書き実装（または設計書の想定型）と生成クラスを比較し、差異を記録する。詳細パターンは [references/adaptation-patterns.md](references/adaptation-patterns.md) を参照。

**YAML 修正が必要な差異 → docs Issue 起票・実装中断**

> YAML は設計フェーズの正典であり、フロントエンドも同じ YAML から自動生成している。実装者が直接修正してはならない。

1. `claude-poc-docs` リポジトリに GitHub Issue を起票する（差異内容・影響範囲・修正案を記載）
2. **実装をここで中断し**、ユーザーに Issue URL を報告する
3. docs の Issue が採択（`main` マージ）されてから再開する

YAML 修正が必要な差異の例: フィールド名の表記ゆれ・数値型の不一致（`Long` vs `Integer`）・enum 値の不一致・フィールドの過不足

**YAML 修正不要の差異 → 実装継続**

| 差異 | 対処 |
|---|---|
| カスタムバリデーション不足（`@FutureDatetime` 等） | UseCase 内 `Validator` または `@Validated` グループで補完 |
| ドメイン enum ↔ 生成 inline enum | Controller 境界で `EnumMapper` を挟む（[references/enum-mapping-guide.md](references/enum-mapping-guide.md) 参照） |

### 4. Request DTO の実装

1. Controller の `@RequestBody` の型に生成クラスを使用する
2. UseCase / Service には生成クラスをそのまま渡すか、ドメインオブジェクトへ変換してから渡す
3. **ドメイン enum への変換**が必要な場合は Controller で `EnumMapper` を呼ぶ
4. **カスタムバリデーション**（`@FutureDatetime` 等）が生成クラスにない場合、生成クラスには追記しない（再生成で消えるため）。UseCase 内 `Validator` で対処する
5. テストの `@RequestBody` 型に生成クラスを使用する

### 5. Response DTO の実装

1. Controller の戻り値型に生成クラスを使用する
2. UseCase / Service の戻り値型に生成クラスを使用する
3. 生成クラスは setter を持つため `new XxxResponse()` → setter で組み立てる

```java
JobResponse res = new JobResponse();
res.setJobId(job.getId().intValue());
res.setFromLocation(job.getFromLocation());
return ResponseEntity.ok(res);
```

### 6. エラーレスポンスの実装

- `_common.yaml` の `ErrorResponse` スキーマから生成された `ErrorResponse` を `GlobalExceptionHandler` で使用する
- 操作固有の 4xx クラス（`CreateJob400Response` 等）は `@Operation` アノテーションの型宣言に使用する

### 7. ページネーション / 一覧型の実装

```java
OpenJobListResponse res = new OpenJobListResponse();
OpenJobListResponsePageInfo page = new OpenJobListResponsePageInfo();
page.setPage(pageRequest.getPage());
page.setSize(pageRequest.getSize());
page.setTotalCount(totalCount);
res.setPageInfo(page);
res.setItems(items);
return ResponseEntity.ok(res);
```

### 8. コンパイルと品質ゲート

```bash
mvn compile -q && mvn test -q
```

エラーが残る場合は 1 件ずつ解消してから次へ進む。

### 9. 旧ファイルの削除（置き換えの場合のみ）

生成クラスで完全に代替できた手書き DTO ファイルを削除する。

```bash
git rm src/main/java/com/example/logisticsmatching/<feature>/presentation/request/XxxRequest.java
git rm src/main/java/com/example/logisticsmatching/<feature>/presentation/response/XxxResponse.java
```

削除後に `mvn compile && mvn test` を再実行して問題がないことを確認する。

## 完了条件

- 対象機能の presentation 層で手書き DTO が新規作成されていない（新規実装）、または削除済み（置き換え）
- `*.presentation.request.*` / `*.presentation.response.*` に残るのは生成対応物が無いクラスのみ
- ドメイン enum はドメイン層に残り、Controller 境界のみで変換している
- `mvn compile` と `mvn test` が成功している

## 注意事項

- **生成クラスを直接編集しない**（`mvn generate-sources` で上書きされる）
- **ドメインモデル（`*.domain.model.*`）は対象外**。生成クラスは presentation 層の DTO のみ
- **YAML の不整合は docs Issue 起票・実装中断**。フロントエンドにも影響するため実装者が直接修正しない
