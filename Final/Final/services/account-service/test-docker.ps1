# Docker 环境测试脚本 - PowerShell 版本

Write-Host "=== Docker Environment Test ===" -ForegroundColor Green

# 1. 健康检查
Write-Host "`n1. Testing Health Endpoints..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -ErrorAction Stop
    Write-Host "✓ Health endpoint: OK" -ForegroundColor Green
    Write-Host "Status: $($health.status)"
} catch {
    Write-Host "✗ Health endpoint: FAILED" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
}

# 2. 用户注册测试
Write-Host "`n2. Testing User Registration..." -ForegroundColor Yellow
$registerBody = @{
    email = "docker-test@example.com"
    username = "dockertest"
    password = "dockerpass123"
    shippingAddress = "456 Docker St"
    billingAddress = "456 Docker St"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ User registration: OK" -ForegroundColor Green
    Write-Host "User ID: $($registerResponse.id)"
    Write-Host "Email: $($registerResponse.email)"
} catch {
    Write-Host "✗ User registration: FAILED" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
}

# 3. 用户登录测试
Write-Host "`n3. Testing User Login..." -ForegroundColor Yellow
$loginBody = @{
    email = "docker-test@example.com"
    password = "dockerpass123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ User login: OK" -ForegroundColor Green
    $token = $loginResponse.token
    Write-Host "Token received: $($token.Length > 0)"
    Write-Host "Token preview: $($token.Substring(0, [Math]::Min(20, $token.Length)))..."
} catch {
    Write-Host "✗ User login: FAILED" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
}

# 4. 认证 API 测试
if ($token) {
    Write-Host "`n4. Testing Authenticated APIs..." -ForegroundColor Yellow
    $headers = @{
        "Authorization" = "Bearer $token"
    }

    try {
        $userResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/users/email/docker-test@example.com" -Headers $headers -ErrorAction Stop
        Write-Host "✓ Authenticated API: OK" -ForegroundColor Green
        Write-Host "User email: $($userResponse.email)"
        Write-Host "Username: $($userResponse.username)"
    } catch {
        Write-Host "✗ Authenticated API: FAILED" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)"
    }
}

# 5. Swagger 文档测试
Write-Host "`n5. Testing API Documentation..." -ForegroundColor Yellow
try {
    $swaggerDoc = Invoke-RestMethod -Uri "http://localhost:8081/v3/api-docs" -ErrorAction Stop
    Write-Host "✓ Swagger documentation: OK" -ForegroundColor Green
    Write-Host "API Title: $($swaggerDoc.info.title)"
} catch {
    Write-Host "✗ Swagger documentation: FAILED" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
}

# 6. 容器状态检查
Write-Host "`n6. Checking Container Status..." -ForegroundColor Yellow
docker-compose ps

Write-Host "`n=== Test Complete ===" -ForegroundColor Green