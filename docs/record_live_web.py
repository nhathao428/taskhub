# -*- coding: utf-8 -*-
"""
QUAY TRỰC TIẾP app web với ĐĂNG NHẬP THẬT + THAO TÁC THẬT:
- Tự động chạy Vite dev server.
- [Desktop Phase] Đăng nhập Quản lý (Manager), thêm nhân viên, dự án, công việc, chạy AI gợi ý.
- [Mobile Phase] Giả lập iPhone 13, Đăng nhập Nhân viên (Employee), xem công việc, chấm công, dự án. Đăng nhập Quản lý (Manager), chạy AI gợi ý trên mobile.
- Tự động ghép nối hai phân đoạn video thành file docs/demo_video.mp4.
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

# Import mock data from capture_screenshots
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from capture_screenshots import install_routes, EMPLOYEES

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

# Setup paths
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

# Mock stateful employee list
_emp_list = [dict(e) for e in EMPLOYEES]
_emp_next = [max((e.get("id") or e.get("employeeId") or 0) for e in _emp_list) + 1]

def employees_route(route):
    r = route.request
    if r.method == "GET":
        route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": _emp_list}))
    elif r.method == "POST":
        try:
            body = json.loads(r.post_data or "{}")
        except Exception:
            body = {}
        new = {"id": _emp_next[0], "employeeId": _emp_next[0],
               "email": body.get("email") or "demo@company.vn", "phone": "", "status": "ACTIVE"}
        new.update({k: v for k, v in body.items() if v})
        _emp_next[0] += 1
        _emp_list.append(new)
        route.fulfill(status=200, content_type="application/json", body=json.dumps({"success": True, "message": "OK", "data": new}))
    else:
        route.fallback()

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

def wait_render(page, ms=300):
    try:
        page.wait_for_function(
            "document.getElementById('root') && document.getElementById('root').innerHTML.length > 100", timeout=30000)
    except Exception:
        pass
    try:
        page.wait_for_selector(".animate-spin", state="hidden", timeout=10000)
    except Exception:
        pass
    try:
        page.wait_for_load_state("networkidle", timeout=4000)
    except Exception:
        pass
    page.wait_for_timeout(ms)

def smooth_scroll(page, total=500, step=100, pause=150):
    y = 0
    while y < total:
        page.mouse.wheel(0, step); y += step; page.wait_for_timeout(pause)
    page.wait_for_timeout(350)
    while y > 0:
        page.mouse.wheel(0, -step); y -= step; page.wait_for_timeout(pause // 2)

def do_login(page, email, password):
    if "/login" not in page.url:
        page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000)
    wait_render(page, 1200)
    e = page.query_selector("input[type='email']")
    if e:
        e.click(); page.type("input[type='email']", email, delay=45)
    pw = page.query_selector("input[type='password']")
    if pw:
        pw.click(); page.type("input[type='password']", password, delay=45)
    page.wait_for_timeout(700)
    page.click("button[type='submit']")
    page.wait_for_timeout(1500)
    if "/login" in page.url:
        page.goto(BASE + "/dashboard", wait_until="domcontentloaded", timeout=60000)
    wait_render(page, 1400)
    smooth_scroll(page, 250); page.wait_for_timeout(900)

def logout(page):
    try:
        page.get_by_text("Đăng xuất", exact=True).last.click()
        page.wait_for_timeout(1500)
    except Exception:
        page.evaluate("()=>{localStorage.clear()}")
        page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000)
    wait_render(page, 1000)

def nav(page, label, hold=1400):
    page.get_by_role("link", name=label, exact=True).first.click()
    wait_render(page, 300); page.wait_for_timeout(hold)

def search_demo(page, term):
    sel = "input[placeholder*='Tìm kiếm']"
    box = page.query_selector(sel)
    if not box:
        return
    box.click()
    page.type(sel, term, delay=120)
    page.wait_for_timeout(1800)
    for _ in range(len(term)):
        page.keyboard.press("Backspace"); page.wait_for_timeout(60)
    page.wait_for_timeout(1200)

def close_modal_if_open(page):
    try:
        m = page.locator("div.fixed.inset-0")
        if m.count() and m.last.is_visible():
            page.keyboard.press("Escape"); page.wait_for_timeout(500)
    except Exception:
        pass

def add_record(page, button_name, samples):
    try:
        page.get_by_role("button", name=button_name).first.click()
    except Exception:
        return
    page.wait_for_timeout(1300)
    modal = page.locator("div.fixed.inset-0").last
    for sel in modal.locator("select").element_handles():
        try:
            real = [o for o in sel.query_selector_all("option") if (o.get_attribute("value") or "").strip()]
            if real:
                sel.select_option(value=real[0].get_attribute("value")); page.wait_for_timeout(250)
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
                inp.type("demo@company.vn", delay=18)
            elif t == "date":
                inp.fill("2026-06-15")
            elif t == "number":
                inp.type("3", delay=20)
            elif t in ("text", "tel", ""):
                inp.type(samples[idx % len(samples)], delay=28); idx += 1
            page.wait_for_timeout(160)
        except Exception:
            pass
    for ta in modal.locator("textarea").element_handles():
        try:
            if ta.is_visible():
                ta.click(); ta.type("Java, Spring Boot, PostgreSQL, React.", delay=10)
        except Exception:
            pass
    page.wait_for_timeout(900)
    try:
        modal.locator("button[type='submit']").first.click()
    except Exception:
        pass
    page.wait_for_timeout(1300); close_modal_if_open(page); page.wait_for_timeout(500)

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
        time.sleep(8)  # Wait for Vite bundle
        
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
            install_routes(page)
            page.route("http://localhost:5000/api/auth/login", login_route)
            page.route("http://127.0.0.1:5000/api/auth/login", login_route)
            page.route("http://localhost:5000/api/employees", employees_route)
            page.route("http://127.0.0.1:5000/api/employees", employees_route)

            print("  - Thao tác public (đăng nhập + đăng ký)...")
            page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000); wait_render(page, 1500)
            page.goto(BASE + "/register", wait_until="domcontentloaded", timeout=60000); wait_render(page, 1500)
            page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000); wait_render(page, 500)

            print("  - Đăng nhập Quản lý (Manager) trên Web...")
            do_login(page, "manager@hutech.edu.vn", "Manager@123")
            nav(page, "Nhân viên")
            smooth_scroll(page, 300)
            add_record(page, "Thêm nhân viên", ["Nguyễn", "Văn Demo", "Kỹ sư phần mềm", "Kỹ thuật"])
            search_demo(page, "Nguyễn")
            nav(page, "Dự án"); smooth_scroll(page, 250)
            add_record(page, "Thêm dự án", ["Dự án Demo Đồ án cơ sở", "Triển khai hệ thống"])
            nav(page, "Công việc"); smooth_scroll(page, 250)
            add_record(page, "Thêm công việc", ["Phát triển API thanh toán VNPay", "Tích hợp cổng thanh toán"])
            nav(page, "Chấm công"); smooth_scroll(page, 250)
            nav(page, "Văn phòng", hold=2000)

            print("  - Chạy AI gợi ý nhân sự...")
            page.get_by_role("link", name="AI Gợi ý", exact=True).first.click(); wait_render(page, 1000)
            ti = page.query_selector("input[type='text']")
            if ti:
                ti.click(); page.type("input[type='text']", "Triển khai backend RESTful API cho hệ thống đặt hàng", delay=30)
            td = page.query_selector("textarea")
            if td:
                td.click(); page.type("textarea", "Cần phát triển hệ thống quản lý đơn hàng dùng Java Spring Boot, PostgreSQL, JWT.", delay=10)
            page.wait_for_timeout(600)
            for sel in ["button:has-text('Phân tích')", "button:has-text('Gợi ý')", "button[type='submit']"]:
                b = page.query_selector(sel)
                if b and b.is_enabled():
                    b.click(); break
            page.wait_for_timeout(2500); smooth_scroll(page, 500); page.wait_for_timeout(800)
            
            print("  - Đăng xuất Quản lý trên Web...")
            logout(page)

            print("  - Đăng nhập Nhân viên (Employee) trên Web...")
            do_login(page, "binh.tt@company.vn", "Employee@123")
            nav(page, "Công việc của tôi")
            smooth_scroll(page, 300)
            nav(page, "Chấm công của tôi", hold=2000)
            nav(page, "Dự án")
            smooth_scroll(page, 200)
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
            install_routes(page_m)
            page_m.route("http://localhost:5000/api/auth/login", login_route)
            page_m.route("http://127.0.0.1:5000/api/auth/login", login_route)
            page_m.route("http://localhost:5000/api/employees", employees_route)
            page_m.route("http://127.0.0.1:5000/api/employees", employees_route)

            print("  - Đăng nhập Nhân viên (Employee) trên Mobile...")
            do_login(page_m, "binh.tt@company.vn", "Employee@123")
            
            print("  - Xem công việc của tôi trên Mobile...")
            nav(page_m, "Công việc của tôi")
            smooth_scroll(page_m, 300)
            
            print("  - Xem chấm công của tôi trên Mobile...")
            nav(page_m, "Chấm công của tôi", hold=2000)
            
            print("  - Xem dự án của tôi trên Mobile...")
            nav(page_m, "Dự án")
            logout(page_m)

            print("  - Đăng nhập Quản lý (Manager) trên Mobile...")
            do_login(page_m, "manager@hutech.edu.vn", "Manager@123")
            
            print("  - Xem Dashboard Quản lý trên Mobile...")
            nav(page_m, "AI Gợi ý")
            ti_m = page_m.query_selector("input[type='text']")
            if ti_m:
                ti_m.click(); page_m.type("input[type='text']", "Thiết kế UI mobile", delay=30)
            td_m = page_m.query_selector("textarea")
            if td_m:
                td_m.click(); page_m.type("textarea", "Thiết kế wireframe màn hình chấm công.", delay=10)
            page_m.wait_for_timeout(600)
            for sel in ["button:has-text('Phân tích')", "button:has-text('Gợi ý')", "button[type='submit']"]:
                b = page_m.query_selector(sel)
                if b and b.is_enabled():
                    b.click(); break
            page_m.wait_for_timeout(2500)
            smooth_scroll(page_m, 400)
            page_m.wait_for_timeout(1000)
            
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
        
        # 1. Convert desktop webm to standardized mp4 (padded to 1280x720)
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
        
        # 2. Convert mobile webm to standardized mp4 (padded with dark navy color #0d1b2a)
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
        
        # 3. Concatenate using FFMPEG concat demuxer
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
        
        # Clean up
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
