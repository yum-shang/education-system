# Images 模块 API 测试脚本
# 使用方法：在 education-system/images/ 目录下执行
#   powershell -ExecutionPolicy Bypass -File .\test-images-api.ps1
# 前提：确保 Spring Boot 服务已启动在 localhost:8080

$baseUrl = "http://localhost:8080/api"
$imagesDir = $PSScriptRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Images Module API Test Start" -ForegroundColor Cyan
Write-Host "  Test Image Dir: $imagesDir" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# Step 1: Login and get JWT Token
# ============================================
Write-Host "[Step 1] Login..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "admin123"
    verification_code = "123456"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.token
    $userId = $loginResponse.data.userId
    $role = $loginResponse.data.role
    Write-Host "  Login OK: code=$($loginResponse.code), role=$role" -ForegroundColor Green
    $shortToken = $token.Substring(0, [Math]::Min(50, $token.Length))
    Write-Host "  Token: ${shortToken}..." -ForegroundColor Gray
    Write-Host "  UserId: $userId" -ForegroundColor Gray
} catch {
    Write-Host "  Login FAILED: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

$headers = @{
    "Authorization" = "Bearer $token"
}

# ============================================
# Step 2: Upload JPG image (type=avatar)
# ============================================
Write-Host "[Step 2] Upload JPG (type=avatar)..." -ForegroundColor Yellow

try {
    $jpgFile = Get-Item "$imagesDir\jpg图片.jpg"
    $uploadResult1 = Invoke-RestMethod -Uri "$baseUrl/images" -Method POST `
        -Form @{ file = $jpgFile; type = "avatar" } `
        -Headers $headers
    if ($uploadResult1.code -eq 200) {
        $imageId1 = $uploadResult1.data.imageId
        Write-Host "  Upload OK: imageId=$imageId1" -ForegroundColor Green
    } else {
        Write-Host "  Upload Failed: code=$($uploadResult1.code)" -ForegroundColor Red
    }
} catch {
    Write-Host "  Upload Error: $_" -ForegroundColor Red
}
Write-Host ""

# ============================================
# Step 3: Upload PNG image (type=other)
# ============================================
Write-Host "[Step 3] Upload PNG (type=other)..." -ForegroundColor Yellow

try {
    $pngFile = Get-Item "$imagesDir\png测试图片.png"
    $uploadResult2 = Invoke-RestMethod -Uri "$baseUrl/images" -Method POST `
        -Form @{ file = $pngFile; type = "other" } `
        -Headers $headers
    if ($uploadResult2.code -eq 200) {
        $imageId2 = $uploadResult2.data.imageId
        Write-Host "  Upload OK: imageId=$imageId2" -ForegroundColor Green
    } else {
        Write-Host "  Upload Failed: code=$($uploadResult2.code)" -ForegroundColor Red
    }
} catch {
    Write-Host "  Upload Error: $_" -ForegroundColor Red
}
Write-Host ""

# ============================================
# Step 3.5: Test auth - upload without Token
# ============================================
Write-Host "[Step 3.5] Test without Token (auth check)..." -ForegroundColor Yellow

try {
    $pngFile = Get-Item "$imagesDir\png测试图片.png"
    $noAuthResult = Invoke-RestMethod -Uri "$baseUrl/images" -Method POST `
        -Form @{ file = $pngFile; type = "other" }
    Write-Host "  Result: code=$($noAuthResult.code)" -ForegroundColor Red
} catch {
    Write-Host "  Blocked as expected (no token)" -ForegroundColor Green
}
Write-Host ""

# ============================================
# Step 4: Get image list (all, page 1)
# ============================================
Write-Host "[Step 4] Get all images (page=1, pageSize=10)..." -ForegroundColor Yellow

try {
    $listUrl = $baseUrl + '/images?page=1&pageSize=10'
    $listResult = Invoke-RestMethod -Uri $listUrl -Method GET -Headers $headers
    $total = $listResult.data.total
    Write-Host "  Result: code=$($listResult.code), total=$total, page=$($listResult.data.page)" -ForegroundColor Green
    foreach ($item in $listResult.data.list) {
        Write-Host ("    imageId={0}, fileName={1}, fileType={2}, uploadTime={3}" -f $item.imageId, $item.fileName, $item.fileType, $item.uploadTime) -ForegroundColor Gray
    }
} catch {
    Write-Host "  List Error: $_" -ForegroundColor Red
}
Write-Host ""

# ============================================
# Step 5: Filter by type=avatar
# ============================================
Write-Host "[Step 5] Filter by type=avatar..." -ForegroundColor Yellow

try {
    $filterUrl = $baseUrl + '/images?type=avatar&page=1&pageSize=10'
    $filterResult = Invoke-RestMethod -Uri $filterUrl -Method GET -Headers $headers
    Write-Host "  Result: code=$($filterResult.code), total=$($filterResult.data.total)" -ForegroundColor Green
    foreach ($item in $filterResult.data.list) {
        Write-Host ("    imageId={0}, fileName={1}" -f $item.imageId, $item.fileName) -ForegroundColor Gray
    }
} catch {
    Write-Host "  Filter Error: $_" -ForegroundColor Red
}
Write-Host ""

# ============================================
# Step 6: View image by ID (public, no token)
# ============================================
Write-Host "[Step 6] View image (public endpoint)..." -ForegroundColor Yellow

if ($imageId1) {
    try {
        $viewUrl = "$baseUrl/images/$imageId1/view"
        $viewResult = Invoke-WebRequest -Uri $viewUrl -Method GET
        Write-Host "  Status: $($viewResult.StatusCode)" -ForegroundColor Green
        Write-Host "  Content-Type: $($viewResult.Headers['Content-Type'])" -ForegroundColor Gray
        Write-Host "  Content-Length: $($viewResult.RawContentLength) bytes" -ForegroundColor Gray
    } catch {
        Write-Host "  View Error: $_" -ForegroundColor Red
    }
} else {
    Write-Host "  Skipped: no image uploaded" -ForegroundColor Gray
}
Write-Host ""

# ============================================
# Step 7: Delete uploaded images
# ============================================
Write-Host "[Step 7] Delete uploaded images..." -ForegroundColor Yellow

if ($imageId1) {
    try {
        $deleteResult1 = Invoke-RestMethod -Uri "$baseUrl/images/$imageId1" -Method DELETE -Headers $headers
        Write-Host ("  Delete imageId={0}: code={1}, msg={2}" -f $imageId1, $deleteResult1.code, $deleteResult1.message) -ForegroundColor Green
    } catch {
        Write-Host "  Delete Error: $_" -ForegroundColor Red
    }
}

if ($imageId2) {
    try {
        $deleteResult2 = Invoke-RestMethod -Uri "$baseUrl/images/$imageId2" -Method DELETE -Headers $headers
        Write-Host ("  Delete imageId={0}: code={1}, msg={2}" -f $imageId2, $deleteResult2.code, $deleteResult2.message) -ForegroundColor Green
    } catch {
        Write-Host "  Delete Error: $_" -ForegroundColor Red
    }
}
Write-Host ""

# ============================================
# Step 8: Verify after deletion
# ============================================
Write-Host "[Step 8] Verify list after deletion..." -ForegroundColor Yellow

try {
    $finalUrl = $baseUrl + '/images?page=1&pageSize=10'
    $finalList = Invoke-RestMethod -Uri $finalUrl -Method GET -Headers $headers
    Write-Host "  Current image count: $($finalList.data.total)" -ForegroundColor Green
} catch {
    Write-Host "  Verify Error: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Images Module API Test Done!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
