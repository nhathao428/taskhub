"""
Quay video demo cho 'Góc nhìn Quản lý' bằng Playwright recordVideo.
- Khởi động Vite dev server + mock backend (tái sử dụng routes từ capture_screenshots.py)
- Drive chuột/keyboard qua flow: Dashboard → Nhân viên → Dự án → Công việc →
  Chấm công → Văn phòng → AI Gợi ý (điền form + xem kết quả)
- Output: docs/_manager_segment.mp4 (1280x720, ~70s)
Sau đó build_demo_video.py sẽ ghép thêm slideshow employee vào cuối.
"""
import os
import shutil
import socket
import subprocess
import sys
import time
from pathlib import Path

import imageio_ffmpeg
from playwright.sync_api import sync_playwright

# tái sử dụng mock data + routes đã có
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from capture_screenshots import install_routes, wait_for_port  # noqa: E402

ROOT = Path(r"C:\Users\Admin\taskhub")
FRONTEND = ROOT / "frontend"
OUT_DIR = ROOT / "docs"
VIDEO_DIR = OUT_DIR / "_rec"
OUT_MP4 = OUT_DIR / "_manager_segment.mp4"

FE_PORT = 5173
W, H = 1280, 720
FFMPEG = imageio_ffmpeg.get_ffmpeg_exe()


def main():
    print("[1/4] Khởi động Vite dev server...")
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
        time.sleep(8)  # Vite pre-bundle

        if VIDEO_DIR.exists():
            shutil.rmtree(VIDEO_DIR)
        VIDEO_DIR.mkdir(parents=True, exist_ok=True)

        print("[2/4] Mở Chromium + bật recordVideo...")
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            ctx = browser.new_context(
                viewport={"width": W, "height": H},
                locale="vi-VN",
                record_video_dir=str(VIDEO_DIR),
                record_video_size={"width": W, "height": H},
            )
            # Seed token ngay từ đầu để qua auth guard
            ctx.add_init_script("""
                try {
                    if (!localStorage.getItem('token')) {
                        localStorage.setItem('token', 'demo.jwt.token.for.video');
                    }
                } catch (e) {}
            """)
            page = ctx.new_page()
            install_routes(page)
            base = f"http://127.0.0.1:{FE_PORT}"

            def goto(path, dwell=2200):
                page.goto(base + path, wait_until="domcontentloaded", timeout=60000)
                try:
                    page.wait_for_function(
                        "document.getElementById('root') && "
                        "document.getElementById('root').innerHTML.length > 100",
                        timeout=20000,
                    )
                except Exception:
                    pass
                page.wait_for_timeout(dwell)

            print("[3/4] Drive flow manager...")
            # 1. Login (cold start, dwell lâu để Inter font + animation lên)
            goto("/login", dwell=3500)

            # Seed MANAGER user
            page.evaluate("""
                () => {
                    localStorage.setItem('token', 'demo.jwt.token.for.video');
                    localStorage.setItem('user', JSON.stringify({
                        username: 'manager', email: 'manager@hutech.edu.vn',
                        role: 'MANAGER'
                    }));
                }
            """)

            # 2. Dashboard
            goto("/dashboard", dwell=5500)
            page.mouse.wheel(0, 200)
            page.wait_for_timeout(1200)
            page.mouse.wheel(0, -200)
            page.wait_for_timeout(800)

            # 3. Nhân viên
            goto("/employees", dwell=5000)
            page.mouse.wheel(0, 250)
            page.wait_for_timeout(1500)

            # 4. Dự án
            goto("/projects", dwell=5000)
            page.mouse.wheel(0, 200)
            page.wait_for_timeout(1500)

            # 5. Công việc
            goto("/tasks", dwell=5500)
            page.mouse.wheel(0, 150)
            page.wait_for_timeout(1500)

            # 6. Chấm công
            goto("/attendance", dwell=4500)

            # 7. Văn phòng (có Leaflet map, cần thời gian render)
            goto("/office-locations", dwell=5500)

            # 8. AI Gợi ý — điền form
            goto("/ai-suggestions", dwell=3000)
            title_input = page.query_selector("input[type='text']")
            if title_input:
                title_input.click()
                page.wait_for_timeout(400)
                title_input.fill("")
                page.keyboard.type(
                    "Triển khai backend RESTful API cho hệ thống đặt hàng",
                    delay=18,
                )
            page.wait_for_timeout(600)
            desc = page.query_selector("textarea")
            if desc:
                desc.click()
                page.wait_for_timeout(300)
                desc.fill("")
                page.keyboard.type(
                    "Cần phát triển hệ thống quản lý đơn hàng dùng Java Spring Boot 3, "
                    "PostgreSQL và bảo mật JWT. Ưu tiên ứng viên có Spring Security.",
                    delay=10,
                )
            page.wait_for_timeout(800)

            # Submit
            for sel in [
                "button:has-text('Phân tích')",
                "button:has-text('Gợi ý')",
                "button[type='submit']",
            ]:
                btn = page.query_selector(sel)
                if btn and btn.is_enabled():
                    btn.click()
                    break

            # Đợi kết quả + scroll xem các hạng
            page.wait_for_timeout(2200)
            page.mouse.wheel(0, 280)
            page.wait_for_timeout(2000)
            page.mouse.wheel(0, 300)
            page.wait_for_timeout(2500)
            page.mouse.wheel(0, -400)
            page.wait_for_timeout(1500)

            # Close & lấy file video webm
            page.close()
            ctx.close()
            browser.close()

        print("[4/4] Chuyển webm → mp4...")
        webm_files = list(VIDEO_DIR.glob("*.webm"))
        if not webm_files:
            print("Không tìm thấy file video!", file=sys.stderr)
            return 1
        webm = max(webm_files, key=lambda f: f.stat().st_size)
        if OUT_MP4.exists():
            OUT_MP4.unlink()
        cmd = [
            FFMPEG, "-y", "-i", str(webm),
            "-c:v", "libx264", "-preset", "medium", "-crf", "23",
            "-pix_fmt", "yuv420p", "-r", "25",
            "-vf", f"scale={W}:{H}:force_original_aspect_ratio=decrease,"
                   f"pad={W}:{H}:(ow-iw)/2:(oh-ih)/2:color=white,setsar=1",
            "-movflags", "+faststart", str(OUT_MP4),
        ]
        subprocess.run(cmd, check=True, capture_output=True)
        shutil.rmtree(VIDEO_DIR, ignore_errors=True)
        size = OUT_MP4.stat().st_size / 1024 / 1024
        print(f"[OK] {OUT_MP4.name} — {size:.1f} MB")
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
