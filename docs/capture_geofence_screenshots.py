"""
Chụp screenshot cho tính năng Geofence / Xác thực chấm công GPS:
  - 14_office_locations.png : Trang Văn phòng (manager CRUD + bản đồ)
  - 15_my_attendance_map.png : Trang Chấm công của tôi với map + khoảng cách
  - 16_attendance_pending.png : Trang Chấm công (manager) với nút duyệt PENDING_REVIEW

Mock backend API + override navigator.geolocation để Leaflet hiển thị marker
vị trí giả lập (HUTECH ĐBP).
"""
import json
import os
import socket
import subprocess
import sys
import time
from pathlib import Path

from playwright.sync_api import sync_playwright

ROOT = Path(r"C:\Users\Admin\taskhub")
FRONTEND = ROOT / "frontend"
OUT = ROOT / "docs" / "screenshots"
OUT.mkdir(parents=True, exist_ok=True)

FE_PORT = 5173

# Toạ độ giả lập – HUTECH ĐBP
FAKE_LAT = 10.80246
FAKE_LNG = 106.71607


def wait_for_port(host: str, port: int, timeout: float = 90.0) -> bool:
    end = time.time() + timeout
    while time.time() < end:
        with socket.socket() as s:
            s.settimeout(1.0)
            try:
                s.connect((host, port))
                return True
            except OSError:
                time.sleep(0.5)
    return False


OFFICES = [
    dict(id=1, name="Trụ sở chính HUTECH",
         address="475A Điện Biên Phủ, Bình Thạnh, TP.HCM",
         latitude=10.8021, longitude=106.7159,
         radiusMeters=100, status="ACTIVE"),
    dict(id=2, name="Chi nhánh Q1",
         address="65 Đồng Khởi, Bến Nghé, Q1, TP.HCM",
         latitude=10.7769, longitude=106.7009,
         radiusMeters=80, status="ACTIVE"),
    dict(id=3, name="Kho Tân Bình",
         address="Trường Chinh, Q. Tân Bình, TP.HCM",
         latitude=10.8005, longitude=106.6504,
         radiusMeters=120, status="INACTIVE"),
]

# Bản ghi chấm công với đủ trạng thái (APPROVED / PENDING_REVIEW)
ATTENDANCE = [
    dict(attendanceId=1, date="2026-05-13",
         checkIn="08:05:12", checkOut=None,
         checkInLat=10.80246, checkInLng=106.71607,
         checkInOffice=dict(id=1, name="Trụ sở chính HUTECH",
                            latitude=10.8021, longitude=106.7159, radiusMeters=100),
         checkInDistanceMeters=44, reviewStatus="APPROVED",
         employee=dict(employeeId=1, firstName="Nguyễn Văn", lastName="An")),
    dict(attendanceId=2, date="2026-05-12",
         checkIn="08:35:21", checkOut="17:32:08",
         checkInLat=10.7770, checkInLng=106.7011,
         checkInOffice=dict(id=2, name="Chi nhánh Q1",
                            latitude=10.7769, longitude=106.7009, radiusMeters=80),
         checkInDistanceMeters=22, reviewStatus="APPROVED",
         employee=dict(employeeId=1, firstName="Nguyễn Văn", lastName="An")),
    dict(attendanceId=3, date="2026-05-11",
         checkIn="09:12:00", checkOut="17:28:00",
         checkInLat=10.7800, checkInLng=106.7100,
         checkInOffice=dict(id=1, name="Trụ sở chính HUTECH",
                            latitude=10.8021, longitude=106.7159, radiusMeters=100),
         checkInDistanceMeters=2710, reviewStatus="PENDING_REVIEW",
         employee=dict(employeeId=1, firstName="Nguyễn Văn", lastName="An")),
    dict(attendanceId=4, date="2026-05-10",
         checkIn="08:11:00", checkOut="17:30:00",
         checkInLat=10.8022, checkInLng=106.7158,
         checkInOffice=dict(id=1, name="Trụ sở chính HUTECH",
                            latitude=10.8021, longitude=106.7159, radiusMeters=100),
         checkInDistanceMeters=12, reviewStatus="APPROVED",
         employee=dict(employeeId=2, firstName="Trần Thị", lastName="Bình")),
    dict(attendanceId=5, date="2026-05-09",
         checkIn="08:45:00", checkOut=None,
         checkInLat=10.7000, checkInLng=106.6500,
         checkInOffice=None, checkInDistanceMeters=None,
         reviewStatus="PENDING_REVIEW", isMocked=True,
         employee=dict(employeeId=2, firstName="Trần Thị", lastName="Bình")),
]

EMPLOYEES = [
    dict(employeeId=1, firstName="Nguyễn Văn", lastName="An",
         department="Phát triển phần mềm", position="Backend Developer"),
    dict(employeeId=2, firstName="Trần Thị", lastName="Bình",
         department="Phát triển phần mềm", position="Frontend Developer"),
]


def ok(data):
    return dict(success=True, message="OK", data=data)


def install_routes(page):
    def handle(route):
        url = route.request.url
        method = route.request.method
        path = url.split("?")[0]
        if path.endswith("/api/office-locations") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(OFFICES)))
        if path.endswith("/api/employees") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(EMPLOYEES)))
        if path.endswith("/api/attendance") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(ATTENDANCE)))
        if path.endswith("/api/attendance/me") and method == "GET":
            mine = [a for a in ATTENDANCE if a["employee"]["employeeId"] == 1]
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(mine)))
        return route.fulfill(status=200, content_type="application/json",
                             body=json.dumps(ok([])))

    page.route("http://localhost:5000/api/**", handle)
    page.route("http://127.0.0.1:5000/api/**", handle)


