"""
Chụp screenshot ứng dụng Flutter mobile (build web) ở viewport mobile (412x915 –
Pixel 7) với user-agent Android. Mock backend API bằng Playwright route intercept.

Output: docs/screenshots/mobile/*.png
Yêu cầu: đã chạy `flutter build web --dart-define=API_BASE_URL=http://localhost:5050`
trong thư mục mobile/ trước đó.
"""
import http.server
import json
import os
import socket
import socketserver
import sys
import threading
import time
from pathlib import Path

from playwright.sync_api import sync_playwright

ROOT = Path(r"C:\Users\Admin\taskhub")
WEB_DIR = ROOT / "mobile" / "build" / "web"
OUT = ROOT / "docs" / "screenshots" / "mobile"
OUT.mkdir(parents=True, exist_ok=True)

WEB_PORT = 5170
API_BASE = "http://localhost:5050"  # phải khớp --dart-define=API_BASE_URL

VIEWPORT = {"width": 412, "height": 915}
UA = ("Mozilla/5.0 (Linux; Android 14; Pixel 7) "
      "AppleWebKit/537.36 (KHTML, like Gecko) "
      "Chrome/126.0.0.0 Mobile Safari/537.36")


# ---------------- Mock data (dùng lại từ capture_screenshots.py) ----------------
EMPLOYEES = [
    dict(employeeId=1, firstName="Nguyễn Văn", lastName="An", email="an.nv@company.vn",
         phone="0901234567", department="Phát triển phần mềm",
         position="Backend Developer", status="ACTIVE"),
    dict(employeeId=2, firstName="Trần Thị", lastName="Bình", email="binh.tt@company.vn",
         phone="0902345678", department="Phát triển phần mềm",
         position="Frontend Developer", status="ACTIVE"),
    dict(employeeId=3, firstName="Lê Minh", lastName="Cường", email="cuong.lm@company.vn",
         phone="0903456789", department="Thiết kế đồ hoạ",
         position="UI/UX Designer", status="ACTIVE"),
    dict(employeeId=4, firstName="Phạm Thu", lastName="Dung", email="dung.pt@company.vn",
         phone="0904567890", department="Marketing",
         position="Marketing Lead", status="ACTIVE"),
    dict(employeeId=5, firstName="Hoàng Quốc", lastName="Em", email="em.hq@company.vn",
         phone="0905678901", department="Phát triển phần mềm",
         position="Mobile Developer", status="ACTIVE"),
    dict(employeeId=6, firstName="Vũ Hà", lastName="Phương", email="phuong.vh@company.vn",
         phone="0906789012", department="Kinh doanh",
         position="Sales Manager", status="ACTIVE"),
]

PROJECTS = [
    dict(projectId=1, name="Website thương mại điện tử cho ABC Mart",
         description="Xây dựng nền tảng e-commerce cho chuỗi siêu thị ABC",
         startDate="2026-02-01", endDate="2026-06-30", status="ACTIVE"),
    dict(projectId=2, name="App giao hàng cho Foody Việt",
         description="Ứng dụng mobile đặt món ăn online",
         startDate="2026-03-15", endDate="2026-08-15", status="ACTIVE"),
    dict(projectId=3, name="Hệ thống quản lý kho",
         description="Phần mềm quản lý xuất nhập kho cho doanh nghiệp SME",
         startDate="2026-01-10", endDate="2026-04-30", status="COMPLETED"),
    dict(projectId=4, name="Chiến dịch marketing Tết 2026",
         description="Lên kế hoạch và triển khai chiến dịch tổng thể dịp Tết",
         startDate="2025-12-01", endDate="2026-02-15", status="COMPLETED"),
    dict(projectId=5, name="Redesign logo và bộ nhận diện",
         description="Thiết kế lại logo và brand identity cho khách hàng XYZ",
         startDate="2026-04-01", endDate="2026-05-31", status="PLANNING"),
]


def emp_ref(i):
    e = EMPLOYEES[i - 1]
    return {"employeeId": e["employeeId"], "firstName": e["firstName"],
            "lastName": e["lastName"]}


def proj_ref(i):
    p = PROJECTS[i - 1]
    return {"projectId": p["projectId"], "name": p["name"]}


