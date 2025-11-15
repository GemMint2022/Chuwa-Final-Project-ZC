# 测试基础功能
Write-Host "=== Testing Basic Order Service ===" -ForegroundColor Green

# 1. 检查健康状态
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8083/actuator/health" -ErrorAction Stop
    Write-Host "✅ Health: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Health check failed" -ForegroundColor Red
    exit 1
}

# 2. 检查 Cassandra 数据
Write-Host "`nChecking Cassandra data..." -ForegroundColor Yellow
docker exec cassandra-test cqlsh -e "USE order_keyspace; SELECT * FROM orders LIMIT 1;"

# 3. 检查 Kafka 主题
Write-Host "`nChecking Kafka topics..." -ForegroundColor Yellow
docker exec kafka-test kafka-topics --bootstrap-server kafka:9092 --list

# 4. 测试获取用户订单（应该返回空数组）
Write-Host "`nTesting get orders by user..." -ForegroundColor Yellow
try {
    $orders = Invoke-RestMethod -Uri "http://localhost:8083/api/orders/user/test-user-1" -ErrorAction Stop
    Write-Host "✅ Get user orders works: $($orders.Count) orders" -ForegroundColor Green
} catch {
    Write-Host "❌ Get user orders failed: $_" -ForegroundColor Red
}

Write-Host "`n=== Basic connectivity test completed ===" -ForegroundColor Green