def inject_geolocation(page, lat, lng):
    """Override navigator.geolocation để trả về toạ độ giả lập (không cần OS prompt)."""
    page.add_init_script(f"""
        Object.defineProperty(navigator, 'geolocation', {{
          value: {{
            getCurrentPosition: (success) => success({{
              coords: {{ latitude: {lat}, longitude: {lng},
                         accuracy: 8, altitude: null, altitudeAccuracy: null,
                         heading: null, speed: null }},
              timestamp: Date.now()
            }}),
            watchPosition: () => 0,
            clearWatch: () => {{}}
          }},
          configurable: true
        }});
    """)


def main():
    # Vite dev server có vấn đề 504 Outdated Optimize Dep với react-leaflet 4
    # khi navigate liên tục. Build production trước rồi dùng `vite preview`
    # cho ổn định và nhanh hơn nhiều.
    print("[1/3] Build production frontend...")
    env = os.environ.copy()
    env["VITE_API_BASE_URL"] = "http://localhost:5000"
    build = subprocess.run(
        ["npm.cmd", "run", "build"],
        cwd=str(FRONTEND), env=env,
        stdout=subprocess.DEVNULL, stderr=subprocess.STDOUT,
        shell=False,
    )
    if build.returncode != 0:
        print("Build thất bại.", file=sys.stderr)
        return 1
    print("    -> dist/ ready")

    print("[1b/3] Khởi động vite preview server...")
    proc = subprocess.Popen(
        ["npm.cmd", "run", "preview", "--", "--host", "127.0.0.1", "--port", str(FE_PORT)],
        cwd=str(FRONTEND), env=env,
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
        shell=False,
        creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0,
    )
    try:
        if not wait_for_port("127.0.0.1", FE_PORT, timeout=60):
            print("Preview server không khởi động được.", file=sys.stderr)
            return 1
        print(f"    -> preview server ready @ http://127.0.0.1:{FE_PORT}")
        time.sleep(3)

        print("[2/3] Mở Chromium + mock API + override geolocation...")
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            ctx = browser.new_context(
                viewport={"width": 1440, "height": 900},
                locale="vi-VN",
                permissions=["geolocation"],
                geolocation={"latitude": FAKE_LAT, "longitude": FAKE_LNG},
            )
            ctx.add_init_script("""
                try {
                    localStorage.setItem('token', 'demo.jwt.token.for.screenshot');
                    localStorage.setItem('user', JSON.stringify({
                        username: 'manager', email: 'manager@hutech.edu.vn',
                        role: 'MANAGER'
                    }));
                } catch(e) {}
            """)
            page = ctx.new_page()
            page.on("pageerror", lambda e: print(f"   [JS ERROR] {e}"))
            page.on("console", lambda m: print(f"   [{m.type.upper()}] {m.text}") if m.type in ("error", "warning") else None)
            install_routes(page)
            inject_geolocation(page, FAKE_LAT, FAKE_LNG)

            base = f"http://127.0.0.1:{FE_PORT}"

            def goto(path_url, wait_ms=4500):
                page.goto(base + path_url, wait_until="domcontentloaded", timeout=60000)
                try:
                    page.wait_for_function(
                        "document.getElementById('root') && "
                        "document.getElementById('root').innerHTML.length > 100",
                        timeout=60000,
                    )
                except Exception:
                    pass
                page.wait_for_timeout(wait_ms)

            print("[3/3] Chụp...")

            # 14: Office locations (manager)
            goto("/office-locations", wait_ms=4000)
            # Chờ Leaflet tile tải xong
            try:
                page.wait_for_selector(".leaflet-container", timeout=15000)
                page.wait_for_timeout(5000)  # cho tiles tải
            except Exception:
                page.wait_for_timeout(4000)
            page.screenshot(path=str(OUT / "14_office_locations.png"), full_page=True)
            print("    -> 14_office_locations.png")

            # 15: My attendance with map (employee role)
            page.evaluate("""
                () => {
                    localStorage.setItem('user', JSON.stringify({
                        username: 'binhtt', email: 'binh.tt@company.vn',
                        role: 'EMPLOYEE'
                    }));
                }
            """)
            goto("/my-attendance", wait_ms=6000)
            try:
                page.wait_for_selector(".leaflet-container", timeout=15000)
                page.wait_for_timeout(5000)
            except Exception:
                page.wait_for_timeout(4000)
            page.screenshot(path=str(OUT / "15_my_attendance_map.png"), full_page=True)
            print("    -> 15_my_attendance_map.png")

            # 16: Attendance manager view (with pending review actions)
            page.evaluate("""
                () => {
                    localStorage.setItem('user', JSON.stringify({
                        username: 'manager', email: 'manager@hutech.edu.vn',
                        role: 'MANAGER'
                    }));
                }
            """)
            goto("/attendance", wait_ms=4500)
            page.screenshot(path=str(OUT / "16_attendance_pending.png"), full_page=True)
            print("    -> 16_attendance_pending.png")

            browser.close()
        print("[OK] Done.")
        return 0
    finally:
        try:
            proc.terminate()
            try: proc.wait(timeout=10)
            except subprocess.TimeoutExpired: proc.kill()
        except Exception:
            pass


if __name__ == "__main__":
    sys.exit(main())
