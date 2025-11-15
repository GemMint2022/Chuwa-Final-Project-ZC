FROM openjdk:17-ea-alpine

WORKDIR /app

# 安装依赖（curl + 时区）并创建非 root 用户
RUN apk add --no-cache tzdata curl \
    && ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && addgroup -S spring && adduser -S spring -G spring

# 复制精确的 JAR 文件
COPY target/order-service-1.0.0.jar app.jar

# 切换非 root 用户
USER spring

# 暴露服务端口（与配置一致）
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# 启动命令（指定 Docker 环境）
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
