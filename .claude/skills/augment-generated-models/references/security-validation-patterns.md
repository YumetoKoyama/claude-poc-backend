# 入力バリデーション層の補完パターン

SKILL.md の手順 12 から参照する。

---

## 手順 12: 入力バリデーション層の補完（R-SEC-021〜024）

### バリデーションの層責務確認（R-SEC-021）

Controller に手動の null チェックや業務判断が書かれていないか確認する:

```bash
grep -rn "== null\|!= null\|if.*request\." \
  src/main/java/com/example/logisticsmatching/*/presentation/
```

フォーマット制約は Request DTO の Bean Validation アノテーションで行い、
業務ルール検証は UseCase Validator（手順 5）に委ねる。Controller の if 文は削除する。

### パスパラメータ・クエリパラメータのバリデーション（R-SEC-022）

```bash
grep -rn "@PathVariable\|@RequestParam" \
  src/main/java/com/example/logisticsmatching/*/presentation/
```

`@Positive`・`@NotNull` 等の制約が付いていないパラメータに付与する:

```java
// ✅
@GetMapping("/{jobId}")
public ResponseEntity<JobResponse> get(
        @PathVariable @Positive Long jobId) { ... }
```

### ネストされたオブジェクトへの @Valid 付与（R-SEC-023）

```bash
grep -rn "List\|@NotEmpty" \
  src/main/java/com/example/logisticsmatching/*/presentation/request/
```

List フィールドの要素に制約が必要な場合は `@Valid` を付与する:

```java
public record CreateSetBidRequest(
        @NotEmpty @Valid List<BidItem> items
) {}
```

### HTML タグの除去（R-SEC-024、推奨）

自由記述テキストフィールドが含まれる場合は UseCase Validator でサニタイズする（SUGGEST 扱い）。
