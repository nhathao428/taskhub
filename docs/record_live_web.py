# -*- coding: utf-8 -*-
"""
QUAY TRỰC TIẾP app với ĐẦY ĐỦ THAO TÁC CRUD + CHẤM CÔNG + AI GỢI Ý (BẢN STATEFUL MOCK):
- Tự động chạy Vite dev server.
- [Desktop Phase - Quản lý] Đăng nhập, CRUD nhân viên, CRUD dự án, CRUD công việc, duyệt chấm công, chạy AI gợi ý, đăng xuất.
- [Desktop Phase - Nhân viên] Đăng nhập, cập nhật trạng thái công việc (select), chấm công check-in/check-out, xem dự án, đăng xuất.
- [Mobile Phase - Nhân viên] Giả lập iPhone 13, đăng nhập, đổi trạng thái công việc, check-in/check-out, xem dự án, đăng xuất.
- [Mobile Phase - Quản lý] Giả lập iPhone 13, đăng nhập, xem Dashboard, chạy AI gợi ý, đăng xuất.
- Ghép nối hai video thành docs/demo_video.mp4.
"""
import os
import sys
import json
import socket
import time
import subprocess
import shutil
from pathlib import Path
import imageio_ffmpeg
from playwright.sync_api import sync_playwright

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from capture_screenshots import PROJECTS, TASKS, ATTENDANCE, EMPLOYEES

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

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = Path(os.path.dirname(HERE))
FRONTEND = ROOT / "frontend"
OUTDIR = ROOT / "docs"
OUT_MP4 = OUTDIR / "demo_video.mp4"
VIDEO_DIR = OUTDIR / "_rec_live"

FE_PORT = 5173
BASE = f"http://127.0.0.1:{FE_PORT}"
W, H = 1280, 720
FFMPEG = imageio_ffmpeg.get_ffmpeg_exe()

# Stateful Mock DB
_emp_list = [dict(e) for e in EMPLOYEES]
_emp_next = [max((e.get("id") or e.get("employeeId") or 0) for e in _emp_list) + 1]

_proj_list = [dict(p) for p in PROJECTS]
_proj_next = [max((p.get("id") or p.get("projectId") or 0) for p in _proj_list) + 1]

_task_list = [dict(t) for t in TASKS]
_task_next = [max((t.get("id") or t.get("taskId") or 0) for t in _task_list) + 1]

_attendance_list = [dict(a) for a in ATTENDANCE]

def login_route(route):
    try:
        email = json.loads(route.request.post_data or "{}").get("email", "")
    except Exception:
        email = ""
    is_mgr = "manager" in email.lower()
    user = {
        "token": "demo.jwt.token.live",
        "username": "Quản lý" if is_mgr else "Trần Thị Bình",
        "email": email,
        "role": "MANAGER" if is_mgr else "EMPLOYEE",
    }
    route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": user}))

