#!/usr/bin/env bash
# OpenAPI → Java モデル生成スクリプト
# フロントエンドの gen-api-types.sh に対応するバックエンド版。
# docs/design/api/*.yaml から target/generated-sources/openapi/ に Java クラスを生成する。
#
# 使い方:
#   bash scripts/gen-api-models.sh
#
# 環境変数:
#   OPENAPI_SPEC_DIR  YAML ディレクトリのパス（省略時: ../claude-poc-docs/docs/design/api）
#                     Windows/Git Bash では自動的に Maven が解釈できる形式（C:/...）に変換される。
#
# 出力先:
#   target/generated-sources/openapi/com/example/logisticsmatching/generated/openapi/

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Maven コマンドを解決（PATH → mvnw → ~/.m2/wrapper 配下の順で探す）
find_mvn() {
  if command -v mvn &>/dev/null; then
    echo "mvn"
  elif [[ -f "${PROJECT_DIR}/mvnw" ]]; then
    echo "${PROJECT_DIR}/mvnw"
  else
    local wrapper_dir="${HOME}/.m2/wrapper/dists"
    local mvn_bin
    mvn_bin=$(find "$wrapper_dir" -name "mvn" -type f 2>/dev/null | sort -V | tail -1)
    if [[ -n "$mvn_bin" ]]; then
      echo "$mvn_bin"
    else
      echo ""
    fi
  fi
}

MVN_CMD=$(find_mvn)
if [[ -z "$MVN_CMD" ]]; then
  echo "❌ Maven が見つかりません。mvn を PATH に追加するか、プロジェクトルートに mvnw を配置してください。"
  exit 1
fi
SPEC_DIR="${OPENAPI_SPEC_DIR:-${PROJECT_DIR}/../claude-poc-docs/docs/design/api}"

# POSIX → Maven（Java）が解釈できるパス形式へ変換
# Git Bash on Windows: /c/Users/... → C:/Users/... (cygpath -m = mixed slash format)
# Linux/Mac: そのまま使用
if command -v cygpath &>/dev/null; then
  SPEC_DIR_FOR_MAVEN=$(cygpath -m "$SPEC_DIR")
else
  SPEC_DIR_FOR_MAVEN="$SPEC_DIR"
fi

echo "▶ OpenAPI モデル生成"
echo "  YAML ディレクトリ : ${SPEC_DIR}"
echo "  Maven 渡しパス    : ${SPEC_DIR_FOR_MAVEN}"
echo ""

if [[ ! -d "$SPEC_DIR" ]]; then
  echo "❌ YAML ディレクトリが見つかりません: ${SPEC_DIR}"
  echo "   OPENAPI_SPEC_DIR 環境変数でパスを指定してください。"
  exit 1
fi

yaml_count=$(find "$SPEC_DIR" -maxdepth 1 -name "*.yaml" | wc -l)
if [[ "$yaml_count" -eq 0 ]]; then
  echo "❌ YAML ファイルが見つかりません: ${SPEC_DIR}/*.yaml"
  exit 1
fi

echo "  対象 YAML: ${yaml_count} ファイル"
echo ""
echo "  mvn generate-sources ..."
echo ""

if "$MVN_CMD" generate-sources \
    -f "${PROJECT_DIR}/pom.xml" \
    -Dopenapi.spec.basedir="${SPEC_DIR_FOR_MAVEN}" \
    2>&1; then
  OUT_DIR="${PROJECT_DIR}/target/generated-sources/openapi"
  java_count=$(find "$OUT_DIR" -name "*.java" 2>/dev/null | wc -l || echo 0)
  echo ""
  echo "✅ モデル生成完了"
  echo "   出力先        : ${OUT_DIR}"
  echo "   生成ファイル数: ${java_count} 件"
  echo ""
  echo "   次のステップ: mvn compile で生成クラスをコンパイル確認できます。"
else
  echo ""
  echo "❌ モデル生成失敗"
  echo "   原因の確認手順:"
  echo "   1. YAML 構文エラーの有無を確認:"
  echo "      bash ../claude-poc-docs/.claude/skills/_common/scripts/validate-yaml-format.sh ${SPEC_DIR}"
  echo "   2. mvn generate-sources -Dopenapi.spec.basedir=... を単体実行して詳細ログを確認"
  exit 1
fi
