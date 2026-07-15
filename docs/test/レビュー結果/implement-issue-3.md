# レビュー結果（implement / Issue #3）

> 最新 round が最上部。各 round は機械可読 JSON を人間向けに整形したもの。

## Round ? — 2026-07-15 00:40 — overall: FAIL（BLOCK 1 / SUGGEST 0 / NIT 0）

| 重大度 | カテゴリ | 該当 | 指摘 | 推奨対応 | 対応状況 |
|---|---|---|---|---|---|
| BLOCK | design_mismatch | src/main/java/com/example/logisticsmatching/shared/clock/SystemClock.java:1 | 「共通部品設計.md」§5は`Clock`の配置を「shared/clock/Clock.java（interface）、infrastructure 側に SystemClock 実装」と定めており、`backend-04-package-structure.md`のsharedモジュール構成例も`clock/`配下は「Clock.java # interface のみ」と明記している。しかし実装では`SystemClock`が`shared/clock/`にinterfaceと同居しており、設計書・パッケージ規約の両方に反する。 | SystemClockをinfrastructure相当のパッケージ（例: shared/infrastructure/clock/SystemClock.java、他モジュールのinfrastructure/配下と同様の位置づけ）へ移動し、shared/clock/にはClock.javaのみを残す。 | 未対応 |

検査済み観点: checked 18 / partial 2 / not-checked 4

未カバー領域:
- dead-field（not-checked）: 本Issueはフロントエンド・API応答の表示消費経路を持たないため対象外（共通部品のみ）。
- security-baseline（partial）: Clockインターフェース経由の時刻取得は達成。BCryptコスト・JWT失効方針・ログイン試行ロック・メールアダプタ実配線は認証API Issue側の範囲のため本Issueでは未着手（設計上も本Issueの対象外と明記）。
- security（partial）: OWASP観点のうちXSS対策（@Pattern付与）は実際の業務DTOフィールドがまだ存在しないため対象外。認可（@PreAuthorize）・テナント越境対応もControllerが存在しないため本Issueでは評価不能（後続API Issueで評価）。
- frontend_convention（not-checked）: バックエンドリポジトリのみの変更のためFE規約は対象外。
- pagination（not-checked）: 一覧系APIのRepository実装が本Issueに存在しないため評価対象外（PageMetaFactoryは算出ロジックのみで対象外）。
- nonfunc_test（not-checked）: 本Issueは横断コンポーネントのみで性能・負荷に影響する処理を含まないため、非機能テスト計画との対応付けは対象外と判断。


## Round ? — 2026-07-15 00:12 — overall: FAIL（BLOCK 2 / SUGGEST 0 / NIT 0）

| 重大度 | カテゴリ | 該当 | 指摘 | 推奨対応 | 対応状況 |
|---|---|---|---|---|---|
| BLOCK | concurrency | src/main/java/com/example/logisticsmatching/shared/GlobalExceptionHandler.java:30 | GlobalExceptionHandler に「OptimisticLockException」「DataIntegrityViolationException」を409へマッピングする @ExceptionHandler が存在しない。StateConflictException のJavadocは「OptimisticLockException / DataIntegrityViolationException も本例外相当としてGlobalExceptionHandlerで409にマッピングする（RC-01）」と明記しているが、実装にはDomainException・MethodArgumentNotValidException・Exceptionの3ハンドラしかなく、対応するテストも無い（GlobalExceptionHandlerTestに該当ケースなし）。楽観ロック競合時に500 INTERNAL_ERRORへフォールバックしてしまう。 | @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class}) を追加し、ErrorCode.CONFLICT・HttpStatus.CONFLICTでErrorResponseを返す実装とテストを追加する。 | 未対応 |
| BLOCK | quality_gate | target/.gate-content | 品質ゲート（JaCoCo/Checkstyle/PMD）実行後に生成されるべきハッシュサイドカー「target/.gate-content」が存在しない（RC-07）。実装スキル自身の参照文書（quality-gate-outputs.md）が「ゲート実行直後にgate-content-hash.shの出力をサイドカーへ保存する」ことを手順として定めているにもかかわらず未実施であり、現HEAD（6c817ad）に対して各レポート（jacoco.xml/checkstyle-result.xml/pmd.xml）が実際に実行されたことを内容ハッシュで確認できない。 | 実装コミット時に bash .claude/skills/_common/scripts/gate-content-hash.sh > target/.gate-content を実行しレポートと同時にサイドカーを生成する運用を徹底する。 | 未対応 |

検査済み観点: checked 21 / partial 0 / not-checked 3

未カバー領域:
- dead-field（not-checked）: 本Issueはバックエンド共通部品のみでFE/画面表示項目を伴わないため対象外
- frontend_convention（not-checked）: バックエンドリポジトリのみの変更でFEファイルは対象外
- nonfunc_test（not-checked）: 本Issueは横断部品の実装でありスループット等の非機能要求値の検証対象なし（非機能テスト計画.mdに本Issue該当項目なし）

