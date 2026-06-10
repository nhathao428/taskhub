#!/usr/bin/env bash
# Khởi động toàn bộ TaskHub (Linux / macOS).
# Chạy backend (Spring Boot), frontend (Vite) và mobile-web (Flutter build).
#
# Sử dụng:
#   ./start.sh             # khởi động cả 3
#   ./start.sh stop        # dừng các tiến trình đã start
#   SKIP_MOBILE=1 ./start.sh   # bỏ qua mobile

set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$ROOT/.start_pids"

stop_started() {
    if [[ ! -f $PID_FILE ]]; then
        echo "Không có tiến trình nào đang chạy."
        return
    fi
    while IFS= read -r pid; do
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" && echo "✓ Đã dừng PID $pid"
        else
            echo "(PID $pid không còn chạy)"
        fi
    done < "$PID_FILE"
    rm -f "$PID_FILE"
}

if [[ "${1:-}" == "stop" ]]; then
    stop_started
    exit 0
fi

need() {
    command -v "$1" >/dev/null 2>&1 || { echo "Thiếu '$1'. $2"; exit 1; }
}
need java    "Cài JDK 17+ và thêm vào PATH."
need mvn     "Cài Apache Maven và thêm vào PATH."
need node    "Cài Node.js 18+ và thêm vào PATH."
need npm     "npm đi kèm Node.js."
[[ -z "${SKIP_MOBILE:-}" ]] && need flutter "Cài Flutter SDK và thêm vào PATH (hoặc đặt SKIP_MOBILE=1)."

export JWT_SECRET="${JWT_SECRET:-dev_secret_change_me_at_least_256_bits_long_xxxxxxxxxxxxxxxx}"
export ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin@12345}"

echo "─────────────────────────────────────────────"
echo "  TASK MANAGEMENT SYSTEM – khởi động dev mode"
echo "─────────────────────────────────────────────"

mkdir -p "$ROOT/.logs"
: > "$PID_FILE"

# 1) Backend
echo "[1/3] Backend Spring Boot (port 5000)..."
( cd "$ROOT/backend" && nohup mvn -q spring-boot:run >"$ROOT/.logs/backend.log" 2>&1 & echo $! >>"$PID_FILE" )

# 2) Frontend
echo "[2/3] Frontend Vite (port 5173)..."
if [[ ! -d "$ROOT/frontend/node_modules" ]]; then
    echo "    npm install (lần đầu)..."
    ( cd "$ROOT/frontend" && npm install --no-audit --no-fund )
fi
( cd "$ROOT/frontend" && nohup npm run dev >"$ROOT/.logs/frontend.log" 2>&1 & echo $! >>"$PID_FILE" )

# 3) Mobile web
if [[ -z "${SKIP_MOBILE:-}" ]]; then
    echo "[3/3] Mobile Flutter Web (port 5170)..."
    ( cd "$ROOT/mobile" && flutter build web --release --dart-define=API_BASE_URL=http://localhost:5000 >"$ROOT/.logs/mobile_build.log" 2>&1 )
    if command -v python3 >/dev/null 2>&1; then
        ( cd "$ROOT/mobile/build/web" && nohup python3 -m http.server 5170 >"$ROOT/.logs/mobile.log" 2>&1 & echo $! >>"$PID_FILE" )
    else
        ( cd "$ROOT/mobile/build/web" && nohup npx --yes http-server -p 5170 -s >"$ROOT/.logs/mobile.log" 2>&1 & echo $! >>"$PID_FILE" )
    fi
else
    echo "[3/3] Bỏ qua mobile (SKIP_MOBILE=1)"
fi

echo
echo "✓ Đã khởi chạy. Mở các URL:"
echo "   Backend API   : http://localhost:5000/swagger-ui.html"
echo "   Frontend Web  : http://localhost:5173"
[[ -z "${SKIP_MOBILE:-}" ]] && echo "   Mobile (Web)  : http://localhost:5170"
echo
echo "Log: $ROOT/.logs/*.log"
echo "Để dừng: ./start.sh stop"
