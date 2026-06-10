"""
Quay video demo từ screenshots với imageio + ffmpeg backend.
"""
import os, shutil
import imageio.v3 as iio
import imageio

SCREENSHOTS = r'C:\Users\Admin\taskhub\docs\screenshots'
OUT = r'C:\Users\Admin\taskhub\docs\demo_video.mp4'

# (file, duration seconds)
SEQUENCE = [
    ('01_login.png', 2),
    ('02_register.png', 2),
    ('03_dashboard.png', 4),
    ('04_employees.png', 3),
    ('05_projects.png', 3),
    ('06_tasks.png', 4),
    ('07_attendance.png', 3),
    ('08_ai_suggestions.png', 3),
    ('14_office_locations.png', 3),
    ('10_emp_dashboard.png', 3),
    ('11_emp_my_tasks.png', 3),
    ('15_my_attendance_map.png', 3),
]

FPS = 12

# Read first image to get target size
first = iio.imread(os.path.join(SCREENSHOTS, SEQUENCE[0][0]))
H, W = first.shape[:2]
# Even dimensions for h264
W = W - (W % 2)
H = H - (H % 2)
print(f'Target: {W}x{H} @ {FPS}fps')

# Build writer
writer = imageio.get_writer(OUT, fps=FPS, codec='libx264', pixelformat='yuv420p',
                             macro_block_size=1, ffmpeg_log_level='error')

from PIL import Image
total_frames = 0
for fname, dur in SEQUENCE:
    src = os.path.join(SCREENSHOTS, fname)
    if not os.path.exists(src):
        print(f'  skip {fname}')
        continue
    img = Image.open(src).convert('RGB')
    if img.size != (W, H):
        img = img.resize((W, H), Image.LANCZOS)
    frame = list(img.getdata())
    import numpy as np
    arr = np.array(img)
    n_frames = dur * FPS
    for _ in range(n_frames):
        writer.append_data(arr)
        total_frames += 1
    print(f'  {fname}: +{n_frames} frames')

writer.close()
import os
size_mb = os.path.getsize(OUT) / 1024 / 1024
print(f'\nSaved {OUT} | {total_frames} frames | {size_mb:.2f} MB')
