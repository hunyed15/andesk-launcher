# Download gradle-wrapper.jar
$url = "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle/wrapper/gradle-wrapper.jar"

Write-Host "Downloading gradle-wrapper.jar..."
Invoke-WebRequest -Uri $url -OutFile $output

if (Test-Path $output) {
    Write-Host "Download successful!"
    Write-Host ""
    Write-Host "Now run:"
    Write-Host "git add -f gradle/wrapper/gradle-wrapper.jar"
    Write-Host 'git commit -m "build: 添加gradle-wrapper.jar"'
    Write-Host "git push origin main"
} else {
    Write-Host "Download failed!"
}
