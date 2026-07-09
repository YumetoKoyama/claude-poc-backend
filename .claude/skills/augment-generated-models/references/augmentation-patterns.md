# 補完パターン集

各手順で使う grep コマンドとコードサンプルをまとめる。
SKILL.md の手順 3〜7 から参照する。

---

## 手順 3: @PreAuthorize 付与

```bash
grep -rn "@PreAuthorize\|@PostMapping\|@GetMapping\|@PutMapping\|@DeleteMapping\|@PatchMapping" \
  src/main/java/com/example/logisticsmatching/<feature>/presentation/
```

- 認可が必要: 認可設計の role 列に従って `@PreAuthorize("hasRole('ROLE_NAME')")` を付与
- 公開 API: `// public endpoint - no auth required` コメントを明示
- 判断できない: 中断してユーザーに確認

```java
@PreAuthorize("hasRole('SHIPPER')")
@PostMapping
public ResponseEntity<JobResponse> create(
        @Valid @RequestBody CreateJobRequest request,
        @AuthenticationPrincipal CustomUserDetails user) { ... }
```

---

## 手順 4: テナント越境防止

```bash
grep -rn "findById\|findBy\|TenantId\|tenantId" \
  src/main/java/com/example/logisticsmatching/<feature>/application/
```

テナント照合が抜けている UseCase に追加する（テナント越境は 404 で返す — R-SEC-041）:

```java
Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new NotFoundException("Job not found"));
if (!job.getTenantId().equals(currentTenantId)) {
    throw new NotFoundException("Job not found");  // 403 ではなく 404 で存在を隠す
}
```

---

## 手順 5: UseCase Validator（カスタムバリデーション補完）

> **YAML vendor extension で対応済みの制約は UseCase Validator を作らない。**
> YAML に `x-validate-future: true` が付いていれば `@Future` は生成クラスに自動付与されている。
> 手順 2.5 の確認で「YAML 設定済み」になった項目はスキップする。

UseCase Validator の補完対象（YAML では表現できない業務ルール）:
- **相対時間条件**（「現在から N 時間以上先」など `@Future` では表現できない）
- 業務上の範囲制約（金額の下限・上限が BR-XXX に記載、YAML の `minimum`/`maximum` と異なる場合）
- **相関バリデーション**（フィールド A と B の組み合わせルール）

```java
@Component
public class Create<Feature>Validator {
    private final Clock clock;

    public Create<Feature>Validator(final Clock clock) {
        this.clock = clock;
    }

    public void validate(final <Feature>CreateRequest req) {
        // 業務ルール BR-XXX: 積込日時は現在から2時間以上先
        if (!req.getFromDatetime().isAfter(OffsetDateTime.now(clock).plusHours(2))) {
            throw new ValidationException("fromDatetime は現在時刻の2時間後以降を指定してください");
        }
    }
}
```

UseCase 冒頭で Validator を呼ぶ:

```java
@Transactional
public <Feature>Response execute(
        final <Feature>CreateRequest req, final TenantId tenantId) {
    validator.validate(req);
    // 実装本体 ...
}
```

カスタムバリデーションが不要な場合はスキップし、その旨をサマリに記録する。

---

## 手順 6: Mass Assignment 防止

```bash
grep -rn "request\.\(get\)\?[Tt]enant\|request\.\(get\)\?[Uu]ser[Ii]d\|request\.\(get\)\?[Rr]ole" \
  src/main/java/com/example/logisticsmatching/<feature>/application/
```

リクエストから取得している箇所は `@AuthenticationPrincipal` 経由に修正する:

```java
@PreAuthorize("hasRole('SHIPPER')")
@PostMapping
public ResponseEntity<JobResponse> create(
        @Valid @RequestBody CreateJobRequest request,
        @AuthenticationPrincipal CustomUserDetails user) {
    TenantId tenantId = user.getTenantId();  // リクエストではなく認証情報から
    return ResponseEntity.ok(useCase.execute(request, tenantId));
}
```

---

## 手順 7: エラーレスポンス情報漏えい確認

```bash
grep -rn "e\.getMessage\(\)\|e\.getStackTrace\(\)\|e\.toString\(\)" \
  src/main/java/com/example/logisticsmatching/
```

スタックトレース・SQL・テーブル名・内部クラス名をレスポンスに含めている箇所を修正する:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleUnexpected(final Exception e) {
    log.error("Unexpected error", e);   // ログには詳細を残す
    ErrorResponse res = new ErrorResponse();
    res.setCode("INTERNAL_ERROR");
    res.setMessage("内部エラーが発生しました");
    return ResponseEntity.status(500).body(res);
}
```