def handle_mock_api(route):
    global _emp_list, _proj_list, _task_list, _attendance_list
    url = route.request.url
    method = route.request.method
    path = url.split("?")[0]
    
    # Auth Login
    if path.endswith("/api/auth/login") and method == "POST":
        return login_route(route)
        
    # Tasks me
    if path.endswith("/api/tasks/me") and method == "GET":
        out = []
        for t in _task_list:
            assigned = t.get("assignedTo")
            if assigned and (assigned.get("employeeId") == 2 or assigned.get("id") == 2):
                out.append({
                    **t,
                    "taskId": t.get("id") or t.get("taskId"),
                    "status": t.get("status", "pending").lower(),
                    "requiredSkills": t.get("requiredSkills") or "React, REST API",
                })
        return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": out}))
        
    # Attendance me
    if path.endswith("/api/attendance/me") and method == "GET":
        out = [a for a in _attendance_list if a.get("employee", {}).get("employeeId") == 2 or a.get("employee", {}).get("id") == 2]
        return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": out}))
        
    # Attendance Check-in / Check-out
    if path.endswith("/api/attendance/me/checkin") and method == "POST":
        new_att = {
            "id": len(_attendance_list) + 1,
            "attendanceId": len(_attendance_list) + 1,
            "date": time.strftime("%Y-%m-%d"),
            "checkIn": time.strftime("%Y-%m-%dT%H:%M:%S"),
            "checkOut": None,
            "reviewStatus": "APPROVED",
            "employee": {"employeeId": 2, "firstName": "Trần Thị", "lastName": "Bình"}
        }
        _attendance_list.append(new_att)
        return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": new_att}))
        
    if path.endswith("/api/attendance/me/checkout") and method == "POST":
        for a in reversed(_attendance_list):
            if (a.get("employee", {}).get("employeeId") == 2 or a.get("employee", {}).get("id") == 2) and not a.get("checkOut"):
                a["checkOut"] = time.strftime("%Y-%m-%dT%H:%M:%S")
                return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": a}))
        new_att = {
            "id": len(_attendance_list) + 1,
            "attendanceId": len(_attendance_list) + 1,
            "date": time.strftime("%Y-%m-%d"),
            "checkIn": time.strftime("%Y-%m-%dT08:00:00"),
            "checkOut": time.strftime("%Y-%m-%dT%H:%M:%S"),
            "reviewStatus": "APPROVED",
            "employee": {"employeeId": 2, "firstName": "Trần Thị", "lastName": "Bình"}
        }
        _attendance_list.append(new_att)
        return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": new_att}))

    # Task status PATCH
    if "/api/tasks/" in path and "/status" in path and method == "PATCH":
        task_id = int(path.split("/")[-2])
        try:
            body = json.loads(route.request.post_data or "{}")
        except Exception:
            body = {}
        for t in _task_list:
            if (t.get("id") or t.get("taskId")) == task_id:
                t["status"] = body.get("status", "PENDING").upper()
                return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": t}))
        return route.fulfill(status=404)

    # Attendance general (GET / PATCH)
    if path.endswith("/api/attendance") and method == "GET":
        return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": _attendance_list}))
        
    if "/api/attendance/" in path and "/review" in path and method == "PATCH":
        att_id = int(path.split("/")[-2])
        for a in _attendance_list:
            if (a.get("id") or a.get("attendanceId")) == att_id:
                a["reviewStatus"] = "APPROVED"
                return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": a}))
        return route.fulfill(status=404)

    # Employees API
    if path.endswith("/api/employees"):
        if method == "GET":
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": _emp_list}))
        elif method == "POST":
            try:
                body = json.loads(route.request.post_data or "{}")
            except Exception:
                body = {}
            new = {"id": _emp_next[0], "employeeId": _emp_next[0], "email": body.get("email") or "demo@company.vn", "phone": "", "status": "ACTIVE"}
            new.update({k: v for k, v in body.items() if v})
            _emp_next[0] += 1
            _emp_list.append(new)
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": new}))

    if "/api/employees/" in path:
        emp_id = int(path.split("/")[-1])
        if method == "PUT":
            try:
                body = json.loads(route.request.post_data or "{}")
            except Exception:
                body = {}
            for e in _emp_list:
                if (e.get("employeeId") or e.get("id")) == emp_id:
                    e.update(body)
                    return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": e}))
            return route.fulfill(status=404)
        elif method == "DELETE":
            _emp_list = [e for e in _emp_list if (e.get("employeeId") or e.get("id")) != emp_id]
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "Deleted"}))

    # Projects API
    if path.endswith("/api/projects"):
        if method == "GET":
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": _proj_list}))
        elif method == "POST":
            try:
                body = json.loads(route.request.post_data or "{}")
            except Exception:
                body = {}
            new = {"id": _proj_next[0], "projectId": _proj_next[0], "status": "ACTIVE"}
            new.update({k: v for k, v in body.items() if v})
            _proj_next[0] += 1
            _proj_list.append(new)
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": new}))

    if "/api/projects/" in path:
        proj_id = int(path.split("/")[-1])
        if method == "PUT":
            try:
                body = json.loads(route.request.post_data or "{}")
            except Exception:
                body = {}
            for p in _proj_list:
                if (p.get("projectId") or p.get("id")) == proj_id:
                    p.update(body)
                    return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": p}))
            return route.fulfill(status=404)
        elif method == "DELETE":
            _proj_list = [p for p in _proj_list if (p.get("projectId") or p.get("id")) != proj_id]
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "Deleted"}))

    # Tasks API
    if path.endswith("/api/tasks"):
        if method == "GET":
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": _task_list}))
        elif method == "POST":
            try:
                body = json.loads(route.request.post_data or "{}")
            except Exception:
                body = {}
            new = {"id": _task_next[0], "taskId": _task_next[0], "status": "PENDING"}
            new.update({k: v for k, v in body.items() if v})
            if body.get("projectId"):
                new["project"] = {"projectId": body["projectId"], "name": next((p["name"] for p in _proj_list if (p.get("id") or p.get("projectId")) == body["projectId"]), "Project")}
            if body.get("assignedToId"):
                new["assignedTo"] = {"employeeId": body["assignedToId"], "firstName": "Trần Thị", "lastName": "Bình"}
            _task_next[0] += 1
            _task_list.append(new)
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": new}))

    if "/api/tasks/" in path:
        task_id = int(path.split("/")[-1])
        if method == "PUT":
            try:
                body = json.loads(route.request.post_data or "{}")
            except Exception:
                body = {}
            for t in _task_list:
                if (t.get("taskId") or t.get("id")) == task_id:
                    t.update(body)
                    if body.get("projectId"):
                        t["project"] = {"projectId": body["projectId"], "name": next((p["name"] for p in _proj_list if (p.get("id") or p.get("projectId")) == body["projectId"]), "Project")}
                    if body.get("assignedToId"):
                        t["assignedTo"] = {"employeeId": body["assignedToId"], "firstName": "Trần Thị", "lastName": "Bình"}
                    return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": t}))
            return route.fulfill(status=404)
        elif method == "DELETE":
            _task_list = [t for t in _task_list if (t.get("taskId") or t.get("id")) != task_id]
            return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "Deleted"}))

    # AI Suggestions Recommend
    if path.endswith("/api/suggestions/recommend") and method == "POST":
        from capture_screenshots import SUGGESTION_RESPONSE
        time.sleep(0.8)
        return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": SUGGESTION_RESPONSE}))

    # Fallback default
    return route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": []}))

