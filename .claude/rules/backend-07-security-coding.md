# バックエンド実装ルール 07 — セキュリティコーディング規約

> 本規約は OWASP Top 10 / OWASP ASVS をベースに、本プロジェクト（Java 25 / Spring Boot 3.5 / Spring Security / Spring Data JPA / PostgreSQL 16）で
> 実装時に守るべきセキュリティコーディングルールをまとめたものである。
> 認証・認可の設計方針（JWT ライフサイクル・CORS・`@PreAuthorize` 規約等）は `docs/design/セキュリティ設計.md` を正典とし、本ファイルでは再掲しない。

## 凡例

| 略号 | 正式名称 | 補足 |
|------|----------|------|
| R-SEC-XXX | セキュリティコーディングルール ID | 本ファイル固有の連番 |

---

## 0. 前提 — OpenAPI 生成クラスとベンダー拡張による自動付与

presentation 層の Request/Response DTO は OpenAPI YAML から生成したクラス（`generated.openapi.*`）を使用する（`backend-02` / `backend-04` / `backend-00` #16）。**生成クラスは再生成で上書きされるため、本規約のアノテーションを生成クラスへ直接付け足さない。** 付与方法は次の 2 系統に分かれる。

### (a) YAML ベンダー拡張で自動付与する（YAML 作成者の責務）

`pojo.mustache` カスタムテンプレートにより、YAML の下記宣言から `mvn generate-sources` でアノテーションが自動生成される。該当フィールドには YAML 側で必ず宣言する（コード側へ手書きしない）。

| YAML 宣言 | 自動生成されるアノテーション | 対応ルール |
|---|---|---|
| `format: password` / `writeOnly: true` / `x-sensitive: true` | getter に `@JsonProperty(access = WRITE_ONLY)` + `toString()` で `[MASKED]` | R-SEC-050 / R-SEC-051 / R-SEC-110 / R-SEC-111 |
| `x-validate-future` / `-future-or-present` / `-past` / `-past-or-present` | `@Future` / `@FutureOrPresent` / `@Past` / `@PastOrPresent` | R-SEC-020 |
| `x-validate-not-blank` | `@NotBlank` | R-SEC-020 |
| `x-validate-positive` / `-positive-or-zero` / `-negative` | `@Positive` / `@PositiveOrZero` / `@Negative` | R-SEC-020 |
| 標準キーワード `minLength` / `maxLength` / `minimum` / `maximum` / `pattern` | `@Size` / `@Min` / `@Max` / `@Pattern` | R-SEC-020 |

### (b) コード側で補完する（生成クラスに表現できない要件）

YAML で表現できない要件は生成クラスを変更せず Controller / UseCase / Validator で補完する（`backend-02` presentation 層）。対象: 認可 `@PreAuthorize`（R-SEC-030）/ テナント越境防止（R-SEC-010・R-SEC-011）/ 相対条件・相関制約の業務バリデーション（R-SEC-020）/ Mass Assignment 防止（R-SEC-060・R-SEC-061）/ エラーレスポンスの情報漏えい防止（R-SEC-040）。

> 以降の各ルールは、生成クラスを使う場合は上表 (a) の YAML 宣言で満たし、手書き DTO（OpenAPI 未定義の内部 DTO）の場合は従来どおりコードに直接アノテーションを付与する。

---

## 1. SQL インジェクション対策

### R-SEC-001: SQL 文に文字列連結でパラメータを埋め込まない（必須）

Spring Data JPA のリポジトリメソッド、`@Query`、`Criteria API`、`Specification` を使う。
`EntityManager.createNativeQuery()` を使う場合は必ずパラメータバインドを使う。

```java
// ✅ パラメータバインド
@Query("SELECT j FROM JobJpaEntity j WHERE j.status = :status")
List<JobJpaEntity> findByStatus(@Param("status") String status);

// ✅ ネイティブクエリでもバインド変数
@Query(value = "SELECT * FROM jobs WHERE tenant_id = :tenantId", nativeQuery = true)
List<JobJpaEntity> findByTenant(@Param("tenantId") Long tenantId);

// ❌ 文字列連結（SQLインジェクション脆弱性）
@Query("SELECT j FROM JobJpaEntity j WHERE j.status = '" + status + "'")
```

### R-SEC-002: 動的クエリ構築には Criteria API / Specification を使う（必須）

検索条件の動的組み立てに文字列連結を使わない。

