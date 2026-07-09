# バックエンド技術スタック確定表

> 本ファイルはバックエンド技術スタックの**正典**であり、設計着手前ハードゲートの機械チェック対象である。
> 「状態」列に `要確定` が 1 行でも残る間は、`design-from-requirements` 以降に進んではならない
> （`.claude/skills/_common/scripts/check-stack-decided.sh` が検査する）。
> 確定の記入は**人間が行う**。Claude は既定値で補完しない（候補の提案までは可）。

## 凡例

| 略号・値 | 正式名称（日本語） | 補足 |
|---|---|---|
| 確定 | 人間が確定済み | 確定値・確定日を記入済みであること |
| 要確定 | 未確定（設計着手ブロック） | 人間が確定値を記入し `確定` に書き換えるまで設計に進まない |

## 確定表

| # | 項目 | 確定値 | 状態 | 確定日 | 根拠・備考 |
|---|------|--------|------|--------|------------|
| 1 | 言語 | Java | 確定 | 2026-06-04 | CLAUDE.md「バックエンドを Java（Spring Boot）で構成する」 |
| 2 | Java バージョン | 25.0.3 | 確定 | 2026-06-08 | LTS。静的解析・カバレッジ・SpotBugs はバイトコード解析のため Java 25 対応版を明示指定する |
| 3 | フレームワーク | Spring Boot（モジュラモノリス） | 確定 | 2026-06-04 | `backend-01-architecture.md` |
| 4 | フレームワークバージョン | 3.5.14 | 確定 | 2026-06-08 | Java 25 は Spring Boot 3.5.5 以降でサポート。3.5 系 EOL は別途確認のうえ採用 |
| 5 | ビルドツール | Maven | 確定 | 2026-06-08 | CI allowlist（`mvn`）・`技術スタック棚卸し.md` と整合 |
| 6 | ORM・データアクセス | Spring Data JPA | 確定 | 2026-06-04 | `backend-02-layers.md` |
| 7 | DB 製品・バージョン | PostgreSQL 16 | 確定 | 2026-06-08 | devcontainer（postgres:16）と同一バージョンに固定 |
| 8 | DB マイグレーション | なし（デモ期間限定・`ddl-auto` 運用） | 確定 | 2026-06-08 | **デモ段階のため migration ツールを導入せず、Entity からの自動生成（`ddl-auto`）で運用する。CLAUDE.md「DB 変更では明示的な migration を作成」ルールの適用を本期間は免除する（CLAUDE.md 側に例外を明記して整合を取ること）。本番移行時は Flyway / Liquibase の導入を再検討する** |
| 9 | 認証・認可 | Spring Security | 確定 | 2026-06-04 | `backend-02-layers.md`（`@PreAuthorize` 規約はセキュリティ設計で詳細化） |
| 10 | 単体テストフレームワーク | JUnit 5 + Mockito（+ AssertJ、`spring-boot-starter-test` 同梱） | 確定 | 2026-06-08 | バージョンは Spring Boot 3.5 BOM 管理に従う |
| 11 | 結合テストの DB 方式 | Testcontainers（PostgreSQL 16） | 確定 | 2026-06-08 | `@ServiceConnection` で接続自動化。本番同一 DB で方言差を排除。IT-XXX 実行の前提 |
| 12 | 静的解析ツール | Checkstyle 3.6.0(本体10.21.4) + PMD 3.26.0(本体7.25.0) + SpotBugs 4.9.8.4 | 確定 | 2026-06-09 | **Java 25 で `mvn verify` 緑を実機確認した確定版。PMD 7.10.0・SpotBugs 4.8.x はバイトコード（class 69）を解析できず落ちるため、ここ記載のバージョン以上を維持する。** 設定は `config/checkstyle.xml`・`checkstyle-suppressions.xml`・`pmd-ruleset.xml`・`spotbugs-exclude.xml` をリポジトリ固定配置し pom から参照（実装での都度生成を禁止）。PMD は `AvoidLiteralsInIfCondition` を除外（業務の文字列比較で多発するノイズ源）。全ゴールは verify フェーズ束縛（CLI default-cli 禁止） |
| 13 | カバレッジ計測・閾値 | JaCoCo 0.8.15 / INSTRUCTION 100% ・ BRANCH 90%（**除外後**） | 確定 | 2026-06-09 | **JaCoCo 0.8.12 は class 69 非対応のため 0.8.15 で確定（Java 25 緑確認済み）。counter は命令(INSTRUCTION)=100%・分岐(BRANCH)=90% に分離。分岐100%の全域強制は「あと1分岐狩り」でターンを溶かすため、要件が分岐網羅を要求する箇所のみ個別に1.00へ引上げ。除外（DTO/Lombok/設定/main/接着層）は各行に1行理由コメント必須。`jacoco:check` は verify 束縛とし CLI 直叩きを禁止** |
| 14 | CI 実行環境の Java 固定 | actions/setup-java@v5 temurin 25（確定表 #2 と一致） | 確定 | 2026-06-09 | 実装の即興ダウングレード防止。mcr の `1-25` タグは未公開のため devcontainer は SDKMAN で 25.0.3 を導入。未供給時はジョブを失敗させ暗黙の互換版採用を禁止 |
| 15 | 品質ゲート実行方式 | `mvn verify` 一括 | 確定 | 2026-06-09 | test→jacoco→checkstyle→pmd→spotbugs を verify に束縛。個別ゴールの CLI 直叩き(default-cli)を禁止 |
| 16 | API モデルのコード生成 | openapi-generator-maven-plugin 7.13.0（`docs/design/api/*.yaml` → `generated.openapi.*`） | 確定 | 2026-06-29 | **OpenAPI 3.1 YAML を Source of Truth とし、presentation 層の Request/Response DTO を自動生成する。生成は `bash scripts/gen-api-models.sh`（`pom.xml` の `openapi-gen-*` 実行エントリ）で行い、出力は `target/generated-sources/openapi/`。生成クラスは直接編集せず（再生成で上書き）、付与できない要件は別アーティファクトで補完する（`backend-02` presentation 層・`backend-07` ベンダー拡張連動を参照）。YAML を追加・削除・リネームした際は `pom.xml` の `openapi-gen-*` エントリを同期する（pom と yaml の同期）。生成パッケージの静的解析・カバレッジ除外は `backend-04` を参照** |

## 運用ルール

- 行の追加は可（項目の細分化など）。行の削除・「状態」列の廃止は不可（機械チェックの前提が壊れる）。
- 確定時は「確定値」「確定日」を記入し、「状態」を `確定` に書き換える。判断の経緯が長い場合は `docs/process/` 側に記録し備考から参照する。
- 本ファイルは `.claude/rules/` 配下のため protect-canon の保護対象。編集は人手（または `ALLOW_RULES_EDIT=1` セッション）で行う。