TASKS = [
    dict(taskId=1, title="Thiết kế ERD cho hệ thống đặt hàng",
         description="Phân tích yêu cầu và vẽ sơ đồ ERD chi tiết.",
         status="completed", priority="HIGH", dueDate="2026-02-15"),
    dict(taskId=2, title="Cài đặt Spring Security + JWT",
         description="Triển khai bảo mật JWT cho toàn bộ REST API.",
         status="completed", priority="HIGH", dueDate="2026-02-28"),
    dict(taskId=3, title="Xây dựng trang quản lý sản phẩm",
         description="Trang admin CRUD cho sản phẩm, danh mục, kho.",
         status="in_progress", priority="HIGH", dueDate="2026-04-15"),
    dict(taskId=4, title="Thiết kế giao diện checkout",
         description="Wireframe và hi-fi mockup cho luồng thanh toán.",
         status="in_progress", priority="MEDIUM", dueDate="2026-05-10"),
    dict(taskId=5, title="Triển khai chức năng đặt món",
         description="Module chính cho phép user đặt món ăn từ app.",
         status="in_progress", priority="URGENT", dueDate="2026-05-15"),
    dict(taskId=6, title="Tích hợp cổng thanh toán VNPAY",
         description="Tích hợp VNPAY + MoMo + Apple Pay.",
         status="pending", priority="HIGH", dueDate="2026-06-01"),
]
_TASK_MAP = [(1, 1), (1, 1), (1, 2), (1, 3), (2, 5), (2, 1)]
for t, (pi, ei) in zip(TASKS, _TASK_MAP):
    t["project"] = proj_ref(pi)
    t["assignedTo"] = emp_ref(ei)


today = time.strftime("%Y-%m-%d")
ATTENDANCE = []
for i, day in enumerate(range(1, 12), start=1):
    ATTENDANCE.append(dict(
        attendanceId=i, date=f"2026-05-{day:02d}",
        status="PRESENT" if day % 7 not in (0, 6) else "ABSENT",
        checkIn="08:00:00", checkOut="17:30:00",
        notes="", employee=emp_ref(1),
    ))
# Bản ghi hôm nay để dashboard hiển thị "Chấm công hôm nay = 1"
ATTENDANCE.append(dict(
    attendanceId=99, date=today, status="PRESENT",
    checkIn="08:05:00", checkOut="", notes="",
    employee=emp_ref(1),
))


SUGGESTION_RESPONSE = [
    dict(rank=1, employeeId=1, firstName="Nguyễn Văn", lastName="An",
         department="Phát triển phần mềm", position="Backend Developer",
         reasoning=(
             "Khớp hoàn toàn kỹ năng yêu cầu (Java, Spring Boot, PostgreSQL). "
             "Đã hoàn thành 95% công việc đúng hạn trong 6 tháng qua. "
             "Hiện đang phụ trách 2 task IN_PROGRESS – workload vừa phải."
         )),
    dict(rank=2, employeeId=5, firstName="Hoàng Quốc", lastName="Em",
         department="Phát triển phần mềm", position="Mobile Developer",
         reasoning=(
             "Có kinh nghiệm 3 năm với Java (bên cạnh Flutter/Dart). "
             "Hiệu suất hoàn thành 88%, chuyên cần tốt."
         )),
    dict(rank=3, employeeId=2, firstName="Trần Thị", lastName="Bình",
         department="Phát triển phần mềm", position="Frontend Developer",
         reasoning=(
             "Quen thuộc với Spring Boot từ các dự án full-stack. "
             "Khớp 1/3 kỹ năng yêu cầu nhưng học hỏi nhanh."
         )),
]


def ok(data):
    return dict(success=True, message="OK", data=data)


# ---------------- HTTP server cho mobile/build/web ----------------
class QuietHandler(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *a, **k):  # tắt log
        pass

    def end_headers(self):
        # Flutter web (CanvasKit/Skwasm) cần COOP/COEP để chạy SharedArrayBuffer
        self.send_header("Cross-Origin-Opener-Policy", "same-origin")
        self.send_header("Cross-Origin-Embedder-Policy", "credentialless")
        super().end_headers()