```java
// ✅ Specification
public static Specification<JobJpaEntity> hasStatus(JobStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
}

// ❌ 文字列で組み立て
String jpql = "SELECT j FROM Job j WHERE 1=1";
if (status != null) {
    jpql += " AND j.status = '" + status + "'";
}
```

### R-SEC-003: ユーザー入力をテーブル名・カラム名に使わない（必須）

ORDER BY のカラム名等をユーザー入力から受け取る場合は、許可リスト（ホワイトリスト）で検証する。

```java
// ✅ 許可リストで検証
private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("createdAt", "price", "status");

public Sort toSort(String column, String direction) {
    if (!ALLOWED_SORT_COLUMNS.contains(column)) {
        throw new IllegalArgumentException("Invalid sort column: " + column);
    }
    return Sort.by(Sort.Direction.fromString(direction), column);
}
```

---

## 2. テナント越境・IDOR 防止

### R-SEC-010: データアクセスには必ずテナント ID でフィルタする（必須）

全てのデータ取得・更新・削除クエリにテナント条件を含める。
パス変数やリクエストボディの ID だけで直接データを取得してはならない。

```java
// ✅ テナント ID でフィルタ
Optional<JobJpaEntity> findByIdAndTenantId(Long id, Long tenantId);

// ❌ ID のみで取得（テナント越境の脆弱性）
Optional<JobJpaEntity> findById(Long id);
```

### R-SEC-011: パス変数の ID を信用しない（必須）

`/api/jobs/{jobId}` のようなパスパラメータの ID が、認証済みユーザーのテナントに属するデータかを必ず検証する。
UseCase 層で「取得 → テナント一致確認」を行う。

```java
// ✅ UseCase でテナント検証
public JobResponse getJob(Long jobId, TenantId currentTenantId) {
    Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new NotFoundException("Job not found"));
    if (!job.getTenantId().equals(currentTenantId)) {
        throw new ForbiddenException("Access denied");
    }
    return toResponse(job);
}
```

### R-SEC-012: 他テナントのリソース ID を推測可能にしない（推奨）

連番 ID が外部に露出する場合、テナント越境テスト（ID ±1 でアクセス）を必ず実施する。
可能であれば UUID を外部公開 ID に使用する。

---

## 3. 入力バリデーション

### R-SEC-020: Request DTO に Bean Validation アノテーションを付与する（必須）

Controller の引数に `@Valid` / `@Validated` を付け、DTO 側で制約を宣言する。

```java
// ✅ DTO にバリデーション
public record CreateJobRequest(
        @NotBlank @Size(max = 100) String title,
        @NotNull @Positive Long price,
        @NotNull @Future LocalDateTime fromDate
) {}

// Controller
@PostMapping
public ResponseEntity<JobResponse> create(
        @Valid @RequestBody CreateJobRequest request) { ... }
```

> 生成クラスを使う場合、標準制約は `minLength` 等の OpenAPI キーワード、`@Future` / `@NotBlank` / `@Positive` 等は `x-validate-*` ベンダー拡張で YAML に宣言し自動付与する（§0(a)）。「現在から 2 時間以上先」のような相対条件・相関制約は YAML では表現できないため UseCase 内 `Validator` で実装する（§0(b)）。

### R-SEC-021: バリデーションの本体は presentation 層に閉じる（必須）

`@NotBlank`, `@Size` 等のフォーマットバリデーションは Request DTO に、
業務ルールバリデーション（ステータス遷移可否等）は domain 層のメソッドで行う。
Controller に `if (request.getTitle() == null)` のような手動チェックを書かない。

### R-SEC-022: パスパラメータ・クエリパラメータも型と範囲を検証する（必須）

```java
// ✅
@GetMapping("/{jobId}")
public ResponseEntity<JobResponse> get(
        @PathVariable @Positive Long jobId) { ... }
```

### R-SEC-023: ネストされたオブジェクトにも `@Valid` を付ける（必須）

```java
public record CreateSetBidRequest(
        @NotEmpty @Valid List<BidItem> items
) {}
```

### R-SEC-024: 入力値のサニタイズ — HTML タグの除去（推奨）

ユーザー入力のテキストフィールドは、保存前に HTML タグを除去またはエスケープする。
XSS はフロントエンド側の責務が主だが、永続化時の防御層として実施する。

---

## 4. 認証・認可の実装ルール

### R-SEC-030: Controller メソッドに `@PreAuthorize` を明示する（必須）

認可不要な公開 API を除き、全 Controller メソッドに認可アノテーションを付ける。
認可アノテーションの無い API エンドポイントは、レビューで BLOCK とする。

