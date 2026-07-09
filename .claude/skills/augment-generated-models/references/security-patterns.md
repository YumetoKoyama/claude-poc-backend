# セキュリティ補完パターン集

SKILL.md の手順 8〜11 から参照する。
手順 12（バリデーション層）は security-validation-patterns.md を参照。
既存の補完パターン（手順 3〜7）は augmentation-patterns.md を参照。

---

## 手順 8: PII ログマスク（R-SEC-050〜051）

### grep で疑いのある箇所を検出

```bash
# パスワード・メールアドレス・電話番号・トークン類をログに渡していないか確認
grep -rn "log\.\(info\|debug\|warn\|error\).*\(password\|email\|phone\|token\|address\)" \
  src/main/java/com/example/logisticsmatching/
```

ヒットした箇所を確認し、機密値を直接渡しているなら修正する:

```java
// ❌ 直接渡している例
log.info("Login attempt: user={}, password={}", username, password);

// ✅ 修正後
log.info("Login attempt: user={}", username);
```

### toString でのマスク（R-SEC-051）

DTO / Entity の `toString()` に機密フィールドを含めない:

```java
@Override
public String toString() {
    return "LoginRequest{username='" + username + "', password='***'}";
}
```

Lombok の `@ToString` を使う場合は `@ToString.Exclude` で除外する:

```java
@ToString.Exclude
private String password;
```

### MDC によるリクエスト ID 付与（R-SEC-052、推奨）

```java
// Filter か Interceptor で設定
MDC.put("requestId", UUID.randomUUID().toString());
```

---

## 手順 9: Jackson デシリアライズ安全（R-SEC-070〜072）

### grep で危険な設定を検出

```bash
grep -rn "enableDefaultTyping\|activateDefaultTyping\|JsonTypeInfo\.Id\.CLASS" \
  src/main/java/com/example/logisticsmatching/
```

ヒットした場合は即修正する。`@JsonTypeInfo` を使う箇所は `Id.NAME`（ホワイトリスト）になっているか確認する:

```java
// ❌ 危険
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)

// ✅ 安全
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
```

### fail-on-unknown-properties の確認（R-SEC-072）

```bash
grep -rn "fail-on-unknown-properties" src/main/resources/
```

設定がない場合は `false` に明示する:

```yaml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: false
```

---

## 手順 10: CORS / セキュリティヘッダ（R-SEC-091〜092）

### CORS のワイルドカード検出

```bash
grep -rn "allowedOrigins\|allowedOrigin" \
  src/main/java/com/example/logisticsmatching/
```

`allowedOrigins("*")` があれば修正する（R-SEC-091）:

```java
// ❌
.allowedOrigins("*")
// ✅
.allowedOrigins("https://app.example.com")
```

### Spring Security デフォルトヘッダの無効化禁止（R-SEC-092）

```bash
grep -rn "headers()\.disable\|\.headers(h -> h\.disable" \
  src/main/java/com/example/logisticsmatching/
```

`X-Content-Type-Options`・`X-Frame-Options`・`Strict-Transport-Security` 等の無効化箇所があれば戻す。

---

## 手順 11: 機密フィールドの Response 混入防止 / actuator（R-SEC-110〜112）

### Response DTO へのパスワード・トークン混入確認（R-SEC-110〜111）

```bash
grep -rn "password\|passwordHash\|token\|secret" \
  src/main/java/com/example/logisticsmatching/*/presentation/response/ \
  src/main/java/com/example/logisticsmatching/*/presentation/request/ 2>/dev/null
```

Response DTO に機密フィールドが含まれる場合は削除する。
JPA Entity が万一レスポンスに混入したときの防御として `@JsonIgnore` を付与する（R-SEC-111）:

```java
@JsonIgnore
private String passwordHash;
```

### actuator エンドポイントの保護確認（R-SEC-112）

```bash
grep -rn "management\.endpoints\|management\.endpoint" src/main/resources/
```

本番プロファイルで全エンドポイントを公開している場合は制限する:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health  # 必要最小限のみ公開
```
