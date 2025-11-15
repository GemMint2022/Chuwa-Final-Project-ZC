# setup-mock.ps1
Write-Host "=== Setting up Mock Item Service ===" -ForegroundColor Green

# 等待 Mock Service 启动
Write-Host "Waiting for Mock Service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 配置 Mock 响应 - 批量获取商品信息
$batchMockConfig = @{
    httpRequest = @{
        method = "POST"
        path = "/api/items/batch"
    }
    httpResponse = @{
        statusCode = 200
        body = (@(
            @{
                itemId = "item-001"
                name = "Premium Wireless Headphones"
                price = 199.99
                stock = 50
                imageUrl = "https://example.com/images/headphones.jpg"
                category = "Electronics"
                description = "High-quality wireless headphones with noise cancellation"
            },
            @{
                itemId = "item-002"
                name = "Smart Fitness Watch"
                price = 299.99
                stock = 30
                imageUrl = "https://example.com/images/watch.jpg"
                category = "Electronics"
                description = "Advanced fitness tracking smartwatch"
            },
            @{
                itemId = "item-003"
                name = "Organic Coffee Beans"
                price = 24.99
                stock = 100
                imageUrl = "https://example.com/images/coffee.jpg"
                category = "Food"
                description = "Premium organic coffee beans from Ethiopia"
            }
        ) | ConvertTo-Json)
        headers = @{
            "Content-Type" = "application/json"
            "Access-Control-Allow-Origin" = "*"
        }
    }
} | ConvertTo-Json -Depth 10

Write-Host "Configuring batch items endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/mockserver/expectation" -Method Put -Body $batchMockConfig -ContentType "application/json"
    Write-Host "✅ Batch items mock configured" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to configure batch mock: $_" -ForegroundColor Red
}

# 配置单个商品信息端点
$singleItemMockConfig = @{
    httpRequest = @{
        method = "GET"
        path = "/api/items/.*"  # 正则匹配所有商品ID
    }
    httpResponse = @{
        statusCode = 200
        body = (@{
            itemId = "dynamic-item"
            name = "Dynamic Product"
            price = 49.99
            stock = 75
            imageUrl = "https://example.com/images/default.jpg"
            category = "General"
            description = "This is a dynamically mocked product"
        } | ConvertTo-Json)
        headers = @{
            "Content-Type" = "application/json"
        }
    }
} | ConvertTo-Json -Depth 10

Write-Host "Configuring single item endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/mockserver/expectation" -Method Put -Body $singleItemMockConfig -ContentType "application/json"
    Write-Host "✅ Single item mock configured" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to configure single item mock: $_" -ForegroundColor Red
}

# 验证 Mock 配置
Write-Host "`nVerifying mock configuration..." -ForegroundColor Yellow
try {
    $mockStatus = Invoke-RestMethod -Uri "http://localhost:8081/mockserver/status" -Method Put
    Write-Host "✅ Mock Server is running: $($mockStatus.version)" -ForegroundColor Green
} catch {
    Write-Host "❌ Mock Server not responding" -ForegroundColor Red
}

Write-Host "`n=== Mock Service Setup Complete ===" -ForegroundColor Green
Write-Host "Mock Service URL: http://localhost:8081" -ForegroundColor Cyan
Write-Host "You can test the mock directly:" -ForegroundColor Cyan
Write-Host "  Invoke-RestMethod -Uri 'http://localhost:8081/api/items/batch' -Method Post -Body '[""item-001"",""item-002""]' -ContentType 'application/json'" -ForegroundColor White