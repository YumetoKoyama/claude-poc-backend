# Enum マッピングガイド

## 設計方針

ドメイン enum（`*.domain.model.*`）はドメイン層の概念を表すため残す。
生成 inline enum（`generated.openapi.*` 内の `*Enum`）は presentation 層の API 表現。
両者の変換は **Controller 境界のみ** で行う。Service / UseCase はドメイン enum を使用する。

```
HTTP リクエスト
   ↓  生成クラスの CargoTypeEnum（@RequestBody でデシリアライズ）
Controller  ←→  EnumMapper（変換）
   ↓  ドメイン enum の CargoType
UseCase / Service
```

## EnumMapper の実装例

```java
// src/main/java/com/example/logisticsmatching/shared/mapper/EnumMapper.java
package com.example.logisticsmatching.shared.mapper;

import com.example.logisticsmatching.generated.openapi.JobCreateRequest.CargoTypeEnum;
import com.example.logisticsmatching.generated.openapi.JobCreateRequest.TruckTypeEnum;
import com.example.logisticsmatching.job.domain.model.CargoType;
import com.example.logisticsmatching.job.domain.model.TruckType;

public final class EnumMapper {

    private EnumMapper() {}

    public static CargoType toDomain(CargoTypeEnum src) {
        if (src == null) return null;
        return CargoType.valueOf(src.getValue());
    }

    public static TruckType toDomain(TruckTypeEnum src) {
        if (src == null) return null;
        return TruckType.valueOf(src.getValue());
    }

    public static CargoTypeEnum toApi(CargoType src) {
        if (src == null) return null;
        return CargoTypeEnum.fromValue(src.name());
    }
}
```

## Controller での使用例

```java
@PostMapping
public ResponseEntity<JobResponse> createJob(
        @Valid @RequestBody JobCreateRequest req,
        @AuthenticationPrincipal AuthenticatedUser user) {

    CargoType cargoType = EnumMapper.toDomain(req.getCargoType());
    TruckType truckType = EnumMapper.toDomain(req.getTruckType());

    Job job = createJobUseCase.execute(user, req, cargoType, truckType);
    // ...
}
```

## `valueOf` が失敗する場合

`CargoType.valueOf(src.getValue())` が `IllegalArgumentException` を出す場合は、YAML の enum 値とドメイン enum 名が一致していない。
これは YAML 設計の問題のため、**YAML を直接修正せず** `claude-poc-docs` リポジトリに Issue を起票して実装を中断する。

## ドメイン enum を生成クラスの外部型として直接参照しない理由

- 生成クラスは `openapi-generator` が管理するため、YAML 変更で再生成されると enum 値が変わる可能性がある
- ドメイン enum は業務ルール（BR-XXX）に従って命名・管理されるため、API 定義変更の影響を受けない
- テスト可能性: Service 層のテストは生成クラスに依存せずに書ける
