"""
Video demo nâng cấp (4 phân đoạn rõ ràng):
- Intro card (2s)
- PHÂN ĐOẠN 1: Góc nhìn QUẢN LÝ (web)
- PHÂN ĐOẠN 2: Góc nhìn NHÂN VIÊN (web)
- PHÂN ĐOẠN 3: QUẢN LÝ trên MOBILE (Flutter)
- PHÂN ĐOẠN 4: NHÂN VIÊN trên MOBILE (Flutter)
- Mỗi phân đoạn mở đầu bằng 1 thẻ tiêu đề (section card)
- Scene web: Ken Burns; scene mobile: đặt ảnh dọc giữa nền (không méo)
- Caption + role chip; crossfade; outro
- 1920x1080, 30fps, H.264
"""
import os
import numpy as np
from PIL import Image, ImageDraw, ImageFont
import imageio

HERE = os.path.dirname(os.path.abspath(__file__))
SCREENSHOTS = os.path.join(HERE, 'screenshots')
LOGO = os.path.join(HERE, 'hutech_logo.png')
OUT = os.path.join(HERE, 'demo_video.mp4')

W, H = 1920, 1080
FPS = 30

# ---------------------------------------------------------------------------
# TIMELINE: mỗi phần tử là
#   ('section', title, subtitle, accent_hex)
#   ('scene', file, caption, role, duration_s, kenburns)   # file có thể là 'mobile/mXX.png'
# ---------------------------------------------------------------------------
TIMELINE = [
    ('scene', '01_login.png',     'Trang đăng nhập · JWT + khóa sau 5 lần sai', 'public', 3.0, 'zoom_in'),
    ('scene', '02_register.png',  'Đăng ký tài khoản · kiểm tra email & mật khẩu', 'public', 2.5, 'zoom_in'),

    ('section', 'GÓC NHÌN QUẢN LÝ', 'Web · Quản trị nhân viên · dự án · công việc · AI gợi ý', '#CC0000'),
    ('scene', '03_dashboard.png',     'Dashboard Quản lý · KPI · biểu đồ thống kê', 'manager', 3.5, 'pan_right'),
    ('scene', '04_employees.png',     'Quản lý nhân viên · phòng ban · chức vụ', 'manager', 3.0, 'pan_down'),
    ('scene', '05_projects.png',      'Quản lý dự án · trạng thái tiến độ', 'manager', 2.8, 'zoom_in'),
    ('scene', '06_tasks.png',         'Quản lý công việc · tạo & gán nhân viên', 'manager', 3.2, 'pan_down'),
    ('scene', '07_attendance.png',    'Theo dõi chấm công nhân viên', 'manager', 2.8, 'zoom_in'),
    ('scene', '08_ai_suggestions.png','AI gợi ý nhân viên · Google Gemini', 'ai', 3.5, 'zoom_in'),
    ('scene', '09_ai_result.png',     'Kết quả AI · xếp hạng top 5 kèm lý do', 'ai', 3.5, 'zoom_out'),

    ('section', 'GÓC NHÌN NHÂN VIÊN', 'Web · Tự xem công việc · cập nhật trạng thái · chấm công', '#147c79'),
    ('scene', '10_emp_dashboard.png', 'Trang cá nhân nhân viên · công việc được giao', 'employee', 3.2, 'pan_right'),
    ('scene', '11_emp_my_tasks.png',  'Công việc của tôi · tự cập nhật trạng thái', 'employee', 3.2, 'pan_down'),
    ('scene', '12_emp_my_attendance.png', 'Lịch sử chấm công của tôi', 'employee', 3.0, 'zoom_in'),
    ('scene', '13_emp_projects.png',  'Dự án nhân viên tham gia', 'employee', 2.8, 'zoom_in'),

    ('section', 'QUẢN LÝ TRÊN MOBILE', 'Ứng dụng Flutter · vai trò Quản lý', '#003366'),
    ('scene', 'mobile/m04_employees.png',     'Quản lý nhân viên trên mobile', 'manager_m', 3.0, 'still'),
    ('scene', 'mobile/m05_projects.png',      'Quản lý dự án trên mobile', 'manager_m', 3.0, 'still'),
    ('scene', 'mobile/m08_ai_suggestions.png','AI gợi ý nhân viên trên mobile', 'manager_m', 3.0, 'still'),
    ('scene', 'mobile/m09_ai_result.png',     'Kết quả AI gợi ý trên mobile', 'manager_m', 3.0, 'still'),

    ('section', 'NHÂN VIÊN TRÊN MOBILE', 'Ứng dụng Flutter · vai trò Nhân viên', '#147c79'),
    ('scene', 'mobile/m03_dashboard.png', 'Trang cá nhân nhân viên trên mobile', 'employee_m', 3.0, 'still'),
    ('scene', 'mobile/m06_tasks.png',     'Công việc của tôi trên mobile', 'employee_m', 3.0, 'still'),
    ('scene', 'mobile/m07_attendance.png','Chấm công trên mobile', 'employee_m', 3.0, 'still'),
]

