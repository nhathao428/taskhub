"""
Khởi động frontend dev server (Vite), mock backend API bằng Playwright route intercept,
chụp full-page screenshot cho 2 vai trò:
  - MANAGER: 9 màn hình chính (login → register → dashboard → employees → projects →
    tasks → attendance → ai_suggestions → ai_result)
  - EMPLOYEE: 4 màn hình self-service (dashboard cá nhân → công việc của tôi →
    chấm công của tôi → dự án)
Tất cả ảnh lưu vào docs/screenshots/.
"""
import json
import os
import socket
import subprocess
import sys
import time
from pathlib import Path

from playwright.sync_api import sync_playwright

ROOT = Path(r"C:\Users\Admin\task-management-system")
FRONTEND = ROOT / "frontend"
OUT = ROOT / "docs" / "screenshots"
OUT.mkdir(parents=True, exist_ok=True)

FE_PORT = 5173


def wait_for_port(host: str, port: int, timeout: float = 90.0) -> bool:
    end = time.time() + timeout
    while time.time() < end:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(1.0)
            try:
                s.connect((host, port))
                return True
            except (ConnectionRefusedError, OSError):
                time.sleep(0.5)
    return False


# ---------------- Mock API data ----------------
EMPLOYEES = [
    dict(id=1, firstName="Nguyễn Văn", lastName="An", email="an.nv@company.vn",
         phone="0901234567", department="Phát triển phần mềm",
         position="Backend Developer", status="ACTIVE"),
    dict(id=2, firstName="Trần Thị", lastName="Bình", email="binh.tt@company.vn",
         phone="0902345678", department="Phát triển phần mềm",
         position="Frontend Developer", status="ACTIVE"),
    dict(id=3, firstName="Lê Minh", lastName="Cường", email="cuong.lm@company.vn",
         phone="0903456789", department="Thiết kế đồ hoạ",
         position="UI/UX Designer", status="ACTIVE"),
    dict(id=4, firstName="Phạm Thu", lastName="Dung", email="dung.pt@company.vn",
         phone="0904567890", department="Marketing",
         position="Marketing Lead", status="ACTIVE"),
    dict(id=5, firstName="Hoàng Quốc", lastName="Em", email="em.hq@company.vn",
         phone="0905678901", department="Phát triển phần mềm",
         position="Mobile Developer", status="ACTIVE"),
    dict(id=6, firstName="Vũ Hà", lastName="Phương", email="phuong.vh@company.vn",
         phone="0906789012", department="Kinh doanh",
         position="Sales Manager", status="ACTIVE"),
]

PROJECTS = [
    dict(id=1, name="Website thương mại điện tử cho ABC Mart",
         description="Xây dựng nền tảng e-commerce cho chuỗi siêu thị ABC",
         startDate="2026-02-01", endDate="2026-06-30",
         status="ACTIVE", taskCount=18, completedTasks=10),
    dict(id=2, name="App giao hàng cho Foody Việt",
         description="Ứng dụng mobile đặt món ăn online",
         startDate="2026-03-15", endDate="2026-08-15",
         status="ACTIVE", taskCount=24, completedTasks=8),
    dict(id=3, name="Hệ thống quản lý kho",
         description="Phần mềm quản lý xuất nhập kho cho doanh nghiệp SME",
         startDate="2026-01-10", endDate="2026-04-30",
         status="COMPLETED", taskCount=15, completedTasks=15),
    dict(id=4, name="Chiến dịch marketing Tết 2026",
         description="Lên kế hoạch và triển khai chiến dịch tổng thể dịp Tết",
         startDate="2025-12-01", endDate="2026-02-15",
         status="COMPLETED", taskCount=12, completedTasks=12),
    dict(id=5, name="Redesign logo và bộ nhận diện",
         description="Thiết kế lại logo và brand identity cho khách hàng XYZ",
         startDate="2026-04-01", endDate="2026-05-31",
         status="PLANNING", taskCount=8, completedTasks=0),
]

def emp_ref(i):
    e = EMPLOYEES[i - 1]
    return {"employeeId": e["id"], "firstName": e["firstName"], "lastName": e["lastName"]}

