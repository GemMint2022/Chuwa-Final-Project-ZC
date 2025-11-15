#!/bin/bash
echo "🧪 开始支付服务Docker测试..."

# 1. 构建镜像
echo "1. 构建Docker镜像..."
docker build -t payment-service:test .

# 2. 运行容器
echo "2. 启动容器..."
docker run -d --name payment-test -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  payment-service:test

# 3. 等待启动
echo "3. 等待服务启动..."
sleep 30

# 4. 健康检查
echo "4. 健康检查..."
curl -f http://localhost:8080/actuator/health || exit 1

# 5. API测试
echo "5. API端点测试..."
curl -f http://localhost:8080/api/payments/health || exit 1

echo "✅ Docker测试通过！"
docker stop payment-test
docker rm payment-test