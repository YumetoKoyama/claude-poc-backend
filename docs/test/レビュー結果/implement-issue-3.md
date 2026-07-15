# レビュー結果（implement / Issue #3）

> 最新 round が最上部。各 round は機械可読 JSON を人間向けに整形したもの。

## Round ? — 2026-07-15 05:15 — overall: FAIL（BLOCK 1 / SUGGEST 1 / NIT 0）

| 重大度 | カテゴリ | 該当 | 指摘 | 推奨対応 | 対応状況 |
|---|---|---|---|---|---|
| BLOCK | quality_gate | target/.gate-content:1 | 品質ゲート（mvn test / JaCoCo / Checkstyle / PMD / SpotBugs）が現在のコードに対して実行されたことを示す内容ハッシュが一致しない。bash .claude/skills/_common/scripts/gate-content-hash.sh の再計算値は「400c24d1...」だが、target/.gate-content に記録された値は「e63d48f1...」であり、直近コミット673eca6（GeneratedModelMaskingTest.java追加・SensitiveDataMasker.javaへのJavadoc追記）を反映したサイドカー更新が行われていない。RC-07の判定ルール上、記録ハッシュと現在のコード内容ハッシュが不一致の場合は「古いコードに対するレポート（内容不一致）」としてBLOCKとする。 | 最終コミット後に品質ゲート（mvn verify等でUT・JaCoCo・Checkstyle・PMD・SpotBugsを実行）を再実行し、bash .claude/skills/_common/scripts/gate-content-hash.sh > target/.gate-content でサイドカーを最終コードに対して更新すること。 | 未対応 |
| SUGGEST | scope | .claude/skills/_common/scripts/init-state-with-dispatch.sh:1 | Issue #3（バックエンド共通部品）の実装ブランチに、Issue本文の対象ファイルに含まれないオーケストレーション基盤の変更（コミットdf7af3c「親アンブレラから <repo> <Issue番号> でディスパッチ可能にする」による.claude/skills/_common/scripts/init-state-with-dispatch.sh新規追加・.claude/skills/implement-loop/SKILL.md変更）が混入している。mainブランチ（549e2dd）にもこの変更は無く、Issue #3のスコープ外である。 | Issue #3のPRからはオーケストレーション基盤変更を分離し、別PR（feat/implement-loop-dispatch等）として提出する。CLAUDE.mdの「変更は差分が追いやすい加算型を優先し、無関係なファイルは書き換えない」の原則に沿わせる。 | 未対応 |

検査済み観点: checked 23 / partial 0 / not-checked 3

未カバー領域:
- dead-field（not-checked）: 本Issueはバックエンド共通部品のみでFE/画面表示項目を伴わないため対象外
- frontend_convention（not-checked）: バックエンドリポジトリのみの変更でFEファイルは対象外
- nonfunc_test（not-checked）: 本Issueは横断部品の実装であり、該当する非機能要求値（性能・負荷）の検証対象が非機能テスト計画.mdに存在しない


## Round ? — 2026-07-15 04:46 — overall: FAIL（BLOCK 3 / SUGGEST 1 / NIT 0）

