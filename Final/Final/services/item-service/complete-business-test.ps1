# complete-business-test.ps1
Write-Host "=== 正确的完整业务流程测试 ===" -ForegroundColor Cyan

# 1. 创建测试商品
Write-Host "1. 创建测试商品..." -ForegroundColor Yellow
$testItem = @'
{
  "itemId": "CORRECT001",
  "name": "Correct API Test",
  "description": "Testing with correct API endpoints",
  "unitPrice": 79.99,
  "upc": "555444333222",
  "category": "Electronics",
  "brand": "APITest"
}
'@

$testItem | Out-File -FilePath "correct_test.json" -Encoding UTF8
$createResponse = curl.exe -s -X POST http://localhost:8082/api/items -H "Content-Type: application/json" -d "@correct_test.json"
Write-Host "创建响应: $createResponse" -ForegroundColor Green
Remove-Item "correct_test.json" -ErrorAction SilentlyContinue

# 2. 正确的库存初始化
Write-Host "`n2. 初始化库存（正确端点）..." -ForegroundColor Yellow
$initResponse = curl.exe -s -X POST "http://localhost:8082/api/inventory/CORRECT001?quantity=100"
Write-Host "初始化响应: $initResponse" -ForegroundColor Green

# 3. 查询库存状态
Write-Host "`n3. 查询库存状态..." -ForegroundColor Yellow
$inventoryResponse = curl.exe -s http://localhost:8082/api/inventory/CORRECT001
Write-Host "库存状态: $inventoryResponse" -ForegroundColor Green

# 4. 查询可用数量
Write-Host "`n4. 查询可用数量..." -ForegroundColor Yellow
$availableResponse = curl.exe -s http://localhost:8082/api/inventory/CORRECT001/available
Write-Host "可用数量: $availableResponse" -ForegroundColor Green

# 5. 预留库存
Write-Host "`n5. 预留库存..." -ForegroundColor Yellow
$reserveResponse = curl.exe -s -X POST "http://localhost:8082/api/inventory/CORRECT001/reserve?quantity=15"
Write-Host "预留响应: $reserveResponse" -ForegroundColor Green

# 6. 预留后查询库存
Write-Host "`n6. 预留后库存状态..." -ForegroundColor Yellow
$afterReserve = curl.exe -s http://localhost:8082/api/inventory/CORRECT001
Write-Host "预留后库存: $afterReserve" -ForegroundColor Green

# 7. 测试商品搜索
Write-Host "`n7. 测试商品搜索..." -ForegroundColor Yellow
$searchResponse = curl.exe -s "http://localhost:8082/api/items/search?keyword=Correct"
Write-Host "搜索结果: $searchResponse" -ForegroundColor Green

Write-Host "`n=== 正确的API测试完成 ===" -ForegroundColor Green