ROLE_COLORS = {
    'public':     ('#003366', '#FFFFFF'),
    'manager':    ('#003366', '#CC0000'),
    'ai':         ('#9333EA', '#FFFFFF'),
    'employee':   ('#147c79', '#FFFFFF'),
    'manager_m':  ('#003366', '#CC0000'),
    'employee_m': ('#147c79', '#FFFFFF'),
}
ROLE_LABELS = {
    'public':     'PUBLIC',
    'manager':    'GÓC NHÌN QUẢN LÝ',
    'ai':         'AI POWERED',
    'employee':   'GÓC NHÌN NHÂN VIÊN',
    'manager_m':  'QUẢN LÝ · MOBILE',
    'employee_m': 'NHÂN VIÊN · MOBILE',
}


def load_font(size, bold=False):
    candidates = [
        r'C:\Windows\Fonts\segoeuib.ttf' if bold else r'C:\Windows\Fonts\segoeui.ttf',
        r'C:\Windows\Fonts\arialbd.ttf' if bold else r'C:\Windows\Fonts\arial.ttf',
    ]
    for f in candidates:
        if os.path.exists(f):
            return ImageFont.truetype(f, size)
    return ImageFont.load_default()

FONT_TITLE = load_font(76, bold=True)
FONT_SEC   = load_font(64, bold=True)
FONT_SUB   = load_font(32)
FONT_CAP   = load_font(34, bold=True)
FONT_ROLE  = load_font(20, bold=True)
FONT_BRAND = load_font(18, bold=True)


def _hex(c):
    return (int(c[1:3], 16), int(c[3:5], 16), int(c[5:7], 16))