| 重大度 | カテゴリ | 該当 | 指摘 | 推奨対応 | 対応状況 |
|---|---|---|---|---|---|
| BLOCK | quality_gate | target/.gate-content:1 | 品質ゲート（JaCoCo/Checkstyle/PMD/SpotBugs）の対象ハッシュサイドカー「target/.gate-content」の値（e63d48f1...）が、現在のワーキングツリー（HEAD=026d352、main比較のALLOW対象ファイル群）から算出したハッシュ（53bd2f40...）と一致しない。両ハッシュのタイムスタンプを確認したところ、各レポート（checkstyle-result.xml/pmd.xml/spotbugsXml.xml/site/jacoco/jacoco.xml、いずれも13:20〜13:21生成）は最後のソース変更（pojo.mustache、13:11。コミット026d352）より後に生成されており内容的には現行コードを反映している可能性が高いが、「target/.gate-content」自体は13:11より前（11:35）に書かれたまま更新されておらず、RC-07が要求する「内容ハッシュによる機械照合」が成立しない。 | コミット前に必ず「bash .claude/skills/_common/scripts/gate-content-hash.sh > target/.gate-content」をmvn verify直後に実行し、レポート生成とサイドカー更新を同一ステップで行う運用を徹底する。 | 未対応 |
| BLOCK | coverage | src/main/resources/openapi-templates/pojo.mustache:377 | pojo.mustacheのtoString()マスク機能（x-sensitive→"[MASKED]"）とwriteOnlyフィールドのJsonProperty.Access.WRITE_ONLY自動付与機能（IMPL-08、R-SEC-050/R-SEC-110/R-SEC-111の実体的な実装手段）を検証する単体テストが1件も存在しない（src/test配下にMASKED/WRITE_ONLY/toString/mustacheを参照するテストなし）。この2機能はOpenAPI生成コード（target/generated-sources/openapi/配下、例: LoginRequest.java/LoginResponse.java）に対して働くが、生成パッケージはbackend-04-package-structure.md R-PKG-004によりJaCoCoカバレッジ除外対象であるため、100%カバレッジの機械ゲートでもこの欠落は検出されない構造的な盲点になっている。テンプレートを将来誤って修正しても回帰検知できない。 | 既に生成済みのtarget/generated-sources/openapi/.../LoginRequest.java等（writeOnly/x-sensitiveフィールドを持つクラス）を対象に、生成クラスをリフレクションまたは直接インスタンス化してtoString()が"[MASKED]"を含み実値を含まないこと、およびJackson ObjectMapperでシリアライズした結果にwriteOnlyフィールドが含まれないことを検証するテスト（例: GeneratedDtoMaskingTest）をsrc/test配下に追加する。 | 未対応 |
| BLOCK | traceability | (commit df7af3c):1 | feature/issue-3ブランチ（main...HEAD、13コミット）に、Issue #3（共通部品実装）と無関係なスキルオーケストレーション基盤の変更（.claude/skills/_common/scripts/init-state-with-dispatch.sh 新規66行、.claude/skills/implement-loop/SKILL.md +10/-2）を追加するコミット「df7af3c feat(implement-loop): 親アンブレラから <repo> <Issue番号> でディスパッチ可能にする」が含まれており、このコミットメッセージにIssue参照（#3 / Refs:）が無い。内容もIssue #3の「対象ファイル（想定）」「実装内容」のいずれにも該当せず、製造フェーズ（実装対象リポジトリのビジネスロジック実装）のスコープ外である。 | 当該コミットをこのfeatureブランチの履歴から分離し、スキル基盤側の変更は別ブランチ/別PRで管理する（rebase等でdf7af3cをIssue #3の変更から除外する）。分離が難しい場合は、コミットメッセージにIssue #3との関係（無関係であること）を明記し、レビュー時に対象外である旨をPR説明に記載する。 | 未対応 |
| SUGGEST | duplication | src/main/java/com/example/logisticsmatching/shared/logging/SensitiveDataMasker.java:20 | SensitiveDataMasker.mask()は単体テスト（SensitiveDataMaskerTest、TC-031）を持つが、本diffのどの本番コードからも呼び出されておらず（grep結果：定義とテスト以外に参照なし）、未結線のまま放置されている。logback-spring.xmlのコメント（7-8行目）は「機微情報はSensitiveDataMaskerでマスクしてからログ出力する運用とする」と明記しているが、実際に機微情報マスクを実現しているのはpojo.mustacheのtoString()自動マスク機構であり、SensitiveDataMaskerは事実上デッドコードになっている。二重の（片方は結線済み・片方は未結線の）マスク機構が存在することで、将来「個別フィールドをログに直接出力する」コード（例: LOGGER.info("email={}", user.getEmail())）を書く開発者が、SensitiveDataMaskerが実際には呼ばれる保証のないユーティリティだと知らずに安全だと誤認するリスクがある。 | SensitiveDataMaskerを実際の呼び出し箇所（個別フィールドをログ出力する箇所）に結線するか、pojo.mustache側の機構のみで十分と判断するならSensitiveDataMaskerとそのテストを削除し、logback-spring.xmlのコメントをpojo.mustache方式の説明に統一する。 | 未対応 |

検査済み観点: checked 19 / partial 2 / not-checked 4

