param(
    [string]$Version = "0.0.1-SNAPSHOT",
    [string]$Registry = "chronoshop"
)

$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$services = @("auth-service", "catalog-service", "order-service", "payment-service", "api-gateway", "notification-service")
$sha = (git rev-parse --short HEAD).Trim()

foreach ($svc in $services) {
    $image = "$Registry/$svc"
    Write-Host "Building $image ($Version, sha-$sha, latest)..." -ForegroundColor Cyan
    docker build -f "services/$svc/Dockerfile" `
        -t "${image}:$Version" `
        -t "${image}:sha-$sha" `
        -t "${image}:latest" `
        .
}

Write-Host "Building $Registry/frontend ($Version, sha-$sha, latest)..." -ForegroundColor Cyan
docker build -f "frontend/Dockerfile" `
    -t "$Registry/frontend:$Version" `
    -t "$Registry/frontend:sha-$sha" `
    -t "$Registry/frontend:latest" `
    frontend

Write-Host "`nGotovo. Slike:" -ForegroundColor Green
docker images "$Registry/*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
