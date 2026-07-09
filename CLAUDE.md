# claude-poc-backend 向け指示（バックエンド実装リポジトリ）

本リポジトリは配送マッチング Web アプリのバックエンド実装を担う。設計書の正典は claude-poc-docs、親（横断ルール）は claude-poc-rules。

## 正典の所在

- **実装規約の正典は本リポジトリの `.claude/rules/backend-*.md`。作業開始前に必ず全て Read する。** スタック確定値（ビルド / DB / テスト / 静的解析 / カバレッジ閾値）も人間がここに追記して確定する。
- 開発フロー・ID 規約の正典は親（CI からは参照不可のため最小限を本ファイルに転記）。FE と競合する決定は frontend ルールが正。

## 採択ゲート

- 実装の開始 = 人間が対象 Issue に `@claude` とコメントすること（Issue は採択済み設計書のマージを契機に自動起票される）。
- Claude・skill が自ら Issue 起票・コメント・マージして作業を開始してはならない。

## ハードゲート（スタック未確定）

- 技術スタック（DB 製品・バージョン・マイグレーションツール・テストツール等）の未確定項目を既定値で補完しない。`.claude/rules/` に「要確定」が残る項目に依存する実装は中断し、人間に指定を求める。

## 実装ルール（最小限）

- 画面描画は持たず、JSON ベースの REST API のみを提供する（API 定義は claude-poc-docs の `docs/design/api/`、OpenAPI 3.1）。
- DB 変更では明示的な migration を作成し、Entity・Repository・DDL・設計書（`docs/design/tables/*.md`）の整合を保つ。
- 設計書の API operationId・受け入れ条件（AC-XXX）を実装・テスト・PR で引用する。テスト ID は TC-XXX / IT-XXX（3 桁ゼロ埋め）。
- 品質ゲート: 単体テスト・静的解析・セキュリティ観点（認可バイパス・IDOR/テナント越境・機微情報のログ出力等）を通過してから PR を作成する。E2E は実行しない（凍結中・claude-poc-e2e の別工程）。
- feature ブランチで作業し、PR 本文に `Closes #N` を記載する。

## 共通ルール

- 応答・コミットメッセージ・PR・作業ログは日本語で記述する。
- 本 CLAUDE.md と `.claude/rules/` は編集禁止（protect-canon フック。変更は人手で行う）。ルールを書き換えて品質ゲートを通すことはできない。
