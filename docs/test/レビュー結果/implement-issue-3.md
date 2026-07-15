# レビュー結果（implement / Issue #3）

> 最新 round が最上部。各 round は機械可読 JSON を人間向けに整形したもの。

## Round ? — 2026-07-15 02:12 — overall: FAIL（BLOCK 2 / SUGGEST 0 / NIT 1）

| 重大度 | カテゴリ | 該当 | 指摘 | 推奨対応 | 対応状況 |
|---|---|---|---|---|---|
| BLOCK | quality_gate | target/.gate-content | 品質ゲートのハッシュサイドカー「target/.gate-content」の記録値（460dea02...）と、現HEAD（75c00ad）に対し bash .claude/skills/_common/scripts/gate-content-hash.sh を再計算した値（0890b634...）が不一致。各レポート（jacoco.exec 09:49:04・checkstyle-result.xml 09:49:08・pmd.xml 09:49:12・spotbugsXml.xml 09:49:25・.gate-content 09:49:54）はいずれも直前コミット e641dd4（09:26:29）時点のコードに対して実行されたものだが、最終コミット75c00ad「SystemClockをshared/infrastructure/clock配下へ移動」は09:51:36とその後に行われており、src/ 配下のファイル移動（SystemClock.java・SystemClockTest.java）を経た現在のコードに対して品質ゲートが再実行されていない。quality-gate-outputs.md が定める「不一致なら category=quality_gate」の判定基準に該当する。 | mvn clean verify（またはプロジェクトの品質ゲート実行コマンド）を現HEADに対して再実行し、bash .claude/skills/_common/scripts/gate-content-hash.sh > target/.gate-content でサイドカーを再生成してから再レビューする。 | 未対応 |
| BLOCK | security | src/main/resources/openapi-templates/pojo.mustache:269 | pom.xml のコメントは「カスタム Mustache テンプレート: pojo.mustache で x-sensitive / format:password → WRITE_ONLY + ログマスク を自動付与」と明記し、backend-07-security-coding.md §0(a) も「format: password / writeOnly: true / x-sensitive: true → getter に @JsonProperty(access = WRITE_ONLY) + toString() で [MASKED]」を要求するが、実装は toString() のマスク（「***」表記）のみで、getter への @JsonProperty(access = WRITE_ONLY) 付与が一切実装されていない。実際に auth.yaml の LoginRequest.password（writeOnly: true, x-sensitive: true）から生成された target/generated-sources/openapi/.../LoginRequest.java を確認したところ、getter は素の @JsonProperty("password") のみで access 制御が無く、R-SEC-110/111（機密フィールドをレスポンスに含めない防御層）の生成時自動付与が欠落している。 | pojo.mustache の getter 生成部（{{#jackson}} ブロック、{{getter}}() 定義付近）に、{{#isPassword}} と同様に vendorExtensions.x-sensitive / writeOnly を判定し、該当フィールドの @JsonProperty アノテーションに access = JsonProperty.Access.WRITE_ONLY を付与する分岐を追加する。追加後は target/generated-sources を再生成し LoginRequest 等で確認する。 | 未対応 |
| NIT | typo | docs/test/単体テストマトリクス.md:59 | TC-023 のシナリオ列が「null／空文字で生成」と記載されているが、TenantId/UserId/JobId は Long 型の値オブジェクトであり「空文字」は型として発生し得ない（実テスト TypedIdTest も null のみを検証している）。設計と実コードの記述に軽微な不整合がある。 | シナリオ列を「null で生成」のみに修正するか、Long 型である旨の注記を追加して「空文字」の記載を削除する。 | 未対応 |

検査済み観点: checked 22 / partial 0 / not-checked 3

未カバー領域:
- dead-field（not-checked）: 本Issueはバックエンド共通部品のみでFE/画面表示項目を伴わないため対象外
- frontend_convention（not-checked）: バックエンドリポジトリのみの変更でFEファイルは対象外
- nonfunc_test（not-checked）: 本Issueは横断部品の実装であり、該当する非機能要求値（性能・負荷）の検証対象が非機能テスト計画.mdに存在しない


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

