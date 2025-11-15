FROM openjdk:17-ea-alpine

WORKDIR /app

# 安装依赖并创建非 root 用户
RUN apk add --no-cache shadow curl \
    && groupadd -r spring && useradd -r -g spring spring \
    && ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone

# 复制 JAR 文件（确保文件名匹配）
COPY target/account-service-1.0.0.jar app.jar

# 切换非 root 用户
USER spring

# 暴露端口（与服务端口一致）
EXPOSE 8082

# JVM 优化参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

