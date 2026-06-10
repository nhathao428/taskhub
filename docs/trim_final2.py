"""
Trim cuối cùng — gọn gọn:
- Bỏ LỜI MỞ ĐẦU (trùng Ch1)
- Ch1: bỏ 1.6 (phạm vi), 1.7 (kết cấu báo cáo)
- Ch3: bỏ 3.1 (khảo sát), 3.4 (use case detail), 3.7-3.10 (UML detail), 3.11
- Ch4: bỏ 4.1, 4.2, 4.8 (môi trường + thư mục + deploy detail)
- Ch5: bỏ 5.1 (kế hoạch test), 5.2 (kịch bản), 5.6 (geofence detail), 5.3 detail
- Ch6: gộp 6.3.x
- Phụ lục: bỏ hết Heading 2.x detail, chỉ giữ tổng quan
"""
import re
import docx
from docx.text.paragraph import Paragraph

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)
body = d.element.body
children = list(body)

def hl_t(c):
    if not c.tag.endswith('}p'): return (0, '')
    p = Paragraph(c, d); n = p.style.name; lvl = 0
    if n.startswith('Heading '):
        try: lvl = int(n.split(' ')[1])
        except: lvl = 0
    return (lvl, p.text.strip())

def chap(t):
    t = t.upper()
    m = re.match(r'CHƯƠNG (\d)', t)
    if m: return int(m.group(1))
    if 'KẾT LUẬN' in t: return 6
    if 'TÀI LIỆU' in t: return 7
    if 'PHỤ LỤC' in t: return 8
    return None

DROP_HEADERS = {
    # (chapter, level, regex)
    (0, 1, r'LỜI MỞ ĐẦU'),
    (1, 2, r'1\.6\.'),
    (1, 2, r'1\.7\.'),
    (3, 2, r'3\.1\.'),
    (3, 2, r'3\.4\.'),
    (3, 2, r'3\.7\.'),
    (3, 2, r'3\.8\.'),
    (3, 2, r'3\.9\.'),
    (3, 2, r'3\.10\.'),
    (3, 2, r'3\.11\.'),
    (4, 2, r'4\.1\.'),
    (4, 2, r'4\.2\.'),
    (4, 2, r'4\.8\.'),
    (5, 2, r'5\.1\.'),
    (5, 2, r'5\.2\.'),
    (5, 2, r'5\.6\.'),
}

def should_drop(chapter, lvl, text):
    if chapter == 0:
        if lvl == 1 and re.search(r'LỜI MỞ ĐẦU', text.upper()): return True
    for ch, l, rx in DROP_HEADERS:
        if ch == chapter and lvl == l and re.search(rx, text):
            return True
    return False

ranges = []
current_ch = None
i = 0
while i < len(children):
    lvl, text = hl_t(children[i])
    if lvl == 1:
        c = chap(text)
        if c is not None: current_ch = c
        else:
            # could be LỜI MỞ ĐẦU etc.
            current_ch = 0
            if 'LỜI MỞ ĐẦU' in text.upper():
                # Drop until next Heading 1
                j = i + 1
                while j < len(children):
                    nl, _ = hl_t(children[j])
                    if nl == 1: break
                    j += 1
                ranges.append((i, j - 1))
                i = j; continue
        i += 1; continue
    # Drop section
    if current_ch is not None and should_drop(current_ch, lvl, text):
        j = i + 1
        # Find next heading of same or higher level (lower number)
        while j < len(children):
            nl, _ = hl_t(children[j])
            if nl > 0 and nl <= lvl: break
            j += 1
        ranges.append((i, j - 1))
        i = j; continue
    i += 1

ranges.sort()
merged = []
for s, e in ranges:
    if merged and s <= merged[-1][1] + 1:
        merged[-1] = (merged[-1][0], max(merged[-1][1], e))
    else: merged.append((s, e))

total = 0
for s, e in reversed(merged):
    for k in range(e, s - 1, -1):
        body.remove(children[k]); total += 1
print(f'Trim final2: {total} elements removed')

# ============================================================
# Resize images to 10cm width (more compact)
# ============================================================
from docx.shared import Cm
from docx.oxml.ns import qn

resized = 0
for p in d.paragraphs:
    for r in p.runs:
        for drawing in r._element.findall(qn('w:drawing')):
            # Find wp:inline or wp:anchor → extent
            for extent in drawing.iter('{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}extent'):
                old_cx = int(extent.get('cx'))
                old_cy = int(extent.get('cy'))
                # New width = 10cm = 3600000 EMU
                new_cx = 3600000
                new_cy = int(old_cy * new_cx / old_cx)
                extent.set('cx', str(new_cx))
                extent.set('cy', str(new_cy))
                resized += 1
            # Also resize the pic element
            for spPr in drawing.iter('{http://schemas.openxmlformats.org/drawingml/2006/picture}pic'):
                for ext2 in spPr.iter('{http://schemas.openxmlformats.org/drawingml/2006/main}ext'):
                    old_cx = int(ext2.get('cx'))
                    old_cy = int(ext2.get('cy'))
                    new_cx = 3600000
                    new_cy = int(old_cy * new_cx / old_cx)
                    ext2.set('cx', str(new_cx))
                    ext2.set('cy', str(new_cy))

print(f'Resized {resized} images to 10cm width')

d.save(PATH)
print(f'Saved')
