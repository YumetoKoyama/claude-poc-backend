# ビルドステージ（Java 25 / Maven）
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
COPY config ./config
COPY claude-poc-docs-api /claude-poc-docs/docs/design/api
RUN mvn -B -q clean package -DskipTests

# 実行ステージ
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]