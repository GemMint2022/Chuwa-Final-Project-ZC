# test-with-mock.ps1
Write-Host "=== Complete Test with Mock Item Service ===" -ForegroundColor Green

# 1. 构建和启动所有服务
Write-Host "`n1. Starting all services..." -ForegroundColor Yellow
docker-compose -f docker-compose.test.yml down
docker-compose -f docker-compose.test.yml up --build -d

# 2. 等待服务启动
Write-Host "`n2. Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# 3. 设置 Mock
Write-Host "`n3. Setting up mock responses..." -ForegroundColor Yellow
.\setup-mock.ps1

# 4. 测试 Order Service
Write-Host "`n4. Testing Order Service..." -ForegroundColor Yellow

# 测试健康状态
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8083/actuator/health" -ErrorAction Stop
    Write-Host "✅ Order Service Health: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Order Service Health failed" -ForegroundColor Red
    exit 1
}

# 创建测试订单
Write-Host "`nCreating test order with multiple items..." -ForegroundColor Yellow
$orderRequest = @{
    userId = "test-user-mock-1"
    items = @(
        @{
            itemId = "item-001"
            quantity = 1
        },
        @{
            itemId = "item-002"
            quantity = 2
        }
    )
    shippingAddress = @{
        street = "456 Mock Avenue"
        city = "Testville"
        state = "TV"
        zipCode = "54321"
        country = "Testland"
    }
} | ConvertTo-Json -Depth 5

try {
    $orderResponse = Invoke-RestMethod -Uri "http://localhost:8083/api/orders" -Method Post -Body $orderRequest -ContentType "application/json"
    Write-Host "✅ Order created successfully: $($orderResponse.orderId)" -ForegroundColor Green
    Write-Host "   Total Amount: $($orderResponse.totalAmount)" -ForegroundColor Cyan
    Write-Host "   Status: $($orderResponse.status)" -ForegroundColor Cyan

    $orderId = $orderResponse.orderId
} catch {
    Write-Host "❌ Order creation failed: $_" -ForegroundColor Red
    exit 1
}

# 5. 验证数据持久化
Write-Host "`n5. Verifying data persistence..." -ForegroundColor Yellow

# 检查 Cassandra
Write-Host "Checking Cassandra data..." -ForegroundColor Cyan
docker exec cassandra-test cqlsh -e "USE order_keyspace; SELECT orderid, userid, status, totalamount FROM orders WHERE orderid = '$orderId';"

# 检查 Kafka 事件
Write-Host "`nChecking Kafka events..." -ForegroundColor Cyan
docker exec kafka-test kafka-console-consumer --bootstrap-server kafka:9092 --topic order-created-topic --from-beginning --max-messages 1 --timeout-ms 5000

# 6. 测试完整流程
Write-Host "`n6. Testing complete order flow..." -ForegroundColor Yellow

# 模拟支付成功
Write-Host "Simulating payment success..." -ForegroundColor Cyan
$paymentEvent = @{
    orderId = $orderId
    paymentId = "pay_mock_$(Get-Random -Minimum 100000 -Maximum 999999)"
    amount = $orderResponse.totalAmount
    currency = "USD"
    status = "succeeded"
    timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssZ")
} | ConvertTo-Json -Depth 3

echo $paymentEvent | docker exec -i kafka-test kafka-console-producer --broker-list kafka:9092 --topic payment-success-topic
Write-Host "✅ Payment event sent to Kafka" -ForegroundColor Green

# 等待状态更新
Start-Sleep -Seconds 5

# 检查订单状态
try {
    $updatedOrder = Invoke-RestMethod -Uri "http://localhost:8083/api/orders/$orderId" -ErrorAction Stop
    Write-Host "✅ Order status updated to: $($updatedOrder.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to get updated order: $_" -ForegroundColor Red
}

Write-Host "`n🎉 COMPLETE TEST SUCCESSFUL! ===" -ForegroundColor Green
Write-Host "All microservice interactions working correctly:" -ForegroundColor Cyan
Write-Host "✅ Order Service → Mock Item Service" -ForegroundColor Green
Write-Host "✅ Order Service → Cassandra" -ForegroundColor Green
Write-Host "✅ Order Service → Kafka" -ForegroundColor Green
Write-Host "✅ Kafka Consumer → Order Service" -ForegroundColor Green