def wait_render(page, ms=350):
    try:
        page.wait_for_function("document.getElementById('root') && document.getElementById('root').innerHTML.length > 100", timeout=15000)
    except Exception:
        pass
    try:
        page.wait_for_selector(".animate-spin", state="hidden", timeout=5000)
    except Exception:
        pass
    try:
        page.wait_for_load_state("networkidle", timeout=3000)
    except Exception:
        pass
    page.wait_for_timeout(ms)

def smooth_scroll(page, total=400, step=100, pause=120):
    y = 0
    while y < total:
        page.mouse.wheel(0, step); y += step; page.wait_for_timeout(pause)
    page.wait_for_timeout(300)
    while y > 0:
        page.mouse.wheel(0, -step); y -= step; page.wait_for_timeout(pause // 2)

def do_login(page, email, password):
    if "/login" not in page.url:
        page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000)
    wait_render(page, 1000)
    e = page.query_selector("input[type='email']")
    if e:
        e.click(); page.type("input[type='email']", email, delay=25)
    pw = page.query_selector("input[type='password']")
    if pw:
        pw.click(); page.type("input[type='password']", password, delay=25)
    page.wait_for_timeout(400)
    page.click("button[type='submit']")
    page.wait_for_timeout(1000)
    if "/login" in page.url:
        page.goto(BASE + "/dashboard", wait_until="domcontentloaded", timeout=60000)
    wait_render(page, 1200)
    smooth_scroll(page, 150); page.wait_for_timeout(500)

def logout(page):
    try:
        page.get_by_text("Đăng xuất", exact=True).last.click()
        page.wait_for_timeout(1000)
    except Exception:
        page.evaluate("()=>{localStorage.clear()}")
        page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000)
    wait_render(page, 800)

def nav(page, label, hold=1200):
    page.get_by_role("link", name=label, exact=True).first.click()
    wait_render(page, 200); page.wait_for_timeout(hold)

def search_demo(page, term):
    sel = "input[placeholder*='Tìm kiếm']"
    box = page.query_selector(sel)
    if not box:
        return
    box.click()
    page.type(sel, term, delay=80)
    page.wait_for_timeout(1200)
    for _ in range(len(term)):
        page.keyboard.press("Backspace"); page.wait_for_timeout(40)
    page.wait_for_timeout(800)

def close_modal_if_open(page):
    try:
        m = page.locator("div.fixed.inset-0")
        if m.count() and m.last.is_visible():
            page.keyboard.press("Escape"); page.wait_for_timeout(400)
    except Exception:
        pass