def proj_ref(i):
    p = PROJECTS[i - 1]
    return {"projectId": p["id"], "name": p["name"]}

TASKS = [
    dict(id=1, title="Thiết kế ERD cho hệ thống đặt hàng",
         description="Phân tích yêu cầu và vẽ sơ đồ ERD chi tiết.",
         status="DONE", priority="HIGH", dueDate="2026-02-15",
         project=None, assignedTo=None),
    dict(id=2, title="Cài đặt Spring Security + JWT",
         description="Triển khai bảo mật JWT cho toàn bộ REST API.",
         status="DONE", priority="HIGH", dueDate="2026-02-28",
         project=None, assignedTo=None),
    dict(id=3, title="Xây dựng trang quản lý sản phẩm",
         description="Trang admin CRUD cho sản phẩm, danh mục, kho.",
         status="IN_PROGRESS", priority="HIGH", dueDate="2026-04-15",
         project=None, assignedTo=None),
    dict(id=4, title="Thiết kế giao diện checkout",
         description="Wireframe và hi-fi mockup cho luồng thanh toán.",
         status="IN_PROGRESS", priority="MEDIUM", dueDate="2026-05-10",
         project=None, assignedTo=None),
    dict(id=5, title="Triển khai chức năng đặt món",
         description="Module chính cho phép user đặt món ăn từ app.",
         status="IN_PROGRESS", priority="URGENT", dueDate="2026-05-15",
         project=None, assignedTo=None),
    dict(id=6, title="Tích hợp cổng thanh toán VNPAY",
         description="Tích hợp VNPAY + MoMo + Apple Pay.",
         status="TODO", priority="HIGH", dueDate="2026-06-01",
         project=None, assignedTo=None),
    dict(id=7, title="Viết content blog 5 bài chuẩn SEO",
         description="Blog post cho website thương hiệu, tối ưu SEO.",
         status="DONE", priority="MEDIUM", dueDate="2026-02-10",
         project=None, assignedTo=None),
    dict(id=8, title="Demo sản phẩm cho khách hàng",
         description="Buổi demo nội bộ trước khi gặp khách hàng.",
         status="TODO", priority="LOW", dueDate="2026-05-20",
         project=None, assignedTo=None),
    dict(id=9, title="Sửa bug đăng nhập trên iOS",
         description="Bug crash khi nhập password trên Safari iOS 17.",
         status="IN_PROGRESS", priority="URGENT", dueDate="2026-05-12",
         project=None, assignedTo=None),
    dict(id=10, title="Khảo sát đối thủ cạnh tranh",
         description="Tổng hợp phân tích 5 đối thủ cùng phân khúc.",
         status="DONE", priority="MEDIUM", dueDate="2026-03-20",
         project=None, assignedTo=None),
]
# Mapping task → (project, employee)
_TASK_MAP = [(1, 1), (1, 1), (1, 2), (1, 3), (2, 5),
             (2, 1), (4, 4), (2, 6), (2, 5), (5, 4)]
for t, (pi, ei) in zip(TASKS, _TASK_MAP):
    t["project"] = proj_ref(pi)
    t["assignedTo"] = emp_ref(ei)

ATTENDANCE = [
    dict(id=i, date=f"2026-05-{day:02d}",
         status="PRESENT" if day % 7 not in (0, 6) else "ABSENT",
         checkIn="08:00:00", checkOut="17:30:00",
         notes="", employee=emp_ref(1))
    for i, day in enumerate(range(1, 12), start=1)
]
for i, day in enumerate(range(1, 12), start=12):
    ATTENDANCE.append(dict(
        id=i, date=f"2026-05-{day:02d}",
        status="LATE" if day == 5 else "PRESENT",
        checkIn="08:30:00" if day == 5 else "07:55:00",
        checkOut="17:30:00", notes="",
        employee=emp_ref(2),
    ))

