# 補完内容サマリ テンプレート

手順 17 で出力するサマリの形式。

```
## augment-generated-models 完了サマリ（Issue #<N>）

| 要件カテゴリ | 対処 | 対象クラス / 根拠 |
|---|---|---|
| @PreAuthorize | 付与済み / 既存確認済み | <クラス名> / 認可設計 operationId |
| テナント越境防止 | 実装済み / 確認済み | <UseCase 名> / R-SEC-010 |
| UseCase Validator | 新規作成: <Validator 名> / 不要（標準制約で充足） | <Validator 名> / BR-XXX |
| Mass Assignment 防止 | 確認済み | <UseCase 名> / R-SEC-060 |
| エラーレスポンス | 確認済み / 修正済み | GlobalExceptionHandler / R-SEC-040 |
| PII ログマスク | 確認済み / 修正済み | <クラス名> / R-SEC-050 |
| Jackson 安全設定 | 確認済み / 修正済み | ObjectMapper 設定 / R-SEC-070 |
| CORS / ヘッダ | 確認済み / 修正済み | SecurityFilterChain / R-SEC-091 |
| 機密フィールド / actuator | 確認済み / 修正済み | <Response 名> / R-SEC-110 |
| バリデーション層補完 | 確認済み / 修正済み | <Controller 名> / R-SEC-021 |
| SQL インジェクション | 確認済み / 修正済み | <Repository 名> / R-SEC-001 |
| 認証実装安全 | 確認済み / 修正済み | <Controller 名> / R-SEC-031 |
| バリデーションエラー構造化 | 確認済み / 修正済み | GlobalExceptionHandler / R-SEC-042 |
```

## チェックリスト

- [ ] 全 Controller メソッドに `@PreAuthorize` または `// public endpoint` コメントがある
- [ ] テナント ID を扱う UseCase は「取得 → テナント照合（404 隠蔽）」パターンになっている
- [ ] カスタムバリデーションは UseCase Validator に実装済み（不要な場合はその旨を記録）
- [ ] テナント ID / ユーザー ID はリクエストではなく SecurityContext から取得している
- [ ] エラーレスポンスに内部情報（スタックトレース・SQL）が含まれていない
- [ ] ログにパスワード・メールアドレス・電話番号・トークンを直接渡していない
- [ ] DTO の `toString()` で機密フィールドをマスクしている（または Lombok `@ToString.Exclude`）
- [ ] `enableDefaultTyping` / `JsonTypeInfo.Id.CLASS` を使用していない
- [ ] `allowedOrigins("*")` を使用していない
- [ ] Response DTO にパスワード・トークン等の機密フィールドが含まれていない
- [ ] `@Query` 内に文字列連結がなく、パラメータバインドを使用している
- [ ] JWT を Authorization ヘッダから手動パースしていない
- [ ] シークレットが `application.yml` にハードコードされていない
- [ ] `GlobalExceptionHandler` がバリデーションエラーをフィールド単位で構造化して返している
- [ ] `mvn compile -q && mvn test -q` が成功している
- [ ] `generated/` 配下のファイルを変更していない
