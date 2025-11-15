# 基于 openjdk 17 镜像
FROM openjdk:17-ea-alpine

# 设置工作目录
WORKDIR /app

# 安装依赖（curl + 时区 + shadow）
RUN apk add --no-cache curl shadow tzdata \
    && ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && groupadd -r spring && useradd -r -g spring spring

# 复制 JAR 文件
COPY target/payment-service-1.0.0.jar app.jar

# 切换为非 root 用户
USER spring

# 暴露服务端口（与配置一致）
EXPOSE 8080

# JVM 优化参数
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令（指定 Docker 环境）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=docker"]
