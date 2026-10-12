# Smoke-check North Star REST API. Requires running server and DB.
# Usage: .\scripts\api-smoke.ps1 -BaseUrl http://localhost:8080 -Username admin -Password secret

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username = $env:NORTHSTAR_ADMIN_USER,
    [string]$Password = $env:NORTHSTAR_ADMIN_PASSWORD
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        $Body = $null,
        [int[]]$ExpectedStatus = @(200)
    )
    $uri = "$BaseUrl$Path"
    $params = @{
        Uri = $uri
        Method = $Method
        Headers = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 6)
    }
    try {
        $response = Invoke-WebRequest @params
    } catch {
        $response = $_.Exception.Response
        if ($null -eq $response) {
            Write-Host "FAIL $Method $Path -> $($_.Exception.Message)" -ForegroundColor Red
            return $null
        }
        $statusCode = [int]$response.StatusCode
        $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Close()
        if ($ExpectedStatus -notcontains $statusCode) {
            Write-Host "FAIL $Method $Path -> HTTP ${statusCode}: $content" -ForegroundColor Red
            return $null
        }
        Write-Host "OK   $Method $Path -> $statusCode" -ForegroundColor Green
        if ($content) {
            try { return $content | ConvertFrom-Json } catch { return $content }
        }
        return $null
    }
    if ($ExpectedStatus -notcontains $response.StatusCode) {
        Write-Host "FAIL $Method $Path -> HTTP $($response.StatusCode): $($response.Content)" -ForegroundColor Red
        return $null
    }
    Write-Host "OK   $Method $Path -> $($response.StatusCode)" -ForegroundColor Green
    if ($response.Content) {
        try { return $response.Content | ConvertFrom-Json } catch { return $response.Content }
    }
    return $null
}

Write-Host "=== Health ===" -ForegroundColor Cyan
Invoke-Api -Method GET -Path "/" | Out-Null

if (-not $Username -or -not $Password) {
    Write-Host "Skip authenticated checks: set -Username/-Password or NORTHSTAR_ADMIN_* env vars." -ForegroundColor Yellow
    exit 0
}

$login = Invoke-Api -Method POST -Path "/api/auth/login" -Body @{
    username = $Username
    password = $Password
}
if (-not $login -or -not $login.token) {
    Write-Host "Login failed." -ForegroundColor Red
    exit 1
}
$auth = @{ Authorization = "Bearer $($login.token)" }

Write-Host "`n=== Auth ===" -ForegroundColor Cyan
Invoke-Api -Method GET -Path "/api/auth/me" -Headers $auth | Out-Null

Write-Host "`n=== Core API ===" -ForegroundColor Cyan
Invoke-Api -Method GET -Path "/api/users" -Headers $auth | Out-Null
Invoke-Api -Method GET -Path "/api/cars" -Headers $auth | Out-Null
Invoke-Api -Method GET -Path "/api/applications" -Headers $auth | Out-Null
Invoke-Api -Method GET -Path "/api/completed_applications" -Headers $auth | Out-Null
Invoke-Api -Method GET -Path "/api/driver-messages" -Headers $auth | Out-Null
Invoke-Api -Method GET -Path "/api/routes" -Headers $auth | Out-Null
Invoke-Api -Method GET -Path "/api/route-settings" -Headers $auth | Out-Null

Write-Host "`n=== Analytics ===" -ForegroundColor Cyan
$list = Invoke-Api -Method GET -Path "/api/analytics" -Headers $auth
$routes = Invoke-Api -Method GET -Path "/api/routes" -Headers $auth
$routeId = $null
if ($routes -and $routes.routes -and $routes.routes.Count -gt 0) {
    $routeId = $routes.routes[0].id
} else {
    $createdRoute = Invoke-Api -Method POST -Path "/api/routes" -Headers $auth -ExpectedStatus @(201) -Body @{
        cityStart = "Smoke A"
        cityEnd = "Smoke B"
        platonKm = 10
        autodorKm = 5
        ollKm = 100
        citiesOnRoute = ""
    }
    if ($createdRoute) { $routeId = $createdRoute.id }
}

if ($routeId) {
    Invoke-Api -Method PUT -Path "/api/route-settings" -Headers $auth -Body @{
        driverSalary = 50.0
        platonCost = 3.2
    } | Out-Null
    $variant = Invoke-Api -Method POST -Path "/api/analytics" -Headers $auth -ExpectedStatus @(201) -Body @{
        numberOfRoad = 1
        dateStart = "2026-01-01"
        dateEnd = "2026-01-02"
        routeId = $routeId
        cost = 1000
        fuelCost = 500
    }
    if ($variant -and $variant.finalCost) {
        Write-Host "     finalCost=$($variant.finalCost)" -ForegroundColor DarkGray
    }
    Invoke-Api -Method GET -Path "/api/analytics" -Headers $auth | Out-Null
    Invoke-Api -Method DELETE -Path "/api/analytics" -Headers $auth -ExpectedStatus @(204) | Out-Null
} else {
    Write-Host "SKIP analytics variant (no route)" -ForegroundColor Yellow
}

Write-Host "`nDone." -ForegroundColor Cyan
