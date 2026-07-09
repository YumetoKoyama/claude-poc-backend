---
name: augment-generated-models
description: 生成クラス（generated.openapi.*）に付与できないセキュリティ要件・バリデーション要件を、生成クラスを変更せずに Controller・UseCase・Validator 等の別アーティファクトで補完する。implement-loop の augment ステージとして produce の直後に呼ばれる。
context: fork
argument-hint: <ISSUE-NUMBER>
---

# 生成クラスへの要件付与（セキュリティ・バリデーション補完）

対象 Issue / フィーチャ: $ARGUMENTS

OpenAPI YAML から自動生成された DTO クラス（`com.example.logisticsmatching.generated.openapi.*`）は
再生成で上書きされるため、セキュリティ制約やカスタムバリデーションを直接付与できない。
本スキルは生成クラスを**変更せず**に、Controller・UseCase・Validator 等の別アーティファクトで要件を補完する。

## 補完対象の要件カテゴリ

### YAML vendor extension で自動補完（手動対応不要）

YAML 設計時に以下のベンダー拡張を正しく設定していれば、`mvn generate-sources` で自動生成される。
**本スキルの手順ではなく、YAML の設定漏れを検出・修正することが目的。**
詳細は `implement-from-issue/references/api-models-guide.md` の「YAML ベンダー拡張と自動生成される制約」を参照。

| 要件 | YAML 設定 | 自動生成物 | 根拠ルール |
|---|---|---|---|
| パスワード・機密フィールドの応答除外 | `format: password` または `writeOnly: true` | `@JsonProperty(WRITE_ONLY)` | R-SEC-111 |
| PII フィールドのログマスク | `format: password` / `writeOnly: true` / `x-sensitive: true` | `toString()` で `[MASKED]` | R-SEC-050、R-SEC-051 |
| 未来日時バリデーション | `x-validate-future: true` | `@Future` | R-SEC-020 |
| 現在以降日時バリデーション | `x-validate-future-or-present: true` | `@FutureOrPresent` | R-SEC-020 |
| 過去日時バリデーション | `x-validate-past: true` | `@Past` | R-SEC-020 |
| 正数バリデーション | `x-validate-positive: true` | `@Positive` | R-SEC-020 |
| 0以上バリデーション | `x-validate-positive-or-zero: true` | `@PositiveOrZero` | R-SEC-020 |
| 空白禁止バリデーション | `x-validate-not-blank: true` | `@NotBlank` | R-SEC-020 |

### 手動補完が必要（本スキルの主対象）

YAML vendor extension では表現できないため、コード側で補完する。

| カテゴリ | 補完先 | 根拠ルール |
|---|---|---|
| 認可（`@PreAuthorize`） | Controller メソッド | R-SEC-030 |
| テナント越境防止 | UseCase（取得後にテナント ID 照合） | R-SEC-010、R-SEC-011 |
| 業務ルールバリデーション（相対条件・相関制約） | UseCase 内 `Validator` | R-SEC-020 |
| Mass Assignment 防止 | UseCase（テナント ID / ユーザー ID をリクエストから受け取らない） | R-SEC-060、R-SEC-061 |
| エラーレスポンス情報漏えい防止 | `GlobalExceptionHandler` / UseCase の例外ハンドリング | R-SEC-040、R-SEC-041 |
| Jackson デシリアライズ安全 | `ObjectMapper` 設定 / `@JsonTypeInfo` 使用箇所 | R-SEC-070〜072 |
| CORS / セキュリティヘッダ | `SecurityFilterChain` 設定 | R-SEC-091、R-SEC-092 |
| 機密フィールドの Response 混入防止（actuator 含む） | Response DTO / actuator 設定 | R-SEC-110〜112 |
| 入力バリデーション層の補完 | Controller / Request DTO | R-SEC-021〜024 |
| SQL インジェクション確認 | `@Query` / `EntityManager` 使用箇所 | R-SEC-001〜003 |
| 認証実装安全 | JWT 取得箇所 / シークレット設定 / パスワードハッシュ | R-SEC-031〜033 |
| バリデーションエラー構造化 | `GlobalExceptionHandler` | R-SEC-042 |

将来の要件カテゴリ追加は本テーブルに行を追加し、対応する references ファイルに手順を追記する。

## 前提条件

- `produce` ステージ（`/implement-from-issue`）が完了し、実装コードが feature ブランチに存在すること
- `docs/design/認可設計.md` または `docs/design/セキュリティ設計.md` が存在すること
  （存在しない場合は中断してユーザーに確認する）
- `bash scripts/gen-api-models.sh` が実行可能であること

## 手順

- 手順 3〜7: [references/augmentation-patterns.md](references/augmentation-patterns.md)
- 手順 8〜11: [references/security-patterns.md](references/security-patterns.md)
- 手順 12: [references/security-validation-patterns.md](references/security-validation-patterns.md)
- 手順 13〜15: [references/security-patterns-auth-sql.md](references/security-patterns-auth-sql.md)

### 1. 設計書・認可設計の読み込み

```bash
cat docs/design/認可設計.md 2>/dev/null || cat docs/design/セキュリティ設計.md
```

各 API operationId について「必要ロール・テナント条件・公開 API かどうか」を内部メモに整理する。
認可設計が存在しない場合は**中断**し、ユーザーに設計書の作成を依頼する。

### 2. 生成クラスの確認

