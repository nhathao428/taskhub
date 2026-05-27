"""
Ghép đoạn 'Góc nhìn Nhân viên' (slideshow từ screenshots/10..13) vào cuối
demo_video.mp4 hiện có (đoạn manager đã quay sẵn bằng Playwright).

Chạy:  python build_demo_video.py
Output: docs/demo_video.mp4 (overwrite — backup tự tạo .bak một lần)
"""
import os
import shutil
import subprocess
import imageio_ffmpeg
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
FFMPEG = imageio_ffmpeg.get_ffmpeg_exe()

VIDEO = os.path.join(HERE, "demo_video.mp4")
SHOTS_DIR = os.path.join(HERE, "screenshots")
TITLE_PNG = os.path.join(HERE, "_title_emp.png")
EMP_SEG = os.path.join(HERE, "_emp_segment.mp4")
OUT_TMP = os.path.join(HERE, "_demo_video_new.mp4")
CONCAT_LIST = os.path.join(HERE, "_concat_list.txt")
# Manager source: ưu tiên _manager_segment.mp4 (do record_manager_demo.py tạo);
# fallback về .bak để giữ tương thích với bản cũ.
MANAGER_NEW = os.path.join(HERE, "_manager_segment.mp4")
BACKUP = VIDEO + ".bak"

W, H, FPS = 1280, 720, 25


def make_title_card():
    img = Image.new("RGB", (W, H), (30, 41, 59))  # slate-800
    draw = ImageDraw.Draw(img)
    font_big = font_sub = None
    for fp in ("C:/Windows/Fonts/segoeuib.ttf", "C:/Windows/Fonts/segoeui.ttf",
              "C:/Windows/Fonts/arialbd.ttf", "C:/Windows/Fonts/arial.ttf"):
        if os.path.exists(fp):
            font_big = ImageFont.truetype(fp, 72)
            font_sub = ImageFont.truetype(fp, 28)
            break
    if font_big is None:
        font_big = ImageFont.load_default()
        font_sub = ImageFont.load_default()

    title = "GÓC NHÌN NHÂN VIÊN"
    bb = draw.textbbox((0, 0), title, font=font_big)
    tw, th = bb[2] - bb[0], bb[3] - bb[1]
    draw.text(((W - tw) // 2, (H - th) // 2 - 30), title, fill="white", font=font_big)

    sub = "Dashboard  ·  Công việc của tôi  ·  Chấm công  ·  Dự án"
    bb2 = draw.textbbox((0, 0), sub, font=font_sub)
    draw.text(((W - (bb2[2] - bb2[0])) // 2, (H - th) // 2 + th + 20),
              sub, fill=(180, 200, 220), font=font_sub)
    img.save(TITLE_PNG)


def build_emp_segment():
    shots = [
        (TITLE_PNG,                                       2.0),
        (os.path.join(SHOTS_DIR, "10_emp_dashboard.png"), 3.5),
        (os.path.join(SHOTS_DIR, "11_emp_my_tasks.png"),  3.5),
        (os.path.join(SHOTS_DIR, "12_emp_my_attendance.png"), 3.5),
        (os.path.join(SHOTS_DIR, "13_emp_projects.png"),  3.0),
    ]
    inputs = []
    for p, d in shots:
        inputs += ["-loop", "1", "-t", str(d), "-i", p]
    filters = []
    for i, _ in enumerate(shots):
        filters.append(
            f"[{i}:v]scale={W}:{H}:force_original_aspect_ratio=decrease,"
            f"pad={W}:{H}:(ow-iw)/2:(oh-ih)/2:color=white,"
            f"setsar=1,fps={FPS},format=yuv420p[v{i}]"
        )
    concat_in = "".join(f"[v{i}]" for i in range(len(shots)))
    filters.append(f"{concat_in}concat=n={len(shots)}:v=1:a=0[outv]")
    fc = ";".join(filters)

    cmd = [FFMPEG, "-y"] + inputs + [
        "-filter_complex", fc, "-map", "[outv]",
        "-c:v", "libx264", "-preset", "medium", "-crf", "23",
        "-pix_fmt", "yuv420p", "-r", str(FPS),
        "-movflags", "+faststart", EMP_SEG,
    ]
    subprocess.run(cmd, check=True, capture_output=True)


def concat_with_existing():
    # Ưu tiên đoạn manager mới (UI sau redesign); fallback về .bak nếu chưa có
    if os.path.exists(MANAGER_NEW):
        src = MANAGER_NEW
    else:
        if not os.path.exists(BACKUP):
            shutil.copy2(VIDEO, BACKUP)
        src = BACKUP
    with open(CONCAT_LIST, "w", encoding="utf-8") as f:
        f.write(f"file '{src.replace(chr(92), '/')}'\n")
        f.write(f"file '{EMP_SEG.replace(chr(92), '/')}'\n")

    cmd = [FFMPEG, "-y", "-f", "concat", "-safe", "0", "-i", CONCAT_LIST,
           "-c:v", "libx264", "-preset", "medium", "-crf", "23",
           "-pix_fmt", "yuv420p", "-r", str(FPS),
           "-movflags", "+faststart", OUT_TMP]
    subprocess.run(cmd, check=True, capture_output=True)
    shutil.move(OUT_TMP, VIDEO)


def cleanup():
    for f in (TITLE_PNG, EMP_SEG, CONCAT_LIST):
        if os.path.exists(f):
            os.remove(f)


if __name__ == "__main__":
    make_title_card()
    build_emp_segment()
    concat_with_existing()
    cleanup()
    # In ra thông tin file mới
    size = os.path.getsize(VIDEO) / 1024 / 1024
    print(f"[OK] demo_video.mp4 rebuilt — {size:.1f} MB")
