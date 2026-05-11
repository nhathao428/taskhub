"""Test debug: chạy frontend và xem nó render gì."""
import os, socket, subprocess, sys, time
from pathlib import Path
from playwright.sync_api import sync_playwright

ROOT = Path(r"C:\Users\Admin\task-management-system")
FRONTEND = ROOT / "frontend"
OUT = ROOT / "docs" / "screenshots"

FE_PORT = 5173


def wait_for_port(host, port, timeout=90):
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


env = os.environ.copy()
proc = subprocess.Popen(
    ["npm.cmd", "run", "dev", "--", "--host", "127.0.0.1", "--port", str(FE_PORT)],
    cwd=str(FRONTEND), env=env,
    stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
    text=True, shell=False,
    creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0,
)
print("Vite starting...")
if not wait_for_port("127.0.0.1", FE_PORT, 90):
    print("FAIL")
    proc.terminate(); sys.exit(1)
print(f"ready @ http://127.0.0.1:{FE_PORT}")
time.sleep(3)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    ctx = browser.new_context(viewport={"width": 1440, "height": 900}, locale="vi-VN")
    page = ctx.new_page()
    msgs = []
    page.on("console", lambda m: msgs.append(f"[{m.type}] {m.text}"))
    page.on("pageerror", lambda e: msgs.append(f"[PAGEERROR] {e}"))
    page.on("requestfailed", lambda r: msgs.append(f"[REQFAIL] {r.url} -> {r.failure}"))

    page.goto(f"http://127.0.0.1:{FE_PORT}/login", wait_until="domcontentloaded", timeout=30000)
    page.wait_for_timeout(5000)

    html_len = page.evaluate("document.body.innerHTML.length")
    root_html = page.evaluate("document.getElementById('root')?.innerHTML || '(no #root)'")
    print(f"body length: {html_len}")
    print(f"root content (first 400):\n{root_html[:400]}")
    print("---- console ----")
    for m in msgs[:30]:
        print(m)

    page.screenshot(path=str(OUT / "debug_login.png"), full_page=True)
    browser.close()

proc.terminate()
try:
    proc.wait(timeout=10)
except subprocess.TimeoutExpired:
    proc.kill()
