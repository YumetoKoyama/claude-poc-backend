# 認証・SQL インジェクション・バリデーションエラー補完パターン

SKILL.md の手順 13〜15 から参照する。

---

## 手順 13: SQL インジェクション確認（R-SEC-001〜003）

### nativeQuery での文字列連結を検出（R-SEC-001）

```bash
# @Query アノテーション内の文字列連結を確認
grep -rn "@Query.*+" \
  src/main/java/com/example/logisticsmatching/
# EntityManager での動的クエリ組み立てを確認
grep -rn "createNativeQuery\|createQuery" \
  src/main/java/com/example/logisticsmatching/
```

文字列連結が見つかった場合はパラメータバインドに修正する:

```java
// ❌ 文字列連結（SQL インジェクション脆弱性）
String jpql = "SELECT j FROM Job j WHERE j.status = '" + status + "'";

// ✅ パラメータバインド
@Query("SELECT j FROM JobJpaEntity j WHERE j.status = :status")
List<JobJpaEntity> findByStatus(@Param("status") String status);
```

### 動的カラム名への許可リスト（R-SEC-003）

```bash
# ユーザー入力をソートカラム・テーブル名に使っていないか確認
grep -rn "sortColumn\|sortField\|Sort\.by.*request\|Sort\.by.*param" \
  src/main/java/com/example/logisticsmatching/*/presentation/
```

ユーザー入力をソートカラムに使う場合は許可リストで検証する:

```java
private static final Set<String> ALLOWED_SORT_COLUMNS =
        Set.of("createdAt", "price", "status");

public Sort toSort(final String column, final String direction) {
    if (!ALLOWED_SORT_COLUMNS.contains(column)) {
        throw new IllegalArgumentException("Invalid sort column: " + column);
    }
    return Sort.by(Sort.Direction.fromString(direction), column);
}
```

---

## 手順 14: 認証実装安全確認（R-SEC-031〜033）

### JWT 手動パースの禁止確認（R-SEC-031）

```bash
# Authorization ヘッダを手動でパースしていないか確認
grep -rn "getHeader.*Authorization\|substring(7)\|Jwts\.parser" \
  src/main/java/com/example/logisticsmatching/
```

ヒットした場合は `@AuthenticationPrincipal` 経由に修正する:

```java
// ❌ ヘッダから手動パース
String token = request.getHeader("Authorization").substring(7);

// ✅ SecurityContext 経由
@GetMapping("/me")
public ResponseEntity<UserResponse> me(
        @AuthenticationPrincipal CustomUserDetails user) { ... }
```

### JWT シークレットのハードコード禁止（R-SEC-032）

```bash
grep -rn "jwt.*secret\|JWT_SECRET\|secretKey" src/main/resources/
```

`application.yml` にシークレットのデフォルト値が直書きされていないか確認する:

```yaml
# ❌ ハードコード
jwt:
  secret: myHardCodedSecretKey

# ✅ 環境変数から注入
jwt:
  secret: ${JWT_SECRET}
```

### パスワードハッシュ方式の確認（R-SEC-033）

```bash
grep -rn "MD5\|SHA-1\|sha1\|md5\|MessageDigest" \
  src/main/java/com/example/logisticsmatching/
```

MD5・SHA-1 でのパスワード保存がないことを確認する。パスワード保存は `PasswordEncoder`（BCrypt）を使うこと。

---

## 手順 15: バリデーションエラーの構造化レスポンス（R-SEC-042）

`GlobalExceptionHandler` が `MethodArgumentNotValidException` をフィールド単位で構造化して返しているか確認する:

```bash
grep -rn "MethodArgumentNotValidException\|BindingResult" \
  src/main/java/com/example/logisticsmatching/
```

`e.getMessage()` をそのまま返している場合はフィールド単位の構造化に修正する:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(
        final MethodArgumentNotValidException e) {
    List<ErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> {
                ErrorDetail d = new ErrorDetail();
                d.setField(fe.getField());
                d.setMessage(fe.getDefaultMessage());
                return d;
            })
            .toList();
    ErrorResponse res = new ErrorResponse();
    res.setCode("VALIDATION_ERROR");
    res.setDetails(details);
    return ResponseEntity.status(400).body(res);
}
```

`_common.yaml` の `ErrorResponse` スキーマに `details` 配列フィールドが定義されているか確認する。
未定義の場合は `claude-poc-docs` に Issue を起票して実装を中断する（YAML の直接修正禁止）。