```bash
bash scripts/gen-api-models.sh
```

対象フィーチャの Request / Response クラスを特定し、Controller・UseCase・Validator とのマッピングを把握する。

### 2.5. YAML vendor extension の設定確認（自動補完チェック）

対象フィーチャの YAML（`docs/design/api/*.yaml`）を開き、以下を確認する。
設定漏れがあれば docs リポジトリに修正 Issue を起票し、マージ後に再生成してから手順 3 に進む。

```bash
# 対象 YAML で vendor extension の使用状況を確認
grep -n "x-sensitive\|x-validate-\|writeOnly\|format: password" \
  ../claude-poc-docs/docs/design/api/*.yaml
```

チェック項目:

| 確認内容 | 期待される YAML 設定 |
|---|---|
| パスワード・ハッシュ等の機密フィールドに `@JsonProperty(WRITE_ONLY)` が付いているか | `format: password` または `writeOnly: true` |
| PII（電話番号・口座番号等）で `toString()` がマスクされているか | `x-sensitive: true` |
| 「未来であること」系の日時制約が YAML で宣言されているか | `x-validate-future: true` 等 |
| 「正の数であること」系の数値制約が YAML で宣言されているか | `x-validate-positive: true` 等 |

> 上記で自動補完済みの項目は手動対応不要。

### 3. Controller への `@PreAuthorize` 付与

認可設計に従い全 Controller メソッドを確認・付与する。詳細は augmentation-patterns.md 「手順 3」を参照。

### 4. UseCase へのテナント越境防止追加

「取得 → テナント ID 照合 → 404 隠蔽」パターンを確認・追加する（R-SEC-010、R-SEC-011）。詳細は augmentation-patterns.md 「手順 4」を参照。

### 5. UseCase Validator の作成（カスタムバリデーション補完）

設計書の AC-XXX・BR-XXX を確認し、標準制約では不足するカスタム制約を UseCase Validator に実装する。詳細は augmentation-patterns.md 「手順 5」を参照。カスタムバリデーションが不要な場合はスキップし、サマリに記録する。

### 6. Mass Assignment 防止の確認

テナント ID / ユーザー ID / ロールをリクエストから取得していないことを確認する（R-SEC-060、R-SEC-061）。詳細は augmentation-patterns.md 「手順 6」を参照。

### 7. エラーレスポンスの情報漏えい確認

`GlobalExceptionHandler` にスタックトレース・SQL を含めていないことを確認する（R-SEC-040）。詳細は augmentation-patterns.md 「手順 7」を参照。

### 8. PII ログマスクの確認・修正

ログに機密値を直接渡していないこと・DTO の `toString()` でマスクしていることを確認する（R-SEC-050〜051）。詳細は security-patterns.md 「手順 8」を参照。

### 9. Jackson デシリアライズ安全の確認

`enableDefaultTyping` / `JsonTypeInfo.Id.CLASS` の使用と `fail-on-unknown-properties` 設定を確認する（R-SEC-070〜072）。詳細は security-patterns.md 「手順 9」を参照。

### 10. CORS / セキュリティヘッダの確認

`allowedOrigins("*")` の使用とデフォルトセキュリティヘッダの無効化を検出・修正する（R-SEC-091〜092）。詳細は security-patterns.md 「手順 10」を参照。

### 11. 機密フィールドの Response 混入 / actuator の確認

Response DTO へのパスワード・トークン混入と actuator エンドポイントの公開範囲を確認する（R-SEC-110〜112）。詳細は security-patterns.md 「手順 11」を参照。

### 12. 入力バリデーション層の補完（R-SEC-021〜024）

パスパラメータへの制約付与・ネストされた `@Valid`・Controller 内の手動 null チェック除去を確認する。詳細は security-validation-patterns.md 「手順 12」を参照。

### 13. SQL インジェクション確認（R-SEC-001〜003）

`@Query` 内の文字列連結・動的カラム名へのユーザー入力混入を検出・修正する。詳細は security-patterns-auth-sql.md 「手順 13」を参照。

### 14. 認証実装安全確認（R-SEC-031〜033）

JWT 手動パース・シークレットのハードコード・MD5/SHA-1 パスワード保存がないことを確認する。詳細は security-patterns-auth-sql.md 「手順 14」を参照。

### 15. バリデーションエラーの構造化レスポンス（R-SEC-042）

`GlobalExceptionHandler` が `MethodArgumentNotValidException` をフィールド単位で構造化して返しているか確認する。詳細は security-patterns-auth-sql.md 「手順 15」を参照。

### 16. コンパイルとテスト確認

```bash
mvn compile -q && mvn test -q
```

失敗した場合は原因を修正してから次の手順へ進む。

### 17. 補完内容のサマリ出力

[references/completion-summary-template.md](references/completion-summary-template.md) の形式でサマリを出力し、
チェックリストの全項目が ✅ であることを確認する。

## 注意事項

- **生成クラスを直接変更しない**（`mvn generate-sources` で上書きされる）
- 認可設計がない場合は**中断**する。推測で `@PreAuthorize` を付与しない
- 公開 API は設計書で明示的に「認可不要」とされている場合のみ `@PreAuthorize` をスキップする
- UseCase Validator の業務ルールは必ず設計書（BR-XXX / AC-XXX）から根拠を引用する
- 本スキルは `review-implementation` の前に実行し、review で補完漏れを BLOCK として検出できる状態にする