def start_static_server(directory: Path, port: int):
    os.chdir(directory)
    handler = QuietHandler
    httpd = socketserver.TCPServer(("127.0.0.1", port), handler)
    httpd.allow_reuse_address = True
    t = threading.Thread(target=httpd.serve_forever, daemon=True)
    t.start()
    return httpd


def wait_for_port(host, port, timeout=20.0):
    end = time.time() + timeout
    while time.time() < end:
        with socket.socket() as s:
            s.settimeout(0.5)
            try:
                s.connect((host, port))
                return True
            except OSError:
                time.sleep(0.3)
    return False


# ---------------- Mock API routes ----------------
def install_routes(page):
    cors_headers = {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
        "Access-Control-Allow-Headers": "Content-Type, Authorization",
        "Access-Control-Max-Age": "86400",
    }

    def fulfill(route, body=None, status=200):
        route.fulfill(
            status=status,
            headers={"Content-Type": "application/json", **cors_headers},
            body=json.dumps(body if body is not None else ok([])),
        )

    def handle(route):
        url = route.request.url
        method = route.request.method
        path = url.split("?")[0]

        # CORS preflight cho mọi route
        if method == "OPTIONS":
            return route.fulfill(status=204, headers=cors_headers, body="")

        # Auth
        if path.endswith("/api/auth/login") and method == "POST":
            return fulfill(route, ok({
                "token": "demo.jwt.mobile.token",
                "username": "manager",
                "email": "manager@hutech.edu.vn",
            }))
        if path.endswith("/api/auth/register") and method == "POST":
            return fulfill(route, ok({"message": "registered"}))

        # Domain data
        if path.endswith("/api/employees") and method == "GET":
            return fulfill(route, ok(EMPLOYEES))
        if path.endswith("/api/projects") and method == "GET":
            return fulfill(route, ok(PROJECTS))
        if path.endswith("/api/tasks") and method == "GET":
            return fulfill(route, ok(TASKS))
        if path.endswith("/api/attendance") and method == "GET":
            return fulfill(route, ok(ATTENDANCE))
        if path.endswith("/api/suggestions/recommend") and method == "POST":
            time.sleep(0.8)
            return fulfill(route, ok(SUGGESTION_RESPONSE))

        # Default OK rỗng
        return fulfill(route)

    page.route(f"{API_BASE}/api/**", handle)