SUGGESTION_RESPONSE = [
    dict(rank=1, employeeId=1, firstName="Nguyễn Văn", lastName="An",
         department="Phát triển phần mềm", position="Backend Developer",
         reasoning=(
             "Khớp hoàn toàn kỹ năng yêu cầu (Java, Spring Boot, PostgreSQL). "
             "Đã hoàn thành 95% công việc đúng hạn trong 6 tháng qua. "
             "Hiện đang phụ trách 2 task IN_PROGRESS – workload vừa phải. "
             "Là lựa chọn lý tưởng cho công việc backend này."
         )),
    dict(rank=2, employeeId=5, firstName="Hoàng Quốc", lastName="Em",
         department="Phát triển phần mềm", position="Mobile Developer",
         reasoning=(
             "Có kinh nghiệm 3 năm với Java (bên cạnh Flutter/Dart). "
             "Có thể hỗ trợ phần backend nếu Backend Dev bị quá tải. "
             "Hiệu suất hoàn thành 88%, chuyên cần tốt."
         )),
    dict(rank=3, employeeId=2, firstName="Trần Thị", lastName="Bình",
         department="Phát triển phần mềm", position="Frontend Developer",
         reasoning=(
             "Quen thuộc với Spring Boot từ các dự án full-stack. "
             "Khớp 1/3 kỹ năng yêu cầu nhưng học hỏi nhanh. "
             "Phù hợp khi cần phối hợp frontend–backend tightly coupled."
         )),
    dict(rank=4, employeeId=3, firstName="Lê Minh", lastName="Cường",
         department="Thiết kế đồ hoạ", position="UI/UX Designer",
         reasoning=(
             "Không khớp kỹ năng kỹ thuật nhưng workload rảnh hoàn toàn. "
             "Có thể đóng góp ở phần thiết kế API contract / mock data. "
             "Không khuyến nghị làm chính."
         )),
    dict(rank=5, employeeId=4, firstName="Phạm Thu", lastName="Dung",
         department="Marketing", position="Marketing Lead",
         reasoning=(
             "Không phù hợp về kỹ năng kỹ thuật. "
             "Chỉ liệt kê để tham chiếu tổng thể."
         )),
]


def ok(data):
    return dict(success=True, message="OK", data=data)


# EMPLOYEE_ID = 2 (Trần Thị Bình) đại diện cho user nhân viên trong flow demo
EMPLOYEE_USER_ID = 2

# Tasks gán cho employee 2 (Trần Thị Bình) — lọc từ TASKS theo assignedTo
def my_tasks_for(emp_id):
    out = []
    for t in TASKS:
        if t.get("assignedTo") and t["assignedTo"].get("employeeId") == emp_id:
            out.append({
                **t,
                "taskId": t["id"],
                "status": t["status"].lower(),
                "requiredSkills": "Java, Spring Boot, PostgreSQL" if t["id"] % 2 else "React, Tailwind, REST API",
            })
    if not out:
        # fallback: 3 task minh hoạ
        out = [
            dict(taskId=101, title="Triển khai trang giỏ hàng", description="Code component giỏ hàng + state.",
                 requiredSkills="React, Tailwind", status="in_progress", dueDate="2026-05-18",
                 project=proj_ref(1), assignedTo=emp_ref(emp_id)),
            dict(taskId=102, title="Fix bug filter sản phẩm", description="Lỗi filter không hoạt động trên iOS.",
                 requiredSkills="React, JavaScript", status="pending", dueDate="2026-05-15",
                 project=proj_ref(1), assignedTo=emp_ref(emp_id)),
            dict(taskId=103, title="Viết unit test cho module checkout", description="Coverage > 80%.",
                 requiredSkills="Vitest, React Testing Library", status="completed", dueDate="2026-05-08",
                 project=proj_ref(1), assignedTo=emp_ref(emp_id)),
        ]
    return out


def my_attendance_for(emp_id):
    return [a for a in ATTENDANCE if a.get("employee", {}).get("employeeId") == emp_id]