```java
// ✅
@PreAuthorize("hasRole('SHIPPER')")
@PostMapping
public ResponseEntity<JobResponse> create(...) { ... }
```

### R-SEC-031: 認証情報の取得は SecurityContext 経由に統一する（必須）

リクエストヘッダから直接 JWT をパースしない。
`SecurityContextHolder` または `@AuthenticationPrincipal` で認証済み情報を取得する。

```java
// ✅
@GetMapping("/me")
public ResponseEntity<UserResponse> me(
        @AuthenticationPrincipal CustomUserDetails user) { ... }

// ❌ ヘッダから手動パース
String token = request.getHeader("Authorization").substring(7);
Claims claims = Jwts.parser()...
```

### R-SEC-032: JWT シークレット / API キーをソースコードにハードコードしない（必須）

環境変数または Spring の外部設定（`application.yml` + Secrets Manager）から注入する。
`application.yml` にシークレットのデフォルト値を書かない。

```yaml
# ❌
jwt:
  secret: myHardCodedSecretKey12345

# ✅
jwt:
  secret: ${JWT_SECRET}
```

### R-SEC-033: パスワードは BCrypt 等の単方向ハッシュで保存する（必須）

平文・MD5・SHA-1 での保存は禁止。`PasswordEncoder`（Spring Security 標準）を使う。

---

## 5. エラーハンドリングとレスポンス

### R-SEC-040: 例外メッセージに内部情報を含めない（必須）

スタックトレース、SQL 文、テーブル名、内部クラス名をクライアントに返さない。
`@RestControllerAdvice` で統一的にエラーレスポンスを生成する。

```java
// ✅ 共通エラーハンドラ
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("Unexpected error", e);  // ログには詳細を出す
    return ResponseEntity.status(500)
            .body(new ErrorResponse("INTERNAL_ERROR", "内部エラーが発生しました"));
}

// ❌ スタックトレースをそのまま返す
return ResponseEntity.status(500).body(e.getMessage());
```

### R-SEC-041: 認証・認可エラーで存在情報を漏らさない（必須）

「ユーザーが存在しない」と「パスワードが違う」を区別するメッセージを返さない。
テナント越境アクセス時は 403 ではなく 404 を返す（リソースの存在を隠す）。

```java
// ✅ 存在を隠す
throw new NotFoundException("Resource not found");

// ❌ 権限不足を明示（リソースの存在がバレる）
throw new ForbiddenException("You don't have permission to access job #123");
```

### R-SEC-042: バリデーションエラーは入力項目ごとに構造化して返す（推奨）

`MethodArgumentNotValidException` をハンドルし、フィールド名とメッセージの配列で返す。

---

## 6. ログの機密情報保護

### R-SEC-050: パスワード・トークン・個人情報をログに出力しない（必須）

```java
// ❌
log.info("Login attempt: user={}, password={}", username, password);
log.debug("JWT token: {}", token);
log.info("User registered: email={}, phone={}", email, phone);

// ✅
log.info("Login attempt: user={}", username);
log.debug("JWT token issued for user={}", username);
log.info("User registered: userId={}", userId);
```

### R-SEC-051: Request/Response のログ出力時に機密フィールドをマスクする（必須）

`toString()` に機密フィールドを含めない。ログ出力用の専用メソッドやフィルタを使う。

```java
// ✅ toString でマスク
@Override
public String toString() {
    return "LoginRequest{username='" + username + "', password='***'}";
}
```

> 生成クラスでは `format: password` / `writeOnly: true` / `x-sensitive: true` を YAML に宣言すると `toString()` のマスク（`[MASKED]`）が自動生成される（§0(a)）。生成クラスへ `toString()` を手書きしない。

### R-SEC-052: 例外ログにはリクエスト ID / トレース ID を含める（推奨）

障害調査でリクエストを追跡できるように、MDC（Mapped Diagnostic Context）を活用する。

---

## 7. Mass Assignment（一括代入）防止

### R-SEC-060: Request DTO と Entity / Domain Model を直接マッピングしない（必須）

リクエストの全フィールドを Entity にそのままコピーすると、意図しないフィールド（ロール・テナント ID 等）を書き換えられる。
DTO → Domain Model の変換では、許可するフィールドのみを明示的にマッピングする。

