"""
Trim cuối cùng v3:
- Ch5.3 detail (giữ summary table 5.3)
- Ch5.4 detail subsections (giữ ảnh + caption, đã xong)
- Ch5.5 mobile demo (giữ 5.5.1 + ảnh, bỏ rest)
- Ch6.2, 6.3.x (giữ 6.1 + 6.3 chính)
- Phụ lục tinh giản
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

DROP_HEADERS = [
    # Ch5
    (5, 3, r'5\.3\.[12]\.'),
    # Ch5.5 — giữ 5.5.1 (đã có ảnh), bỏ rest đã làm
    # Ch6
    (6, 2, r'6\.2\.'),  # Hạn chế gộp vào 6.1
    # Phụ lục Heading 2 chi tiết
    (8, 2, r'Phụ lục II'),
    (8, 2, r'Phụ lục III'),
    (8, 2, r'Phụ lục IV'),
    (8, 2, r'Phụ lục V'),
]

def should_drop(ch, lvl, text):
    for c, l, rx in DROP_HEADERS:
        if c == ch and lvl == l and re.search(rx, text):
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
        i += 1; continue
    if current_ch is not None and should_drop(current_ch, lvl, text):
        j = i + 1
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
print(f'final3: {total} removed')

# Cũng giảm font cho Heading 1 từ 16 → 14 và Heading 2 từ 14 → 13 để gọn
# Đợi đã — DACS yêu cầu 16/14. Giữ.
# Thay vào đó: giảm line spacing Normal từ 1.5 xuống 1.3 cho gọn (vẫn đọc được)
# Nhưng DACS yêu cầu 1.5. Giữ.

# Force trim empty paragraphs liên tiếp
prev_empty = False
removed_empty = 0
for p in list(d.paragraphs):
    is_empty = not p.text.strip() and not any(r.element.findall('.//{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}inline') for r in p.runs)
    if is_empty and prev_empty:
        el = p._element
        if el.getparent() is not None:
            el.getparent().remove(el); removed_empty += 1
    prev_empty = is_empty

print(f'removed {removed_empty} extra empty paragraphs')

d.save(PATH)
print(f'Saved')
