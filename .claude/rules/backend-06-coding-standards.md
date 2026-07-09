# バックエンド実装ルール 06 — Java コーディング規約

> 本規約は [フューチャー株式会社 Java コーディング規約](https://future-architect.github.io/coding-standards/documents/forJava/Java%E3%82%B3%E3%83%BC%E3%83%87%E3%82%A3%E3%83%B3%E3%82%B0%E8%A6%8F%E7%B4%84.html) をベースに、
> 本プロジェクト（Java 25 / Spring Boot 3.5 / モジュラモノリス）向けに取捨選択・要約したものである。
> クラス・パッケージの配置ルールや命名は `backend-01` 〜 `backend-05` を正典とし、本ファイルでは再掲しない。

## 凡例

| 略号 | 正式名称 | 補足 |
|------|----------|------|
| R-CODE-XXX | コーディング規約ルール ID | 本ファイル固有の連番 |

---

## 1. ネーミング規約

### R-CODE-001: 大文字・小文字の違いだけで名前を区別しない

```java
// ✅
private int carNumber;
private int trainNumber;

// ❌
private int num;
private int Num;
```

### R-CODE-002: メソッド名は camelCase、単語区切りのみ大文字

変換メソッドは `to` + オブジェクト名（例: `toString()`）。
getter は `get` + 属性名、boolean は `is` + 属性名。
setter は `set` + 属性名。

### R-CODE-003: boolean 変数・メソッドは true/false の状態がわかる名前にする

```java
// ✅
private boolean isOpen;
public boolean hasExpired() { ... }

// ❌
private boolean flag;
```

### R-CODE-004: 定数は `static final`、全て大文字、区切りは `_`

```java
private static final String SYSTEM_NAME = "物流マッチングシステム";
```

### R-CODE-005: 変数名は camelCase、固有名詞は先頭除き大文字可

```java
private String thisIsIPAddress;  // OK
```

### R-CODE-006: スコープが狭いローカル変数は省略名でもよい

ただし広いスコープで使う場合は役割が明確になる名前にする。
for ループカウンタはネストごとに `i`, `j`, `k` を使う。

### R-CODE-007: Enum 定数は全て大文字、区切りは `_`

```java
enum Season {
    WINTER, SPRING, SUMMER, FALL
}
```

### R-CODE-008: メソッドのパラメータ名とインスタンス変数名を一緒にしない

アクセサやコンストラクタの自動生成は可。アンダースコア `_` での区別は原則禁止。

```java
// ✅
public double calc(double rate) {
    return this.value * rate;
}
```

---

## 2. フォーマット

### R-CODE-010: インデントは空白 4 文字分の Tab

### R-CODE-011: `{` の後にステートメントを書かない、1 行に 2 つ以上のステートメントを書かない

### R-CODE-012: 演算子の前後・カンマの後に空白を入れる

対象: 代入（`=`, `+=`）、算術（`+`, `-`）、関係（`<`, `>=`）、論理（`||`, `&&`）、ビット。
例外: `++`, `--` とオペランドの間には空白を入れない。

### R-CODE-013: if 条件式で boolean を `true`/`false` と比較しない

```java
// ✅
if (hasStock)

// ❌
if (hasStock == true)
```

### R-CODE-014: 不等号の向きは左向き（`<`, `<=`）に統一

```java
// ✅
if (from <= x && x <= to)
```

### R-CODE-015: return 文でカッコを使わない

---

## 3. コメント・Javadoc

### R-CODE-020: Javadoc コメントに `@author`, `@param`, `@return`, `@throws` を記述する

`@Override` メソッドでは `{@inheritDoc}` を記述する。

### R-CODE-021: コメントは必要なものだけを簡潔に

コードから自明な内容、名前の説明、ソース管理システムで追跡できる情報（変更履歴）は書かない。
コメントアウトされたコードは残さない。

### R-CODE-022: 「なぜそうなのか」を書く

コードを読めばわかる「何をしているか」ではなく、ビジネスルールや設計判断の背景を記述する。

### R-CODE-023: TODO コメントの形式

```java
// TODO: ワークフローの仕様決定待ち 関連チケット#12345
```

### R-CODE-024: ファイルヘッダの Copyright は不要

顧客からの要求がある場合を除く。

---

## 4. インポート

### R-CODE-030: ワイルドカードインポート（`*`）禁止

```java
// ❌
import java.util.*;
```

### R-CODE-031: static インポートは原則禁止

例外: JUnit・AssertJ のアサーションなどテストフレームワークが推奨するもの。

---

## 5. クラス・メソッド

### R-CODE-040: インスタンスメンバを持たないクラスは private コンストラクタを作成する

### R-CODE-041: 戻り値がコレクション・配列のメソッドで null を返さない

空の場合は `Collections.emptyList()`, `List.of()`, 長さ 0 の配列を返す。

### R-CODE-042: メソッドは 1 つの役割にする

### R-CODE-043: クラスメソッドはクラス名で呼び出す

```java
// ✅
int comp = Integer.compare(x, y);

// ❌
Integer a = ...;
int comp = a.compare(x, y);
```

### R-CODE-044: `@Override` アノテーションを必ず付ける

### R-CODE-045: メンバーの記述順序

1. static フィールド → 2. static イニシャライザ → 3. static メソッド → 4. フィールド → 5. イニシャライザ → 6. コンストラクタ → 7. メソッド。
同一カテゴリ内は `public` → `protected` → パッケージ private → `private` の順。

### R-CODE-046: `final` を適切に利用する

継承されないクラス、オーバーライドされないメソッド、変更しない変数は `final` で宣言する。
メソッド引数は原則 `final` で宣言する。

---

## 6. 変数・定数

### R-CODE-050: 1 ステートメント 1 変数宣言

### R-CODE-051: リテラル（マジックナンバー）を使用しない

`static final` 定数に意味のある名前を付けて使う。例外: ループの `0`, `1`, `-1`。

```java
// ✅
private static final double ONE_MILE_METRE = 1609.344;

// ❌ 定数名が値そのものを表すだけのもの
private static final int ZERO = 0;
```

### R-CODE-052: 配列宣言は `型名[]` 形式

```java
// ✅
private int[] sampleArray;

// ❌
private int sampleArray[];
```

### R-CODE-053: ローカル変数は利用直前で宣言し、安易に再利用しない

### R-CODE-054: インスタンス変数は `private` にする

### R-CODE-055: 不変コレクション定数は `List.of()`, `Set.of()`, `Map.of()` を使う

```java
// ✅
public static final List<Integer> VALUES = List.of(1, 2, 3);
public static final Map<Integer, String> MAP = Map.of(1, "A", 2, "B");

// ❌
public static final List<Integer> VALUES = Arrays.asList(1, 2, 3);
```

---

## 7. 制御構造

### R-CODE-060: `if`, `else`, `while`, `for`, `do while` の `{}` を省略しない

### R-CODE-061: ステートメントが無い `{}` ブロックを書かない

### R-CODE-062: `if`/`while` の条件式で `=` を使わない

### R-CODE-063: for ループ変数の値をループ内で変更しない

### R-CODE-064: 配列・リストの全要素ループは拡張 for 文を使う

### R-CODE-065: 配列コピーは `Arrays.copyOf()` を使う

### R-CODE-066: 繰り返し処理の内部に `try` ブロックを置かない

例外: ループ内で個別に例外キャッチして処理を継続する必要がある場合。

---

## 8. switch 式・文

### R-CODE-070: 変数代入のための if-else は switch 式に置き換えを推奨

```java
// ✅
var value = switch (op) {
    case "add" -> a + b;
    default -> a - b;
};
```

### R-CODE-071: switch はアロー構文（`->`）を使う

コロン構文（`case L:`）はフォールスルーの原因になるため使用しない。

### R-CODE-072: アロー構文の中カッコ・`yield` は省略できる場合は省略する

### R-CODE-073: Enum の switch 式で全値をカバーする場合、`default` は書かない

### R-CODE-074: `instanceof` の連鎖は switch パターンマッチングで置き換える

```java
// ✅
return switch (obj) {
    case Integer i -> String.format("int %d", i);
    case String s  -> String.format("String %s", s);
    default        -> Objects.toString(obj);
};
```

---

## 9. 文字列操作

### R-CODE-080: 文字列比較は `equals()` を使う（`==` 禁止）

### R-CODE-081: 文字列リテラルは `new String()` しない

### R-CODE-082: ループ内の文字列連結は `StringBuilder` を使う

1 ステートメントのみの連結には `+` 演算子を使ってよい。

### R-CODE-083: 文字列リテラルと変数の比較はリテラル側の `equals()` を使う

```java
// ✅
if ("http".equals(url.getProtocol()))

// ❌
if (url.getProtocol().equals("http"))
```

### R-CODE-084: プリミティブ型と String の変換には `String.valueOf()` を使う

### R-CODE-085: システム依存改行記号（`\n`, `\r`）を使用しない

`System.lineSeparator()` を使う。

---

## 10. 数値

### R-CODE-090: 誤差のない計算（金額等）は `BigDecimal` を使う

### R-CODE-091: `BigDecimal` の比較は `compareTo()` を使う（`equals()` は精度が異なると不一致になる）

### R-CODE-092: `BigDecimal` の String 変換は `toPlainString()` を使う（`toString()` は指数表記になりうる）

---

## 11. コレクション

### R-CODE-100: オブジェクト参照にはインターフェースを使う

```java
// ✅
List<Entry> list = new ArrayList<>();
Map<String, String> map = new HashMap<>();
```

### R-CODE-101: ジェネリクスで型パラメータを指定する

### R-CODE-102: `List` のソートは `List.sort()` を使う（`Collections.sort()` は使わない）

### R-CODE-103: `Collection.forEach()` は原則使わない、拡張 for 文を使う

例外: メソッド参照で完結する場合は `forEach` を使ってよい。

```java
// ✅ メソッド参照なら OK
List.of("A", "B").forEach(this::process);

// ✅ それ以外は拡張 for
for (String s : list) {
    // 処理
}
```

### R-CODE-104: `Arrays.asList()` ではなく `List.of()` を使う

### R-CODE-105: 順序付きコレクションの先頭・末尾操作は `SequencedCollection` のメソッドを使う

`getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `reversed()` 等。

---

## 12. ラムダ式・メソッド参照

### R-CODE-110: ラムダ式が使える箇所は匿名クラスより優先する

### R-CODE-111: メソッド参照・コンストラクタ参照が使える場合はそちらを優先する

```java
// ✅
String::compareToIgnoreCase

// ❌
(s1, s2) -> s1.compareToIgnoreCase(s2)
```

### R-CODE-112: ラムダ式の型宣言は省略する

### R-CODE-113: ラムダ式は原則 1 行。複数行になる場合は private メソッドに切り出してメソッド参照を使う

### R-CODE-114: 1 行ラムダの中カッコ・`return` は必ず省略する

---

## 13. Stream API

### R-CODE-120: 利用してよいが、並列ストリーム（`parallelStream()`, `parallel()`）は使用禁止

### R-CODE-121: 改行位置は各中間処理・終端処理前のピリオドの前

```java
List<Character> result = list.stream()
        .filter(Character::isAlphabetic)
        .map(Character::toLowerCase)
        .toList();
```

### R-CODE-122: 中間処理は 3 つ程度まで。超える場合はロジック分割を検討する

### R-CODE-123: Stream を変数に代入しない（使用済みインスタンスの再利用ミスを防ぐ）

---

## 14. Optional

### R-CODE-130: 同メソッド内で値を取り出す場合、Optional を変数に代入しない

```java
// ✅
Employee employee = findEmployee(id)
        .orElseThrow(IllegalArgumentException::new);

// ❌
Optional<Employee> opt = findEmployee(id);
Employee employee = opt.orElseThrow(IllegalArgumentException::new);
```

例外: Optional のまま複数回使う場合は変数代入してよい。

---

## 15. var（ローカル変数の型推論）

### R-CODE-140: プロジェクト方針 — 右辺で型が明確な場合に `var` を使う

リテラル・`new`・キャスト・ファクトリメソッドで型が明らかな場合に使用する。
右辺から型が判断できない場合は明示的な型宣言を使う。

```java
// ✅ 型が明確
var s = "";
var list = new ArrayList<String>();
var items = List.of("A", "B");

// ❌ 型が不明確
var data = e.getData();
```

---

## 16. Record

### R-CODE-150: プロジェクト方針 — 一時的なデータ表現に限定して使用可

DTO（Request/Response）や domain model には使わず、メソッド内の一時的なキー・タプル表現に使う。
ライブラリの JavaBeans 互換性に注意する。

### R-CODE-151: Record のアクセサをオーバーライドしない

### R-CODE-152: レコードパターンを使って分解する

```java
// ✅
if (obj instanceof Point(int x, int y)) {
    System.out.println(x + y);
}
```

---

## 17. テキストブロック

### R-CODE-160: 複数行文字列にはテキストブロックを使う

```java
// ✅
String sql = """
        SELECT id, name
        FROM users
        WHERE status = 'ACTIVE'
        """;
```

### R-CODE-161: 単一行文字列にはテキストブロックを使わない

例外: `"` のエスケープ回避目的では使用可。

### R-CODE-162: テキストブロックのインデントは周辺の Java コードに合わせる

---

## 18. 例外

### R-CODE-170: catch は詳細な例外クラスで受ける（`Exception` で括らない）

### R-CODE-171: `Exception` オブジェクトを直接 throw しない

### R-CODE-172: catch ブロックで処理を握りつぶさない。意図的に無視する場合は `// ignore` コメントを記述する

---

## 19. リソース管理

### R-CODE-180: ストリーム・リソースは try-with-resources で後処理する

```java
try (InputStream in = Files.newInputStream(path)) {
    // 処理
}
```

### R-CODE-181: リソース解放が必要なクラスを作成する場合は `AutoCloseable` を実装する

---

## 20. インスタンス比較

### R-CODE-190: オブジェクト比較は `equals()` を使う（`==` はプリミティブと Enum のみ）

### R-CODE-191: Class 名文字列での型比較を行わない。`instanceof` パターンマッチングを使う

```java
// ✅
if (o instanceof Foo f) { ... }

// ❌
if ("my.Foo".equals(o.getClass().getName()))
```

---

## 21. 三項演算子

### R-CODE-200: 三項演算子のネスト禁止

---

## 22. ガベージコレクション

### R-CODE-210: `finalize()` のオーバーライド禁止

---

## 23. シールクラス

### R-CODE-220: プロジェクト方針 — 共通ライブラリ（shared）に限定して使用可

ドメイン層の型階層が限定的な場合に使用する。外部公開 API やサードパーティ連携部分では使用しない。

---

## 24. テスト規約

### R-TEST-001: @Valid @RequestBody を持つコントローラーメソッドのテストは MockMvc を使う（必須）

@Valid @RequestBody を持つコントローラーメソッドのテストは、
メソッドを直接呼び出さず MockMvc を使用する。
メソッド直呼びでは Bean Validation (@Valid) が実行されないため、
カスタムバリデーターの型不一致・制約違反が検出されない。

```java
// OK: MockMvc (Bean Validation が動く)
mockMvc.perform(post("/api/jobs")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(request)))
    .andExpect(status().isCreated());

// NG: 直呼び (Bean Validation がスキップされる)
controller.createJob(request, user);
```

### R-TEST-002: ConstraintValidator<A, T> の T はフィールドの実際の型に一致させる

型不一致は Hibernate Validator が実行時に
HV000030: No validator could be found
を投げて 500 エラーになる。コンパイルも静的解析も通るため発見が難しい。

```java
// OK: OffsetDateTime フィールドに OffsetDateTime バリデーター
public class FutureDatetimeValidator
    implements ConstraintValidator<FutureDatetime, OffsetDateTime> { ... }

// NG: OffsetDateTime フィールドに LocalDateTime バリデーター (実行時例外)
public class FutureDatetimeValidator
    implements ConstraintValidator<FutureDatetime, LocalDateTime> { ... }
```
