"""
Thay video demo trong PPT slide 12 bằng demo_video.mp4 mới.
"""
import os, zipfile, shutil, tempfile

PPT = r'C:\Users\Admin\taskhub\docs\THUYET_TRINH_DO_AN.pptx'
NEW_VIDEO = r'C:\Users\Admin\taskhub\docs\demo_video.mp4'

# Unzip pptx
tmp = tempfile.mkdtemp(prefix='pptx_')
print(f'Temp dir: {tmp}')
with zipfile.ZipFile(PPT) as z:
    z.extractall(tmp)

# Find media files
media_dir = os.path.join(tmp, 'ppt', 'media')
if os.path.exists(media_dir):
    for f in os.listdir(media_dir):
        full = os.path.join(media_dir, f)
        print(f'  {f}: {os.path.getsize(full)} bytes')

# Find .mp4
mp4_files = [f for f in os.listdir(media_dir) if f.endswith('.mp4')]
print(f'\nFound {len(mp4_files)} mp4: {mp4_files}')

new_size = os.path.getsize(NEW_VIDEO)
print(f'New video size: {new_size} bytes')

# Replace each mp4 (likely just one)
for f in mp4_files:
    target = os.path.join(media_dir, f)
    shutil.copy2(NEW_VIDEO, target)
    print(f'Replaced {f} with new video')

# Find thumbnail (poster image) — usually media1.jpeg or similar
# Also generate a poster from first frame for the video
# For simplicity, skip thumbnail replacement (PowerPoint will regenerate on first open)

# Rezip
import io
with zipfile.ZipFile(PPT, 'w', zipfile.ZIP_DEFLATED) as zout:
    for root, _, files in os.walk(tmp):
        for f in files:
            full = os.path.join(root, f)
            arc = os.path.relpath(full, tmp).replace(os.sep, '/')
            zout.write(full, arc)

shutil.rmtree(tmp)
print(f'\nSaved → {PPT}')
