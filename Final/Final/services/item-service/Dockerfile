# 使用Alpine Linux基础的OpenJDK镜像
FROM openjdk:17-ea-alpine

# 设置维护者信息
LABEL maintainer="your-team@shopping.com"
LABEL version="1.0.0"
LABEL description="Item Service for Online Shopping Platform"

# 设置工作目录
WORKDIR /app

# 安装curl（现在apk命令可以工作）
RUN apk update && apk add --no-cache curl

# 复制JAR文件
COPY target/item-service-1.0.0.jar app.jar

# 创建非root用户运行应用
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# 暴露应用端口
EXPOSE 8082

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# 容器启动时运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]