def add_record(page, button_name, samples):
    try:
        page.get_by_role("button", name=button_name).first.click()
    except Exception:
        return
    page.wait_for_timeout(800)
    modal = page.locator("div.fixed.inset-0").last
    for sel in modal.locator("select").element_handles():
        try:
            real = [o for o in sel.query_selector_all("option") if (o.get_attribute("value") or "").strip()]
            if real:
                sel.select_option(value=real[0].get_attribute("value")); page.wait_for_timeout(150)
        except Exception:
            pass
    idx = 0
    for inp in modal.locator("input").element_handles():
        try:
            if not inp.is_visible():
                continue
            t = (inp.get_attribute("type") or "text").lower()
            inp.click()
            if t == "email":
                inp.type("demo@company.vn", delay=15)
            elif t == "date":
                inp.fill("2026-06-15")
            elif t == "number":
                inp.type("3", delay=15)
            elif t in ("text", "tel", ""):
                inp.type(samples[idx % len(samples)], delay=20); idx += 1
            page.wait_for_timeout(100)
        except Exception:
            pass
    for ta in modal.locator("textarea").element_handles():
        try:
            if ta.is_visible():
                ta.click(); ta.type("Yêu cầu các kỹ năng chuyên môn liên quan.", delay=10)
        except Exception:
            pass
    page.wait_for_timeout(600)
    try:
        modal.locator("button[type='submit']").first.click()
    except Exception:
        pass
    page.wait_for_timeout(850); close_modal_if_open(page); page.wait_for_timeout(300)