def install_routes(page):
    """Mock toàn bộ API mà frontend gọi."""
    def handle(route):
        url = route.request.url
        method = route.request.method
        path = url.split("?")[0]
        # Employee self-service endpoints — check BEFORE the broader rules
        if path.endswith("/api/tasks/me") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(my_tasks_for(EMPLOYEE_USER_ID))))
        if path.endswith("/api/attendance/me") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(my_attendance_for(EMPLOYEE_USER_ID))))
        if path.endswith("/api/attendance/me/checkin") and method == "POST":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok({"message": "checked in"})))
        if path.endswith("/api/attendance/me/checkout") and method == "POST":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok({"message": "checked out"})))
        if "/api/tasks/" in path and "/status" in path and method == "PATCH":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok({"message": "status updated"})))
        if path.endswith("/api/employees") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(EMPLOYEES)))
        if path.endswith("/api/projects") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(PROJECTS)))
        if path.endswith("/api/tasks") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(TASKS)))
        if path.endswith("/api/attendance") and method == "GET":
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(ATTENDANCE)))
        if path.endswith("/api/suggestions/recommend") and method == "POST":
            # Mô phỏng độ trễ AI ~1s cho realistic
            time.sleep(0.8)
            return route.fulfill(status=200, content_type="application/json",
                                 body=json.dumps(ok(SUGGESTION_RESPONSE)))
        # default
        return route.fulfill(status=200, content_type="application/json",
                             body=json.dumps(ok([])))

    # Chỉ intercept request đến backend (localhost:5000) – KHÔNG đụng đến
    # static module của Vite ở localhost:5173/src/api/...
    page.route("http://localhost:5000/api/**", handle)
    page.route("http://127.0.0.1:5000/api/**", handle)


