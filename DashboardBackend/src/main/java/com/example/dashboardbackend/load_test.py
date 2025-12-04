# Test by multiple record
$url = "http://localhost:8080/api/dashboard/ingest"
$secret = "axaipay_dashboard_secret_key_123" # Check application.properties

$merchants = @("Gacha Nexus", "Infinite Play", "Coffee House", "Tech Store", "Burger King", "Cinema City")
$categories = @("Visa", "Mastercard", "FPX", "TNG E-Wallet", "GrabPay")
$statuses = @("success", "success", "success", "success", "failed")

Write-Host "🚀 Starting Traffic Simulation... (Press Ctrl+C to stop)" -ForegroundColor Cyan

while ($true) {
$merchant = $merchants | Get-Random
$category = $categories | Get-Random
$status = $statuses | Get-Random

$dollars = Get-Random -Minimum 10 -Maximum 500
$cents = (Get-Random -Minimum 0 -Maximum 99) / 100
$amount = "{0:N2}" -f ($dollars + $cents)

$body = '{"id":null,"amount":' + $amount + ',"status":"' + $status + '","merchant":"' + $merchant + '","category":"' + $category + '","timestamp":null}'

$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$hashBytes = $hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($body))
$signature = [Convert]::ToBase64String($hashBytes)

try {
$response = Invoke-RestMethod -Uri $url `
-Method Post `
-Body ([System.Text.Encoding]::UTF8.GetBytes($body)) `
-ContentType "application/json; charset=utf-8" `
-Headers @{ "X-Signature" = $signature }

if ($status -eq "success") {
Write-Host "[$((Get-Date).ToString("HH:mm:ss"))] Sent: RM $amount ($merchant)" -ForegroundColor Green
} else {
    Write-Host "[$((Get-Date).ToString("HH:mm:ss"))] Sent: RM $amount ($merchant) - FAILED" -ForegroundColor Red
}
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Start-Sleep -Milliseconds (Get-Random -Minimum 200 -Maximum 1000)
}


# Check how many count
docker-compose exec db psql -U postgres -d axai_dashboard -c "SELECT COUNT(*) FROM transaction;"

# Remove container
docker-compose down -v

# Rebuild container
docker-compose up -d --build

# Using docker terminal to check db
docker exec -it axp_dashboard-db-1 psql -U postgres -d axai_dashboard -c "SELECT count(*) FROM transaction;"
# to exit
\q
