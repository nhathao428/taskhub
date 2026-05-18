# -*- coding: utf-8 -*-
"""
Tạo video tour demo (.mp4) từ ảnh chụp màn hình ứng dụng.
Chạy: python build_demo_video.py  ->  demo_video.mp4 + demo_poster.png
"""
import os
import numpy as np
import imageio.v2 as imageio
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
SS = os.path.join(HERE, 'screenshots')
OUT = os.path.join(HERE, 'demo_video.mp4')
POSTER = os.path.join(HERE, 'demo_poster.png')

W, H = 1280, 720
FPS = 25
HOLD = 3.2          # giây mỗi cảnh
FADE = 0.55         # giây chuyển cảnh

INDIGO = (79, 70, 229)
DEEP = (49, 46, 129)
LIGHT = (238, 242, 255)
INK = (30, 41, 59)
WHITE = (255, 255, 255)
PINK = (236, 72, 153)
SHADOW = (203, 213, 225)
LILAC = (165, 180, 252)


def font(bold, size):
    p = 'C:/Windows/Fonts/arialbd.ttf' if bold else 'C:/Windows/Fonts/arial.ttf'
    return ImageFont.truetype(p, size)


F_APP = font(True, 24)
F_KICK = font(True, 17)
F_CAP = font(True, 30)
F_TITLE = font(True, 50)
F_SUB = font(False, 25)


def ctext(d, cx, y, text, fnt, fill):
    b = d.textbbox((0, 0), text, font=fnt)
    d.text((cx - (b[2] - b[0]) / 2, y), text, font=fnt, fill=fill)


def scene_app(img_file, kicker, caption):
    cv = Image.new('RGB', (W, H), LIGHT)
    d = ImageDraw.Draw(cv)
    d.rectangle([0, 0, W, 66], fill=INDIGO)
    d.rectangle([0, 66, W, 70], fill=PINK)
    d.text((34, 20), 'Task Manager', font=F_APP, fill=WHITE)
    kb = d.textbbox((0, 0), kicker, font=F_KICK)
    kw = kb[2] - kb[0]
    d.rounded_rectangle([W - kw - 58, 17, W - 30, 49], radius=16, fill=WHITE)
    d.text((W - kw - 44, 23), kicker, font=F_KICK, fill=INDIGO)

    shot = Image.open(os.path.join(SS, img_file)).convert('RGB')
    rw, rh = 1130, 506
    r = min(rw / shot.width, rh / shot.height)
    nw, nh = int(shot.width * r), int(shot.height * r)
    shot = shot.resize((nw, nh), Image.LANCZOS)
    px, py = (W - nw) // 2, 96 + (rh - nh) // 2
    d.rounded_rectangle([px - 4, py + 4, px + nw + 16, py + nh + 18],
                        radius=14, fill=SHADOW)
    d.rounded_rectangle([px - 10, py - 10, px + nw + 10, py + nh + 10],
                        radius=14, fill=WHITE)
    cv.paste(shot, (px, py))
    ctext(d, W / 2, 636, caption, F_CAP, INK)
    return cv


def scene_cover(title, sub):
    cv = Image.new('RGB', (W, H), DEEP)
    d = ImageDraw.Draw(cv)
    d.rectangle([0, 0, W, 8], fill=PINK)
    d.rectangle([0, H - 8, W, H], fill=PINK)
    d.rounded_rectangle([W / 2 - 46, 158, W / 2 + 46, 250], radius=22, fill=INDIGO)
    ctext(d, W / 2, 180, 'TM', font(True, 46), WHITE)
    ctext(d, W / 2, 312, title, F_TITLE, WHITE)
    ctext(d, W / 2, 392, sub, F_SUB, LILAC)
    return cv


SCENES = [
    ('cover', 'HỆ THỐNG QUẢN LÝ CÔNG VIỆC', 'Video demo sản phẩm'),
    ('app', '01_login.png', 'Đăng nhập', 'Đăng nhập hệ thống'),
    ('app', '03_dashboard.png', 'Dashboard', 'Bảng điều khiển — tổng quan trực quan'),
    ('app', '04_employees.png', 'Nhân viên', 'Quản lý nhân viên'),
    ('app', '05_projects.png', 'Dự án', 'Quản lý dự án'),
    ('app', '06_tasks.png', 'Công việc', 'Quản lý và giao công việc'),
    ('app', '08_ai_suggestions.png', 'AI', 'AI gợi ý nhân viên — nhập yêu cầu'),
    ('app', '09_ai_result.png', 'AI', 'AI gợi ý nhân viên — kết quả phân tích'),
    ('app', '07_attendance.png', 'Chấm công', 'Chấm công nhân viên'),
    ('app', '15_my_attendance_map.png', 'GPS', 'Chấm công GPS xác thực bằng bản đồ'),
    ('cover', 'CẢM ƠN ĐÃ THEO DÕI', 'Hệ thống Quản lý Công việc tích hợp AI'),
]


def build(s):
    return scene_cover(s[1], s[2]) if s[0] == 'cover' else scene_app(s[1], s[2], s[3])


print('Đang dựng cảnh...')
composed = [build(s) for s in SCENES]

frames = []
hold_n, fade_n = int(HOLD * FPS), int(FADE * FPS)
for i, img in enumerate(composed):
    for _ in range(hold_n):
        frames.append(img)
    if i < len(composed) - 1:
        nxt = composed[i + 1]
        for f in range(1, fade_n + 1):
            frames.append(Image.blend(img, nxt, f / (fade_n + 1)))

print(f'Đang ghi {len(frames)} khung hình -> demo_video.mp4')
writer = imageio.get_writer(OUT, fps=FPS, codec='libx264', quality=8,
                            macro_block_size=None)
for fr in frames:
    writer.append_data(np.asarray(fr))
writer.close()

composed[2].save(POSTER)
secs = round(len(frames) / FPS, 1)
print(f'[OK] demo_video.mp4 ({secs}s) + demo_poster.png')
