# 测试 wttr.in 天气API
Write-Host "测试 wttr.in 天气API..." -ForegroundColor Cyan
Write-Host "免费，无需API Key" -ForegroundColor Green
$url = "https://wttr.in/Shanghai?format=j1&lang=zh"
try {
    $response = Invoke-RestMethod -Uri $url -TimeoutSec 10
    $current = $response.current_condition[0]
    Write-Host "温度: $($current.temp_C)°C" -ForegroundColor Yellow
    Write-Host "天气: $($current.weatherDesc[0].value)" -ForegroundColor Yellow
    Write-Host "湿度: $($current.humidity)%" -ForegroundColor Yellow
} catch {
    Write-Host "API 调用失败: $_" -ForegroundColor Red
}