def main():
    print(f"[1/4] Khởi động static server cho {WEB_DIR}...")
    if not WEB_DIR.exists():
        print("Không tìm thấy build web. Chạy lại flutter build web trước.",
              file=sys.stderr)
        return 1

    httpd = start_static_server(WEB_DIR, WEB_PORT)
    try:
        if not wait_for_port("127.0.0.1", WEB_PORT):
            print("Static server không khởi động được.", file=sys.stderr)
            return 1
        print(f"    -> serving http://127.0.0.1:{WEB_PORT}")

        print("[2/4] Mở Chromium ở viewport mobile...")
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            ctx = browser.new_context(
                viewport=VIEWPORT,
                user_agent=UA,
                is_mobile=True,
                has_touch=True,
                device_scale_factor=2.0,
                locale="vi-VN",
            )
            page = ctx.new_page()

            install_routes(page)

            base = f"http://127.0.0.1:{WEB_PORT}"

            def goto(path_str, wait_ms=6000):
                page.goto(base + path_str, wait_until="domcontentloaded",
                          timeout=60000)
                # Flutter web cần thời gian để Skwasm/CanvasKit load
                try:
                    page.wait_for_function(
                        "document.querySelector('flutter-view') !== null || "
                        "document.querySelector('flt-glass-pane') !== null",
                        timeout=60000,
                    )
                except Exception:
                    pass
                page.wait_for_timeout(wait_ms)

            def shot(name):
                path = OUT / name
                page.screenshot(path=str(path), full_page=False)
                print(f"    -> {name}")

            def tap(x, y):
                # Flutter web mobile context: dùng touchscreen.tap thay vì
                # mouse.click vì Flutter render canvas, listen pointer event.
                page.touchscreen.tap(x, y)

            print("[3/4] Chụp từng màn hình...")

            # 1) LOGIN
            goto("/#/login", wait_ms=8000)
            shot("m01_login.png")

            # 2) REGISTER
            goto("/#/register", wait_ms=5000)
            shot("m02_register.png")

            # 3) Pre-seed SharedPreferences trong localStorage để bypass form login.
            # Flutter web shared_preferences lưu với prefix "flutter."
            # Cách hoạt động: AuthProvider._restoreSession() chạy async ở
            # constructor. AuthGate watch state → khi token được đọc xong,
            # notifyListeners fire → AuthGate tự redirect /login → /dashboard.
            # Vì vậy cần (a) set localStorage, (b) full reload, (c) vào /#/login
            # và chờ AuthGate redirect.
            # Flutter shared_preferences_web JSON-encodes giá trị
            # → string phải được wrap trong quotes thêm 1 lần.
            page.evaluate("""
                () => {
                    localStorage.setItem('flutter.jwt_token', JSON.stringify('demo.jwt.mobile.token'));
                    localStorage.setItem('flutter.username', JSON.stringify('manager'));
                    localStorage.setItem('flutter.email', JSON.stringify('manager@hutech.edu.vn'));
                }
            """)
            # Full reload với URL khác để Flutter chắc chắn boot lại
            page.goto(f"http://127.0.0.1:{WEB_PORT}/?reload=1",
                      wait_until="domcontentloaded", timeout=60000)
            try:
                page.wait_for_function(
                    "document.querySelector('flutter-view') !== null || "
                    "document.querySelector('flt-glass-pane') !== null",
                    timeout=60000,
                )
            except Exception:
                pass
            # Flutter mặc định route '/' = LoginScreen wrap trong AuthGate.
            # AuthGate sẽ redirect /dashboard sau khi _restoreSession xong.
            # Chờ đủ lâu cho restore async + redirect + render dashboard.
            page.wait_for_timeout(9000)
            shot("m03_dashboard.png")

            # 4) BOTTOM NAV: 5 tabs đều x = (i+0.5) * 412/5
            #    y = viewport.height - 28 = 887
            tab_y = VIEWPORT["height"] - 28
            tab_xs = [int((i + 0.5) * VIEWPORT["width"] / 5) for i in range(5)]
            for idx, (x, fname) in enumerate([
                (tab_xs[1], "m04_employees.png"),
                (tab_xs[2], "m05_projects.png"),
                (tab_xs[3], "m06_tasks.png"),
                (tab_xs[4], "m07_attendance.png"),
            ]):
                tap(x, tab_y)
                page.wait_for_timeout(2800)
                shot(fname)

            # Quay về tab Tổng quan để bấm AI từ AppBar
            tap(tab_xs[0], tab_y)
            page.wait_for_timeout(1800)

            # 5) AI gợi ý: AppBar có 2 action (AI auto_awesome + 3-dots).
            # AppBar height = 56, IconButton size = 48 → tâm y ≈ 28.
            # 3-dots ở cuối cùng (x = width - 24), AI ngay trước (x = width - 72).
            ai_x = VIEWPORT["width"] - 72
            tap(ai_x, 28)
            page.wait_for_timeout(3500)
            shot("m08_ai_suggestions.png")

            # 6) Form AI sau redesign:
            #    Hero 130px + AppBar 56 + 16+16 margin → form card top ~y=222
            #    Form padding 18 → title field center ≈ y=252
            #    + 12 spacing + textarea (3 lines ≈ 96) → desc center ≈ y=340
            #    + 16 spacing + button (50) → button center ≈ y=435
            tap(206, 252)
            page.wait_for_timeout(400)
            page.keyboard.type(
                "Backend RESTful API hệ thống đặt hàng", delay=10)
            page.wait_for_timeout(300)
            tap(206, 340)
            page.wait_for_timeout(400)
            page.keyboard.type(
                "Cần dev backend Java Spring Boot 3.x, PostgreSQL, JWT, Redis.",
                delay=10)
            page.wait_for_timeout(300)
            tap(206, 435)
            page.wait_for_timeout(5500)
            shot("m09_ai_result.png")

            browser.close()

        print("[4/4] Done.")
        return 0
    finally:
        try:
            httpd.shutdown()
        except Exception:
            pass


if __name__ == "__main__":
    sys.exit(main())