def make_intro_frame(t, total):
    img = Image.new('RGB', (W, H), '#003366')
    draw = ImageDraw.Draw(img); draw.rectangle([0, 0, 8, H], fill='#CC0000')
    overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0)); od = ImageDraw.Draw(overlay)
    od.ellipse([W - 400, -200, W + 200, 400], fill=(0, 102, 204, 40))
    od.ellipse([-200, H - 350, 400, H + 200], fill=(204, 0, 0, 40))
    img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')
    try:
        logo = Image.open(LOGO).convert('RGBA').resize((150, 174), Image.LANCZOS)
        img.paste(logo, ((W - 150) // 2, 280), logo)
    except Exception:
        pass
    alpha = min(1.0, t / 0.8)
    overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0)); od = ImageDraw.Draw(overlay)
    title = "DEMO HỆ THỐNG QUẢN LÝ CÔNG VIỆC"
    bbox = od.textbbox((0, 0), title, font=FONT_TITLE); tw = bbox[2] - bbox[0]
    od.text(((W - tw) // 2, 510), title, font=FONT_TITLE, fill=(255, 255, 255, int(255 * alpha)))
    sub = "Tích hợp AI · Web + Mobile · Quản lý & Nhân viên"
    bbox = od.textbbox((0, 0), sub, font=FONT_SUB); sw = bbox[2] - bbox[0]
    od.text(((W - sw) // 2, 620), sub, font=FONT_SUB, fill=(117, 206, 200, int(255 * alpha)))
    od.line([(W // 2 - 200, 685), (W // 2 + 200, 685)], fill=(204, 0, 0, int(255 * alpha)), width=2)
    info = "Nguyễn Nhật Hào · 23DTHC1 · MSSV 2380612688"
    bbox = od.textbbox((0, 0), info, font=FONT_BRAND); iw = bbox[2] - bbox[0]
    od.text(((W - iw) // 2, 720), info, font=FONT_BRAND, fill=(117, 206, 200, int(255 * alpha)))
    img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')
    return np.array(img)


def make_outro_frame(t, total):
    img = Image.new('RGB', (W, H), '#003366')
    draw = ImageDraw.Draw(img); draw.rectangle([0, 0, 8, H], fill='#CC0000')
    overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0)); od = ImageDraw.Draw(overlay)
    od.ellipse([W - 300, -100, W + 100, 300], fill=(204, 0, 0, 50))
    od.ellipse([-100, H - 300, 300, H + 100], fill=(0, 102, 204, 50))
    img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')
    try:
        logo = Image.open(LOGO).convert('RGBA').resize((130, 150), Image.LANCZOS)
        img.paste(logo, ((W - 130) // 2, 300), logo)
    except Exception:
        pass
    alpha = min(1.0, t / 0.5)
    overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0)); od = ImageDraw.Draw(overlay)
    title = "CẢM ƠN THẦY CÔ ĐÃ XEM"
    bbox = od.textbbox((0, 0), title, font=FONT_TITLE); tw = bbox[2] - bbox[0]
    od.text(((W - tw) // 2, 510), title, font=FONT_TITLE, fill=(255, 255, 255, int(255 * alpha)))
    sub = "Rất mong nhận được câu hỏi và góp ý từ hội đồng"
    bbox = od.textbbox((0, 0), sub, font=FONT_SUB); sw = bbox[2] - bbox[0]
    od.text(((W - sw) // 2, 620), sub, font=FONT_SUB, fill=(117, 206, 200, int(255 * alpha)))
    img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')
    return np.array(img)


def make_section_frame(title, sub, accent, t, dur):
    """Thẻ tiêu đề phân đoạn: nền navy + thanh accent + tiêu đề lớn."""
    img = Image.new('RGB', (W, H), '#0d1b2a')
    draw = ImageDraw.Draw(img)
    ac = _hex(accent)
    draw.rectangle([0, 0, 14, H], fill=ac)
    overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0)); od = ImageDraw.Draw(overlay)
    od.ellipse([W - 420, -160, W + 160, 420], fill=(ac[0], ac[1], ac[2], 35))
    img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')
    od = ImageDraw.Draw(img)
    alpha = min(1.0, t / 0.5)
    # accent label line
    od.rectangle([150, 470, 150 + 90, 478], fill=ac)
    bbox = od.textbbox((0, 0), title, font=FONT_SEC); tw = bbox[2] - bbox[0]
    od.text((150, 495), title, font=FONT_SEC, fill=(255, 255, 255))
    od.text((150, 585), sub, font=FONT_SUB, fill=(150, 190, 210))
    return np.array(img)


def make_scene_frame(img_path, caption, role, t, dur, direction):
    base = Image.open(os.path.join(SCREENSHOTS, img_path)).convert('RGB')
    bw, bh = base.size
    portrait = bh > bw

    if portrait:
        # Ảnh mobile dọc: đặt giữa nền tối, fit theo chiều cao, không kéo méo
        canvas = Image.new('RGB', (W, H), '#0d1b2a')
        od0 = ImageDraw.Draw(canvas)
        od0.rectangle([0, 0, 8, H], fill=_hex(ROLE_COLORS[role][0]))
        target_h = H - 200
        ratio = target_h / bh
        pw, ph = int(bw * ratio), target_h
        phone = base.resize((pw, ph), Image.LANCZOS)
        # khung viền nhẹ quanh điện thoại
        fx, fy = (W - pw) // 2, 30
        od0.rounded_rectangle([fx - 8, fy - 8, fx + pw + 8, fy + ph + 8], radius=18,
                              outline=(120, 150, 180), width=3)
        canvas.paste(phone, (fx, fy))
        cropped = canvas
        p = t / dur
    else:
        # Ken Burns cho ảnh web ngang
        scale = 1.18
        sw, sh = int(W * scale), int(H * scale)
        b = base.resize((sw, sh), Image.LANCZOS)
        p = t / dur
        if direction == 'zoom_in':
            zoom = 1.0 + 0.10 * p; crop_w, crop_h = int(W / zoom), int(H / zoom); cx, cy = sw // 2, sh // 2
        elif direction == 'zoom_out':
            zoom = 1.10 - 0.10 * p; crop_w, crop_h = int(W / zoom), int(H / zoom); cx, cy = sw // 2, sh // 2
        elif direction == 'pan_right':
            crop_w, crop_h = W, H; cx = int(crop_w // 2 + (sw - crop_w) * p); cy = sh // 2
        elif direction == 'pan_left':
            crop_w, crop_h = W, H; cx = int(sw - crop_w // 2 - (sw - crop_w) * p); cy = sh // 2
        elif direction == 'pan_down':
            crop_w, crop_h = W, H; cx = sw // 2; cy = int(crop_h // 2 + (sh - crop_h) * p)
        elif direction == 'pan_up':
            crop_w, crop_h = W, H; cx = sw // 2; cy = int(sh - crop_h // 2 - (sh - crop_h) * p)
        else:
            crop_w, crop_h = W, H; cx, cy = sw // 2, sh // 2
        left = max(0, min(sw - crop_w, cx - crop_w // 2))
        top = max(0, min(sh - crop_h, cy - crop_h // 2))
        cropped = b.crop((left, top, left + crop_w, top + crop_h))
        if (crop_w, crop_h) != (W, H):
            cropped = cropped.resize((W, H), Image.LANCZOS)

    # Overlay: caption bar + role chip + progress
    overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0)); od = ImageDraw.Draw(overlay)
    bar_top = H - 130
    for i in range(130):
        a = int(220 * (i / 130))
        od.line([(0, bar_top + i), (W, bar_top + i)], fill=(0, 0, 0, a))
    role_bg, role_fg = ROLE_COLORS[role]; role_label = ROLE_LABELS[role]
    bbox = od.textbbox((0, 0), role_label, font=FONT_ROLE); rl_w = bbox[2] - bbox[0]
    rbg = _hex(role_bg); rfg = _hex(role_fg)
    od.rounded_rectangle([W - 80 - rl_w, 30, W - 30, 75], radius=22, fill=(rbg[0], rbg[1], rbg[2], 235))
    od.text((W - 55 - rl_w, 42), role_label, font=FONT_ROLE, fill=(rfg[0], rfg[1], rfg[2], 255))
    od.text((60, bar_top + 35), caption, font=FONT_CAP, fill=(255, 255, 255, 255))
    od.text((60, bar_top + 88), "HUTECH · Đồ án cơ sở · Nguyễn Nhật Hào", font=FONT_BRAND, fill=(180, 180, 180, 255))
    od.rectangle([0, 0, int(W * p), 4], fill=(204, 0, 0, 230))
    composed = Image.alpha_composite(cropped.convert('RGBA'), overlay).convert('RGB')
    return np.array(composed)


def crossfade_frames(f1, f2, alpha):
    return (f1.astype(np.float32) * (1 - alpha) + f2.astype(np.float32) * alpha).astype(np.uint8)


# ===========================================================================
writer = imageio.get_writer(OUT, fps=FPS, codec='libx264', pixelformat='yuv420p',
                            macro_block_size=1, ffmpeg_log_level='error', quality=8)
INTRO_DUR = 2.0
OUTRO_DUR = 2.0
SECTION_DUR = 1.8
CROSSFADE = 0.4
print(f'Building video {W}x{H} @ {FPS}fps...')

print('  Intro')
for i in range(int(INTRO_DUR * FPS)):
    writer.append_data(make_intro_frame(i / FPS, INTRO_DUR))

prev_last = None
for item in TIMELINE:
    if item[0] == 'section':
        _, title, sub, accent = item
        print(f'  Section: {title.encode("ascii", "ignore").decode("ascii")}')
        n = int(SECTION_DUR * FPS); n_fade = int(CROSSFADE * FPS) if prev_last is not None else 0
        for i in range(n):
            frame = make_section_frame(title, sub, accent, i / FPS, SECTION_DUR)
            if i < n_fade and prev_last is not None:
                frame = crossfade_frames(prev_last, frame, i / n_fade)
            writer.append_data(frame)
        prev_last = make_section_frame(title, sub, accent, SECTION_DUR, SECTION_DUR)
    else:
        _, fname, caption, role, dur, direction = item
        print(f'  Scene: {fname}')
        n = int(dur * FPS); n_fade = int(CROSSFADE * FPS) if prev_last is not None else 0
        for i in range(n):
            frame = make_scene_frame(fname, caption, role, i / FPS, dur, direction)
            if i < n_fade and prev_last is not None:
                frame = crossfade_frames(prev_last, frame, i / n_fade)
            writer.append_data(frame)
        prev_last = make_scene_frame(fname, caption, role, dur - 1 / FPS, dur, direction)

print('  Outro')
n_fade = int(CROSSFADE * FPS)
for i in range(int(OUTRO_DUR * FPS)):
    frame = make_outro_frame(i / FPS, OUTRO_DUR)
    if i < n_fade and prev_last is not None:
        frame = crossfade_frames(prev_last, frame, i / n_fade)
    writer.append_data(frame)

writer.close()
size_mb = os.path.getsize(OUT) / 1024 / 1024
print(f'\nDone -> {OUT} ({size_mb:.2f} MB)')