def main():
    print("[1/4] Khởi động Vite dev server...")
    env = os.environ.copy()
    env["VITE_API_BASE_URL"] = "http://localhost:5000"

    proc = subprocess.Popen(
        ["npm.cmd", "run", "dev", "--", "--host", "127.0.0.1", "--port", str(FE_PORT)],
        cwd=str(FRONTEND),
        env=env,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        shell=False,
        creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0,
    )
    try:
        if not wait_for_port("127.0.0.1", FE_PORT, timeout=90):
            print("Không khởi động được dev server.", file=sys.stderr)
            proc.terminate()
            return 1
        print(f"    -> dev server ready @ http://127.0.0.1:{FE_PORT}")
        # Vite vẫn cần thêm vài giây để optimize deps xong
        print("    -> chờ Vite pre-bundling 8s...")
        time.sleep(8)

        print("[2/4] Mở Chromium + mock API...")
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            ctx = browser.new_context(viewport={"width": 1440, "height": 900},
                                       locale="vi-VN")
            # Inject token + user vào localStorage TRƯỚC khi React mount,
            # để ProtectedRoute không redirect về /login.
            # KHÔNG ghi đè 'user' ở init_script (vì sẽ override mỗi lần goto,
            # phá vỡ role-switch giữa MANAGER flow và EMPLOYEE flow).
            # Chỉ đảm bảo có token sẵn để qua được auth guard.
            ctx.add_init_script("""
                try {
                    if (!localStorage.getItem('token')) {
                        localStorage.setItem('token', 'demo.jwt.token.for.screenshot');
                    }
                } catch (e) { /* localStorage có thể chưa sẵn sàng */ }
            """)
            page = ctx.new_page()
            install_routes(page)

            base = f"http://127.0.0.1:{FE_PORT}"

            def goto_and_wait(url, wait_ms=4500):
                page.goto(url, wait_until="domcontentloaded", timeout=60000)
                # Đợi React render: chờ có nội dung trong #root
                try:
                    page.wait_for_function(
                        "document.getElementById('root') && "
                        "document.getElementById('root').innerHTML.length > 100",
                        timeout=60000,
                    )
                except Exception as ex:
                    print(f"   !! wait_for_function timeout: {ex}; tiếp tục chờ {wait_ms}ms")
                page.wait_for_timeout(wait_ms)

            print("[3/4] Chụp các trang...")
            goto_and_wait(base + "/login", wait_ms=5000)  # cold start cần lâu hơn
            page.screenshot(path=str(OUT / "01_login.png"), full_page=True)
            print("    -> 01_login.png")

            goto_and_wait(base + "/register")
            page.screenshot(path=str(OUT / "02_register.png"), full_page=True)
            print("    -> 02_register.png")

            # Seed localStorage rồi tải các trang protected (role = MANAGER)
            page.evaluate("""
                () => {
                    localStorage.setItem('token', 'demo.jwt.token.for.screenshot');
                    localStorage.setItem('user', JSON.stringify({
                        username: 'manager', email: 'manager@hutech.edu.vn',
                        role: 'MANAGER'
                    }));
                }
            """)

            for path_url, fname, desc in [
                ("/dashboard",       "03_dashboard.png",      "Dashboard"),
                ("/employees",       "04_employees.png",      "Quản lý nhân viên"),
                ("/projects",        "05_projects.png",       "Quản lý dự án"),
                ("/tasks",           "06_tasks.png",          "Quản lý công việc"),
                ("/attendance",      "07_attendance.png",     "Chấm công"),
                ("/ai-suggestions",  "08_ai_suggestions.png", "AI gợi ý nhân viên"),
            ]:
                goto_and_wait(base + path_url, wait_ms=3000)
                page.screenshot(path=str(OUT / fname), full_page=True)
                print(f"    -> {fname}  ({desc})")

            # Riêng AI Suggestion: điền form rồi submit để hiển thị kết quả
            try:
                goto_and_wait(base + "/ai-suggestions", wait_ms=2000)
                # Điền "Tiêu đề công việc" (input đầu tiên)
                title_input = page.query_selector("input[type='text']")
                if title_input:
                    title_input.fill("Triển khai backend RESTful API cho hệ thống đặt hàng")
                # Điền "Mô tả công việc" (textarea)
                desc = page.query_selector("textarea")
                if desc:
                    desc.fill(
                        "Cần phát triển hệ thống quản lý đơn hàng sử dụng Java Spring Boot 3.x, "
                        "PostgreSQL và bảo mật JWT. Tích hợp Redis cache, viết tài liệu Swagger. "
                        "Ưu tiên ứng viên có kinh nghiệm với Spring Security."
                    )
                page.wait_for_timeout(400)
                # Submit form: nút "Phân tích bằng AI" hoặc button[type=submit]
                submitted = False
                for selector in ["button:has-text('Phân tích')",
                                 "button:has-text('Gợi ý')",
                                 "button[type='submit']"]:
                    btn = page.query_selector(selector)
                    if btn and btn.is_enabled():
                        btn.click()
                        submitted = True
                        break
                if submitted:
                    # Đợi response (mock có sleep 0.8s + animation)
                    page.wait_for_timeout(3000)
                page.screenshot(path=str(OUT / "09_ai_result.png"), full_page=True)
                print("    -> 09_ai_result.png (kết quả AI)")
            except Exception as e:
                print(f"    !! không capture được AI result: {e}")

            # ===== EMPLOYEE FLOW =====
            print("[3b/4] Chụp các trang nhân viên (role=EMPLOYEE)...")
            page.evaluate("""
                () => {
                    localStorage.setItem('token', 'demo.jwt.token.for.screenshot');
                    localStorage.setItem('user', JSON.stringify({
                        username: 'binhtt', email: 'binh.tt@company.vn',
                        role: 'EMPLOYEE'
                    }));
                }
            """)
            for path_url, fname, desc in [
                ("/dashboard",     "10_emp_dashboard.png",     "Dashboard nhân viên"),
                ("/my-tasks",      "11_emp_my_tasks.png",      "Công việc của tôi"),
                ("/my-attendance", "12_emp_my_attendance.png", "Chấm công của tôi"),
                ("/projects",      "13_emp_projects.png",      "Dự án (nhân viên xem)"),
            ]:
                goto_and_wait(base + path_url, wait_ms=3000)
                page.screenshot(path=str(OUT / fname), full_page=True)
                print(f"    -> {fname}  ({desc})")

            browser.close()

        print("[4/4] Done.")
        return 0
    finally:
        try:
            proc.terminate()
            try:
                proc.wait(timeout=10)
            except subprocess.TimeoutExpired:
                proc.kill()
        except Exception:
            pass


if __name__ == "__main__":
    sys.exit(main())