def main():
    print("[1/5] Khởi động Vite dev server...")
    env = os.environ.copy()
    env["VITE_API_BASE_URL"] = "http://localhost:5000"
    proc = subprocess.Popen(
        ["npm.cmd", "run", "dev", "--", "--host", "127.0.0.1", "--port", str(FE_PORT)],
        cwd=str(FRONTEND), env=env,
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
        shell=False,
        creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0,
    )
    
    try:
        if not wait_for_port("127.0.0.1", FE_PORT, timeout=90):
            print("Không khởi động được dev server.", file=sys.stderr)
            return 1
        print(f"    -> dev server ready @ http://127.0.0.1:{FE_PORT}")
        time.sleep(5)
        
        if VIDEO_DIR.exists():
            shutil.rmtree(VIDEO_DIR)
        VIDEO_DIR.mkdir(parents=True, exist_ok=True)
        
        print("[2/5] Bắt đầu quay giai đoạn DESKTOP...")
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            ctx_desk = browser.new_context(
                viewport={"width": W, "height": H}, locale="vi-VN",
                record_video_dir=str(VIDEO_DIR), record_video_size={"width": W, "height": H},
            )
            page = ctx_desk.new_page()
            page.on("dialog", lambda dialog: dialog.accept()) # Auto accept confirms
            
            # Use stateful mock
            page.route("http://localhost:5000/api/**", handle_mock_api)
            page.route("http://127.0.0.1:5000/api/**", handle_mock_api)

            print("  - Đăng nhập Quản lý (Manager) trên Web...")
            do_login(page, "manager@hutech.edu.vn", "Manager@123")
            
            # --- CRUD Employees ---
            print("  - CRUD Nhân viên...")
            nav(page, "Nhân viên")
            # Create
            add_record(page, "Thêm nhân viên", ["Nguyễn", "Văn Demo", "Kỹ sư phần mềm", "Kỹ thuật"])
            # Read
            search_demo(page, "Nguyễn")
            # Update
            page.locator("tr:has-text('Nguyễn')").get_by_role("button", name="Sửa").first.click()
            page.wait_for_timeout(800)
            inp_pos = page.locator("input[placeholder='VD: Kỹ sư phần mềm']")
            if inp_pos.count():
                inp_pos.click(); page.keyboard.press("Control+A"); page.keyboard.press("Backspace")
                page.type("input[placeholder='VD: Kỹ sư phần mềm']", "Senior Engineer", delay=20)
            page.click("button:has-text('Lưu')"); page.wait_for_timeout(1000)
            # Delete (Add dummy & Delete)
            add_record(page, "Thêm nhân viên", ["Test Xóa", "Dummy", "Developer", "IT"])
            page.wait_for_timeout(500)
            page.locator("tr:has-text('Test Xóa')").get_by_role("button", name="Xóa").first.click()
            page.wait_for_timeout(1000)

            # --- CRUD Projects ---
            print("  - CRUD Dự án...")
            nav(page, "Dự án")
            # Create
            add_record(page, "Thêm dự án", ["Dự án Đồ án cơ sở", "Hệ thống quản lý"])
            # Update
            page.locator("tr:has-text('Dự án Đồ án')").get_by_role("button", name="Sửa").first.click()
            page.wait_for_timeout(800)
            page.locator("select").select_option(value="ON_TRACK")
            page.click("button:has-text('Lưu')"); page.wait_for_timeout(1000)
            # Delete (Add dummy & Delete)
            add_record(page, "Thêm dự án", ["Dự án Dummy", "Mô tả test"])
            page.wait_for_timeout(500)
            page.locator("tr:has-text('Dự án Dummy')").get_by_role("button", name="Xóa").first.click()
            page.wait_for_timeout(1000)

            # --- CRUD Tasks ---
            print("  - CRUD Công việc...")
            nav(page, "Công việc")
            # Create
            add_record(page, "Thêm công việc", ["Xây dựng REST API backend", "Phát triển các api"])
            # Update
            page.locator("tr:has-text('Xây dựng REST')").get_by_role("button", name="Sửa").first.click()
            page.wait_for_timeout(800)
            page.locator("select").last.select_option(label="Trần Thị Bình")
            page.click("button:has-text('Lưu')"); page.wait_for_timeout(1000)
            # Delete (Add dummy & Delete)
            add_record(page, "Thêm công việc", ["Công việc Dummy", "Mô tả test"])
            page.wait_for_timeout(500)
            page.locator("tr:has-text('Công việc Dummy')").get_by_role("button", name="Xóa").first.click()
            page.wait_for_timeout(1000)

            print("  - Xem Chấm công & Phê duyệt...")
            nav(page, "Chấm công")
            btn_appr = page.query_selector("button:has-text('Duyệt')")
            if btn_appr:
                btn_appr.click(); page.wait_for_timeout(1000)
            smooth_scroll(page, 200)

            print("  - Chạy AI gợi ý nhân sự...")
            nav(page, "AI Gợi ý")
            ti = page.query_selector("input[type='text']")
            if ti:
                ti.click(); page.type("input[type='text']", "Cần phát triển hệ thống RESTful API bằng Spring Boot", delay=20)
            td = page.query_selector("textarea")
            if td:
                td.click(); page.type("textarea", "Có PostgreSQL, kết nối React Frontend.", delay=10)
            page.wait_for_timeout(400)
            b = page.query_selector("button:has-text('Phân tích')")
            if b:
                b.click()
            page.wait_for_timeout(2000); smooth_scroll(page, 300); page.wait_for_timeout(500)

            print("  - Đăng xuất Quản lý trên Web...")
            logout(page)

            # --- Employee Web Flow ---
            print("  - Đăng nhập Nhân viên (Employee) trên Web...")
            do_login(page, "binh.tt@company.vn", "Employee@123")
            
            print("  - Xem và cập nhật trạng thái Công việc của tôi...")
            nav(page, "Công việc của tôi")
            sel_status = page.locator("tr select").first
            if sel_status.count():
                sel_status.select_option(value="in_progress")
                page.wait_for_timeout(1200)
            
            print("  - Chấm công vào/ra trên Web...")
            nav(page, "Chấm công của tôi")
            btn_checkin = page.query_selector("button:has-text('Vào ca')")
            if btn_checkin:
                btn_checkin.click(); page.wait_for_timeout(1000)
            btn_checkout = page.query_selector("button:has-text('Tan ca')")
            if btn_checkout:
                btn_checkout.click(); page.wait_for_timeout(1000)
            smooth_scroll(page, 200)

            print("  - Xem Dự án của tôi...")
            nav(page, "Dự án")
            smooth_scroll(page, 150)
            logout(page)
            
            page.close()
            ctx_desk.close()

            print("[3/5] Bắt đầu quay giai đoạn MOBILE (iPhone 13)...")
            ctx_mobi = browser.new_context(
                **p.devices["iPhone 13"],
                locale="vi-VN",
                record_video_dir=str(VIDEO_DIR),
                record_video_size={"width": 375, "height": 812},
            )
            page_m = ctx_mobi.new_page()
            page_m.on("dialog", lambda dialog: dialog.accept())
            page_m.route("http://localhost:5000/api/**", handle_mock_api)
            page_m.route("http://127.0.0.1:5000/api/**", handle_mock_api)

            print("  - Đăng nhập Nhân viên (Employee) trên Mobile...")
            do_login(page_m, "binh.tt@company.vn", "Employee@123")
            
            print("  - Cập nhật trạng thái Công việc của tôi trên Mobile...")
            nav(page_m, "Công việc của tôi")
            sel_status_m = page_m.locator("tr select").first
            if sel_status_m.count():
                sel_status_m.select_option(value="completed")
                page_m.wait_for_timeout(1200)
            smooth_scroll(page_m, 200)
            
            print("  - Chấm công vào/ra trên Mobile...")
            nav(page_m, "Chấm công của tôi")
            btn_checkin_m = page_m.query_selector("button:has-text('Vào ca')")
            if btn_checkin_m:
                btn_checkin_m.click(); page_m.wait_for_timeout(1000)
            btn_checkout_m = page_m.query_selector("button:has-text('Tan ca')")
            if btn_checkout_m:
                btn_checkout_m.click(); page_m.wait_for_timeout(1000)
            smooth_scroll(page_m, 200)
            
            print("  - Xem Dự án của tôi trên Mobile...")
            nav(page_m, "Dự án")
            smooth_scroll(page_m, 150)
            logout(page_m)

            print("  - Đăng nhập Quản lý (Manager) trên Mobile...")
            do_login(page_m, "manager@hutech.edu.vn", "Manager@123")
            
            print("  - Chạy AI gợi ý trên Mobile...")
            nav(page_m, "AI Gợi ý")
            ti_m = page_m.query_selector("input[type='text']")
            if ti_m:
                ti_m.click(); page_m.type("input[type='text']", "Thiết kế wireframe Mobile", delay=20)
            page_m.wait_for_timeout(400)
            b_m = page_m.query_selector("button:has-text('Phân tích')")
            if b_m:
                b_m.click()
            page_m.wait_for_timeout(2000)
            smooth_scroll(page_m, 300)
            logout(page_m)
            
            page_m.close()
            ctx_mobi.close()
            browser.close()
            
        print("[4/5] Chuyển đổi và ghép nối video...")
        webm_files = sorted(list(VIDEO_DIR.glob("*.webm")), key=lambda f: f.stat().st_mtime)
        if len(webm_files) < 2:
            print(f"Error: Expected 2 webm files, found {len(webm_files)}!", file=sys.stderr)
            return 1
            
        desktop_webm = webm_files[0]
        mobile_webm = webm_files[1]
        
        desk_mp4 = VIDEO_DIR / "desk.mp4"
        mobi_mp4 = VIDEO_DIR / "mobi.mp4"
        
        print("  - Đang xử lý video Desktop...")
        cmd_desk = [
            FFMPEG, "-y", "-i", str(desktop_webm),
            "-c:v", "libx264", "-preset", "medium", "-crf", "23",
            "-pix_fmt", "yuv420p", "-r", "25",
            "-vf", f"scale={W}:{H}:force_original_aspect_ratio=decrease,"
                   f"pad={W}:{H}:(ow-iw)/2:(oh-ih)/2:color=black,setsar=1",
            "-movflags", "+faststart", str(desk_mp4)
        ]
        subprocess.run(cmd_desk, check=True, capture_output=True)
        
        print("  - Đang xử lý video Mobile...")
        cmd_mobi = [
            FFMPEG, "-y", "-i", str(mobile_webm),
            "-c:v", "libx264", "-preset", "medium", "-crf", "23",
            "-pix_fmt", "yuv420p", "-r", "25",
            "-vf", f"scale={W}:{H}:force_original_aspect_ratio=decrease,"
                   f"pad={W}:{H}:(ow-iw)/2:(oh-ih)/2:color=0x0d1b2a,setsar=1",
            "-movflags", "+faststart", str(mobi_mp4)
        ]
        subprocess.run(cmd_mobi, check=True, capture_output=True)
        
        print("  - Ghép nối hai video...")
        concat_txt = VIDEO_DIR / "concat.txt"
        with open(concat_txt, "w", encoding="utf-8") as f:
            f.write(f"file '{str(desk_mp4).replace(chr(92), '/')}'\n")
            f.write(f"file '{str(mobi_mp4).replace(chr(92), '/')}'\n")
            
        if OUT_MP4.exists():
            OUT_MP4.unlink()
            
        cmd_concat = [
            FFMPEG, "-y", "-f", "concat", "-safe", "0", "-i", str(concat_txt),
            "-c", "copy", str(OUT_MP4)
        ]
        subprocess.run(cmd_concat, check=True, capture_output=True)
        
        shutil.rmtree(VIDEO_DIR, ignore_errors=True)
        size = OUT_MP4.stat().st_size / 1024 / 1024
        print(f"[5/5] [OK] Đã hoàn thành ghép nối -> {OUT_MP4.name} — {size:.1f} MB")
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
