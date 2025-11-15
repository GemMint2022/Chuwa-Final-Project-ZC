FROM openjdk:17-ea-alpine

WORKDIR /app

# 复制构建好的 jar 文件到容器
COPY target/account-service-1.0.0.jar app.jar

# 修复：安装 shadow（提供 groupadd/useradd）和 curl（提供健康检查命令），创建非 root 用户
RUN apk add --no-cache shadow curl \
    && groupadd -r spring && useradd -r -g spring spring

# 切换为非 root 用户运行应用（安全最佳实践）
USER spring

# 暴露服务端口
EXPOSE 8081

# 设置 JVM 优化参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# 容器启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