未カバー領域:
- dead-field（not-checked）: 本IssueはController・API応答エンドポイントを持たずFE消費経路が存在しないため対象外。
- security-baseline（partial）: Clockインターフェース経由の時刻取得（SystemClock）を確認。BCryptコスト・JWT失効方針・ログイン試行ロック保存先・メールアダプタ実配線は認証API Issue側の対象であり本Issueに該当コードが無いため評価対象外。
- security（partial）: OWASP観点のうちハードコードされたシークレット・機微情報のログ/レスポンス直接出力は本diffに該当なしを確認（grep済み）。XSS対策（@Pattern）は業務DTOフィールドが本Issueに存在せず対象外。認可(@PreAuthorize)・テナント越境404/403の実装もControllerが存在しないため評価不能（後続API Issueで評価）。機微情報マスクの実装（pojo.mustache）自体は確認したがテスト未整備のためcoverage findingとして別掲。
- frontend_convention（not-checked）: バックエンドリポジトリのみの変更でFEファイルは対象外。
- pagination（not-checked）: 一覧系APIのRepository実装が本Issueに存在しないため評価対象外（PageMetaFactoryは算出ロジックのみ）。
- nonfunc_test（not-checked）: 本Issueは横断コンポーネントのみで性能・負荷に影響する処理を含まないため、非機能テスト計画との対応付けは対象外と判断。


## Round ? — 2026-07-15 04:08 — overall: FAIL（BLOCK 1 / SUGGEST 0 / NIT 0）

| 重大度 | カテゴリ | 該当 | 指摘 | 推奨対応 | 対応状況 |
|---|---|---|---|---|---|
| BLOCK | design_mismatch | src/main/resources/openapi-templates/pojo.mustache:376 | 正典「.claude/rules/backend-07-security-coding.md」§0(a)（25行目）は「format: password / writeOnly: true / x-sensitive: true」宣言時の toString() 自動生成マスクを「[MASKED]」と明記しているが、本 Issue で追加した pojo.mustache のカスタム toString() 生成部は x-sensitive フィールドを「***」でマスクしている（isPassword のみ「*」）。実際に生成された target/generated-sources/openapi/LoginRequest.java 等 6 クラスも「***」を出力しており、正典の記載値と一致しない。同ファイルの R-SEC-051 の手書き例（293行目付近）は「***」を使っており正典内でも表記が割れているため、正典側の記載修正（/propose-canon-patch）か実装側を「[MASKED]」に合わせるかを明示的に決定し、どちらかへ統一する必要がある。 | pojo.mustache の x-sensitive masking 部（375〜376行目）の出力文字列を「[MASKED]」に変更するか、または正典 backend-07-security-coding.md §0(a) 表・R-SEC-051 の記載を「***」に統一する提案を /propose-canon-patch で起票する。shared.logging.SensitiveDataMasker の MASK 定数（"***"、ログ出力用）は本指摘の対象外（R-SEC-051 の手書き例と整合するため）。 | 未対応 |

検査済み観点: checked 18 / partial 2 / not-checked 4

未カバー領域:
- dead-field（not-checked）: 本Issueはフロントエンド・API応答の表示消費経路を持たないため対象外（共通部品のみ）。
- security-baseline（partial）: Clockインターフェース経由の時刻取得は達成。BCryptコスト・JWT失効方針・ログイン試行ロック保存先・メールアダプタ実配線は認証API Issue側の範囲のため本Issueでは対象外（設計上も明記）。x-sensitive/writeOnlyのtoString()自動マスクは実装済みだが出力文字列が正典と不一致（findings参照）。
- security（partial）: OWASP観点のうちXSS対策（@Pattern付与）・認可（@PreAuthorize）・テナント越境対応はControllerが存在しないため本Issueでは評価不能（後続API Issueで評価）。writeOnlyフィールドのWRITE_ONLYアクセス制御（R-SEC-110/111）は実装・生成コードで確認済み（LoginRequest等6クラス）。
- frontend_convention（not-checked）: バックエンドリポジトリのみの変更のためFE規約は対象外。
- pagination（not-checked）: 一覧系APIのRepository実装が本Issueに存在しないため評価対象外（PageMetaFactoryは算出ロジックのみで対象外）。
- nonfunc_test（not-checked）: 本Issueは横断コンポーネントのみで性能・負荷に影響する処理を含まないため、非機能テスト計画との対応付けは対象外と判断。


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

