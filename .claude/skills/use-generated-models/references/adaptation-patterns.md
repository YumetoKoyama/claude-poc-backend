# 差異パターンと対処一覧

## YAML 修正が必要な差異（docs Issue 起票 → 実装中断）

これらは設計変更に該当するため、**実装者が YAML を直接修正してはならない**。
`claude-poc-docs` に Issue を起票し、採択されるまで実装を止める。

| 差異パターン | 例 | Issue 記載内容 |
|---|---|---|
| フィールド名の表記ゆれ | `fromDateTime`（設計想定）vs `fromDatetime`（YAML） | どの API・フィールドで発生したか、Java 規約に合わせた修正案 |
| 数値型の不一致 | `Long jobId`（設計想定）vs `Integer jobId`（生成） | `format: int64` 追加で解消可能な旨を記載 |
| enum 値の不一致 | `CargoType.GENERAL` vs YAML の `General` | ドメイン enum に合わせた YAML enum 値の修正案 |
| 必須フィールドの欠落 | 実装に必要だが YAML に無いフィールド | フィールドの業務要件・追加理由を記載 |
| レスポンス構造の乖離 | 設計書にあるが生成に無いフィールド | 設計書のどの仕様に対応するかを記載 |

### Issue 起票テンプレート

```
タイトル: [API修正] <YAML ファイル名> - <差異の概要>

## 背景
`use-generated-models` スキル実行中に以下の差異を発見した。

## 差異の詳細
- 対象 YAML: `docs/design/api/<file>.yaml`
- 対象フィールド / スキーマ: `<schema name>.<field name>`
- 設計想定 / 手書き実装: `<現状の定義>`
- 生成クラス: `<生成クラスの定義>`

## 修正案
<具体的な YAML 修正内容>

## 影響範囲
- バックエンド: `<影響クラス>`
- フロントエンド: 同 YAML から自動生成されるため型変更が必要
```

---

## YAML 修正不要の差異（実装継続可能）

以下は YAML を変更せずに実装層で対処できる。

### カスタムバリデーション不足

生成クラスのフィールドに `@FutureDatetime` 等のカスタム制約が付かない。

**対処 A — UseCase 内 Validator 呼び出し**:
```java
@Component
public class CreateJobValidator {
    public void validate(JobCreateRequest req) {
        if (!req.getFromDatetime().isAfter(OffsetDateTime.now())) {
            throw new ValidationException("fromDatetime は未来日時を指定してください");
        }
    }
}
```

**対処 B — Controller で `@Validated` グループを使用**:
`@Valid` の代わりに `@Validated(CreateJobGroup.class)` を指定し、グループ付きの制約を別クラスで定義する。

### ドメイン enum ↔ 生成 inline enum の変換

`EnumMapper` を使って Controller 境界で変換する。詳細は [enum-mapping-guide.md](enum-mapping-guide.md) 参照。

### 不変 Response → 可変 Response（置き換え時のみ）

手書き Response は `final` フィールド + 全引数コンストラクタが多い。
生成クラスは setter ベースのため、組み立て方を変える必要がある。

```java
// 手書き（不変）
return new JobResponse(job.getId(), job.getFromLocation(), ...);

// 生成クラス（可変・setter）
JobResponse res = new JobResponse();
res.setFromLocation(job.getFromLocation());
return res;
```

Long → Integer 変換が必要な場合は型の不一致であるため docs Issue 起票の対象。