```java
// ✅ 必要なフィールドのみ明示的に設定
Job job = Job.create(
        request.title(),
        request.price(),
        request.fromDate(),
        currentTenantId  // テナント ID はリクエストからではなく認証情報から
);

// ❌ BeanUtils で全フィールドコピー
BeanUtils.copyProperties(request, jobEntity);
```

### R-SEC-061: テナント ID・ユーザー ID・ロールはリクエストから受け取らない（必須）

これらは認証情報（SecurityContext）から取得する。リクエストボディやパスパラメータで受け取った値を信用しない。

---

## 8. 依存ライブラリ・シリアライゼーション

### R-SEC-070: Jackson のデシリアライゼーションで不要な型を受け入れない（必須）

`@JsonTypeInfo` を使う場合は `JsonTypeInfo.Id.NAME`（ホワイトリスト）を使い、`JsonTypeInfo.Id.CLASS` は使わない。

### R-SEC-071: `ObjectMapper` のデフォルト型情報付与を有効にしない（必須）

```java
// ❌ デシリアライゼーション攻撃の危険
mapper.enableDefaultTyping();
mapper.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL);
```

### R-SEC-072: 未知のプロパティは無視する（推奨）

```java
// ✅ application.yml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: false
```

---

## 9. ファイルアップロード（該当する場合）

### R-SEC-080: アップロードファイルのサイズ上限を設定する（必須）

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### R-SEC-081: ファイル名をそのまま保存パスに使わない（必須）

パストラバーサル攻撃を防ぐため、保存時は UUID 等で生成したファイル名を使う。

```java
// ✅
String savedName = UUID.randomUUID() + getExtension(file.getOriginalFilename());

// ❌
String savedName = file.getOriginalFilename(); // "../../etc/passwd" 等が来る
```

### R-SEC-082: Content-Type をクライアント申告だけで信用しない（推奨）

ファイルのマジックバイトで実際の形式を検証する。

---

## 10. CSRF・CORS・セキュリティヘッダ

### R-SEC-090: REST API（JWT 認証）では CSRF 保護は無効化してよい（確認事項）

Cookie ベース認証を使わない前提。設計書（`セキュリティ設計.md`）の方針に従う。

### R-SEC-091: CORS の許可オリジンをワイルドカード（`*`）にしない（必須）

```java
// ✅ 明示的にオリジンを指定
.allowedOrigins("https://app.example.com")

// ❌
.allowedOrigins("*")
```

### R-SEC-092: セキュリティレスポンスヘッダを設定する（推奨）

Spring Security のデフォルト（`X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security` 等）を無効化しない。
追加で必要なヘッダがあれば `SecurityFilterChain` で設定する。

---

## 11. トランザクションとデータ整合性

### R-SEC-100: 認可チェックとデータ操作を同一トランザクション内で行う（必須）

TOCTOU（Time-of-check to time-of-use）を防ぐため、権限チェックとデータ更新の間にトランザクション境界を置かない。

```java
// ✅ UseCase 内の @Transactional で一括
@Transactional
public void deleteJob(Long jobId, TenantId tenantId) {
    Job job = jobRepository.findByIdAndTenantId(jobId, tenantId)
            .orElseThrow(() -> new NotFoundException("Job not found"));
    job.markDeleted();
    jobRepository.save(job);
}
```

### R-SEC-101: 楽観ロック（`@Version`）で同時更新競合を検出する（推奨）

ステータス遷移や金額変更など、競合が致命的な操作には楽観ロックを適用する。

---

## 12. 機密データの取り扱い

### R-SEC-110: パスワード・トークンを Response DTO に含めない（必須）

### R-SEC-111: 機密フィールドに `@JsonIgnore` を付与する（必須）

JPA Entity が万一レスポンスに混入した場合の防御層として。

```java
@JsonIgnore
private String passwordHash;
```

> 生成 Response DTO では `format: password` / `writeOnly: true` を YAML に宣言すると getter に `@JsonProperty(access = WRITE_ONLY)` が自動付与され、レスポンスから除外される（§0(a)）。上記 `@JsonIgnore` は手書き JPA Entity / 手書き DTO に対する防御層として適用する。

### R-SEC-112: デバッグ用エンドポイントを本番プロファイルで無効化する（必須）

`/actuator` 等の管理エンドポイントは認証必須または非公開にする。

---

## 13. 依存ライブラリの脆弱性管理

### R-SEC-120: 既知の脆弱性がある依存ライブラリを使用しない（必須）

`mvn dependency:tree` や OWASP Dependency-Check 等で定期的に確認する。
Spring Boot BOM で管理される依存は BOM のバージョンアップで対応する。
