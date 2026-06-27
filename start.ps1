#requires -Version 5.1
<#
Khởi động toàn bộ Task Management System (Windows / PowerShell).
Chạy backend (Spring Boot), frontend (Vite), và mobile-web (Flutter build).

Sử dụng:
  pwsh -File start.ps1            # khởi động cả 3
  pwsh -File start.ps1 -Stop      # dừng các tiến trình đã start
  pwsh -File start.ps1 -SkipMobile # bỏ qua build mobile web
#>
[CmdletBinding()]
param(
    [switch]$Stop,
    [switch]$SkipMobile
)

$ErrorActionPreference = 'Stop'
$Root = $PSScriptRoot
$PidFile = Join-Path $Root '.start_pids.txt'

function Stop-Started {
    if (-not (Test-Path $PidFile)) { Write-Host 'Không có tiến trình nào đang chạy.'; return }
    foreach ($procId in Get-Content $PidFile) {
        try {
            $p = Get-Process -Id $procId -ErrorAction Stop
            Stop-Process -Id $procId -Force
            Write-Host "✓ Đã dừng PID $procId ($($p.ProcessName))"
        } catch { Write-Host "(PID $procId không còn chạy)" }
    }
    Remove-Item $PidFile -Force
}

$AppPorts = @(5000, 5173, 5170, 35729)

function Free-Ports {
    foreach ($port in $AppPorts) {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        foreach ($procId in ($conns.OwningProcess | Select-Object -Unique)) {
            try {
                $p = Get-Process -Id $procId -ErrorAction Stop
                Stop-Process -Id $procId -Force
                Write-Host "✓ Giải phóng port $port (đã dừng PID $procId / $($p.ProcessName))"
            } catch { }
        }
    }
}

if ($Stop) { Stop-Started; Free-Ports; return }

# ---- Dọn lần chạy trước để tránh lỗi 'port already in use' ----
Stop-Started
Free-Ports

# ---- Kiểm tra tooling ----
function Need($cmd, $hint) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        throw "Thiếu '$cmd'. $hint"
    }
}
Need 'java' 'Cài JDK 17+ và thêm vào PATH.'
Need 'mvn'  'Cài Apache Maven và thêm vào PATH.'
Need 'node' 'Cài Node.js 18+ và thêm vào PATH.'
Need 'npm'  'npm đi kèm Node.js.'
if (-not $SkipMobile) { Need 'flutter' 'Cài Flutter SDK và thêm vào PATH (hoặc dùng -SkipMobile).' }

# ---- Env mặc định (dev) ----
if (-not $env:JWT_SECRET)     { $env:JWT_SECRET = 'dev_secret_change_me_at_least_256_bits_long_xxxxxxxxxxxxxxxx' }
if (-not $env:ADMIN_PASSWORD) { $env:ADMIN_PASSWORD = 'Admin@12345' }

Write-Host '─────────────────────────────────────────────'
Write-Host '  TASK MANAGEMENT SYSTEM – khởi động dev mode'
Write-Host '─────────────────────────────────────────────'

$pids = @()

# 1) Backend
Write-Host '[1/3] Backend Spring Boot (port 5000)...'
$be = Start-Process -FilePath 'mvn.cmd' -ArgumentList @('-q', 'spring-boot:run') `
    -WorkingDirectory (Join-Path $Root 'backend') -PassThru -WindowStyle Minimized
$pids += $be.Id

# 2) Frontend
Write-Host '[2/3] Frontend Vite (port 5173)...'
$frontDir = Join-Path $Root 'frontend'
if (-not (Test-Path (Join-Path $frontDir 'node_modules'))) {
    Write-Host '    npm install (lần đầu)...'
    Push-Location $frontDir
    & npm.cmd install --no-audit --no-fund
    Pop-Location
}
$fe = Start-Process -FilePath 'npm.cmd' -ArgumentList @('run','dev') `
    -WorkingDirectory $frontDir -PassThru -WindowStyle Minimized
$pids += $fe.Id

# 3) Mobile web
if (-not $SkipMobile) {
    Write-Host '[3/3] Mobile Flutter Web (port 5170)...'
    $mobileDir = Join-Path $Root 'mobile'
    Push-Location $mobileDir
    & flutter build web --release --dart-define=API_BASE_URL=http://localhost:5000 | Out-Null
    Pop-Location
    # Serve thư mục build/web bằng python http server (Python đi kèm Win 11) hoặc npx serve
    $serverCmd = if (Get-Command python -ErrorAction SilentlyContinue) {
        @('python', '-m', 'http.server', '5170')
    } else {
        @('npx.cmd', '--yes', 'http-server', '-p', '5170', '-s')
    }
    $mw = Start-Process -FilePath $serverCmd[0] -ArgumentList $serverCmd[1..($serverCmd.Length-1)] `
        -WorkingDirectory (Join-Path $mobileDir 'build/web') -PassThru -WindowStyle Minimized
    $pids += $mw.Id
} else {
    Write-Host '[3/3] Bỏ qua mobile (-SkipMobile)'
}

$pids | Out-File -FilePath $PidFile -Encoding ASCII

Write-Host ''
Write-Host '✓ Đã khởi chạy. Mở các URL:'
Write-Host '   Backend API   : http://localhost:5000/swagger-ui.html'
Write-Host '   Frontend Web  : http://localhost:5173'
if (-not $SkipMobile) {
Write-Host '   Mobile (Web)  : http://localhost:5170'
}
Write-Host ''
Write-Host "Để dừng: pwsh -File start.ps1 -Stop"
