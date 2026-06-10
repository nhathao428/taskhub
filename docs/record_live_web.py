# -*- coding: utf-8 -*-
"""
QUAY TRỰC TIẾP app web với ĐĂNG NHẬP THẬT + THAO TÁC THẬT:
- Màn login: gõ email + mật khẩu -> Đăng nhập (mock trả role theo email).
- MANAGER: thêm nhân viên/dự án/công việc, AI gợi ý.
- Đăng xuất -> đăng nhập lại bằng tài khoản EMPLOYEE -> xem màn nhân viên.
Mock API qua install_routes + mock /api/auth/login. Xuất: recording/video/live_web.webm
"""
import os, sys, json
from pathlib import Path
from playwright.sync_api import sync_playwright
from capture_screenshots import install_routes, EMPLOYEES
try:
    from capture_screenshots import ok
except Exception:
    def ok(data): return {"data": data, "success": True, "message": "OK"}

# Mock CÓ TRẠNG THÁI cho nhân viên: thêm thì list cập nhật (6 -> 7 người)
_emp_list = [dict(e) for e in EMPLOYEES]
_emp_next = [max((e.get("id") or e.get("employeeId") or 0) for e in _emp_list) + 1]


def employees_route(route):
    r = route.request
    if r.method == "GET":
        route.fulfill(status=200, content_type="application/json", body=json.dumps(ok(_emp_list)))
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
        route.fulfill(status=200, content_type="application/json", body=json.dumps(ok(new)))
    else:
        route.fallback()

BASE = "http://localhost:5173"
OUTDIR = Path(r"C:\Users\Admin\taskhub\docs\recording\video")
OUTDIR.mkdir(parents=True, exist_ok=True)
W, H = 1920, 1080


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
    route.fulfill(status=200, content_type="application/json", body=json.dumps(ok(user)))


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


def smooth_scroll(page, total=900, step=150, pause=170):
    y = 0
    while y < total:
        page.mouse.wheel(0, step); y += step; page.wait_for_timeout(pause)
    page.wait_for_timeout(350)
    while y > 0:
        page.mouse.wheel(0, -step); y -= step; page.wait_for_timeout(pause // 2)


def do_login(page, email, password):
    """Gõ email + mật khẩu trên màn login rồi bấm Đăng nhập."""
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
    if "/login" in page.url:   # phòng khi không tự điều hướng
        page.goto(BASE + "/dashboard", wait_until="domcontentloaded", timeout=60000)
    wait_render(page, 1400)
    smooth_scroll(page, 800); page.wait_for_timeout(900)


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
    """Gõ vào thanh tìm kiếm để lọc danh sách, rồi xóa để hiện lại full."""
    sel = "input[placeholder*='Tìm kiếm']"
    box = page.query_selector(sel)
    if not box:
        return
    box.click()
    page.type(sel, term, delay=120)
    page.wait_for_timeout(1800)            # xem kết quả lọc
    for _ in range(len(term)):
        page.keyboard.press("Backspace"); page.wait_for_timeout(60)
    page.wait_for_timeout(1200)            # hiện lại đầy đủ


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
                ta.click(); ta.type("Java, Spring Boot, PostgreSQL, React — ưu tiên kinh nghiệm Spring Security.", delay=10)
        except Exception:
            pass
    page.wait_for_timeout(900)
    try:
        modal.locator("button[type='submit']").first.click()
    except Exception:
        pass
    page.wait_for_timeout(1300); close_modal_if_open(page); page.wait_for_timeout(500)


def main():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(
            viewport={"width": W, "height": H}, locale="vi-VN",
            record_video_dir=str(OUTDIR), record_video_size={"width": W, "height": H},
        )
        page = ctx.new_page()
        install_routes(page)
        page.route("**/api/auth/login", login_route)
        page.route("**/api/employees", employees_route)   # mock nhân viên có trạng thái

        print("== Màn đăng nhập + đăng ký ==")
        page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000); wait_render(page, 1800)
        page.goto(BASE + "/register", wait_until="domcontentloaded", timeout=60000); wait_render(page, 1500)
        page.goto(BASE + "/login", wait_until="domcontentloaded", timeout=60000); wait_render(page, 800)

        print("== ĐĂNG NHẬP QUẢN LÝ ==")
        do_login(page, "manager@hutech.edu.vn", "Manager@123")
        nav(page, "Nhân viên")
        smooth_scroll(page, 700)               # liệt kê đầy đủ danh sách nhân viên
        add_record(page, "Thêm nhân viên", ["Nguyễn", "Văn Demo", "Kỹ sư phần mềm", "Kỹ thuật"])  # thêm nhân viên
        search_demo(page, "Nguyễn")            # rồi tìm kiếm trên thanh search
        nav(page, "Dự án"); smooth_scroll(page, 400)
        add_record(page, "Thêm dự án", ["Dự án Demo Đồ án cơ sở", "Triển khai tính năng mới cho hệ thống"])
        nav(page, "Công việc"); smooth_scroll(page, 500)
        add_record(page, "Thêm công việc", ["Phát triển API thanh toán VNPay", "Tích hợp cổng thanh toán cho hệ thống"])
        nav(page, "Chấm công"); smooth_scroll(page, 600)
        nav(page, "Văn phòng", hold=2200)

        print("== AI gợi ý ==")
        page.get_by_role("link", name="AI Gợi ý", exact=True).first.click(); wait_render(page, 1000)
        ti = page.query_selector("input[type='text']")
        if ti:
            ti.click(); page.type("input[type='text']", "Triển khai backend RESTful API cho hệ thống đặt hàng", delay=33)
        td = page.query_selector("textarea")
        if td:
            td.click(); page.type("textarea",
                "Cần phát triển hệ thống quản lý đơn hàng dùng Java Spring Boot, PostgreSQL, JWT. "
                "Ưu tiên ứng viên có kinh nghiệm Spring Security.", delay=11)
        page.wait_for_timeout(600)
        for sel in ["button:has-text('Phân tích')", "button:has-text('Gợi ý')", "button[type='submit']"]:
            b = page.query_selector(sel)
            if b and b.is_enabled():
                b.click(); break
        page.wait_for_timeout(2600); smooth_scroll(page, 1100); page.wait_for_timeout(800)

        print("== ĐĂNG XUẤT -> ĐĂNG NHẬP NHÂN VIÊN ==")
        logout(page)
        do_login(page, "binh.tt@company.vn", "Employee@123")
        nav(page, "Công việc của tôi"); smooth_scroll(page, 900)
        nav(page, "Chấm công của tôi", hold=2200); smooth_scroll(page, 700)
        nav(page, "Dự án"); smooth_scroll(page, 700)

        vid = page.video
        ctx.close(); browser.close()
        out = vid.path()
        final = OUTDIR / "live_web.webm"
        if os.path.exists(final):
            os.remove(final)
        os.rename(out, final)
        print("SAVED:", final)


if __name__ == "__main__":
    sys.exit(main())
