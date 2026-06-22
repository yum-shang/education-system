# ============================================================
# 多阶段构建
# 阶段1：Maven 编译打包
# 阶段2：JDK 运行（精简镜像）
# ============================================================

# ---- 构建阶段 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
# 先下载依赖，利用 Docker 缓存层
RUN mvn dependency:go-offline -q || true
COPY src ./src
RUN mvn clean package -DskipTests -q

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 创建日志和上传目录
RUN mkdir -p /app/logs /app/uploads/images /app/uploads/reports

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# JVM 参数可通过环境变量覆盖
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT exec java ${JAVA_OPTS} \
    -Djava.security.egd=file:/dev/./urandom \
    -jar app.jar
