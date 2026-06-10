"""
Trim v2 — pháp pap mạnh thêm:
- Ch3: bỏ chi tiết bảng từ 3.6.x (giữ ERD), bỏ 3.4.x đặc tả use case detail
- Ch5: bỏ Section 5.7 nguyên cả, 5.5 mobile (sẽ có ảnh đại diện)
- Ch3.11: bỏ wireframe detail (đã có ảnh demo)
"""
import re
import docx
from docx.text.paragraph import Paragraph

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)
body = d.element.body
children = list(body)

def get_p(c): return Paragraph(c, d)
def hl_t(c):
    if not c.tag.endswith('}p'): return (0, '')
    p = get_p(c); n = p.style.name; lvl = 0
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

ranges = []
current_ch = None
i = 0
while i < len(children):
    lvl, text = hl_t(children[i])
    if lvl == 1:
        current_ch = chap(text); i += 1; continue
    # Ch3: bỏ 3.4.2, 3.4.3, 3.4.4 use case detail
    if current_ch == 3 and lvl == 3 and re.match(r'3\.4\.[234]\.', text):
        j = i
        while j < len(children):
            nl, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch3.6.x entity tables (keep heading 3.6 only)
    if current_ch == 3 and lvl == 3 and re.match(r'3\.6\.[1-6]\.', text):
        j = i
        while j < len(children):
            nl, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch3.11 wireframe sections
    if current_ch == 3 and lvl == 2 and re.match(r'3\.11\.', text):
        j = i + 1
        while j < len(children):
            nl, _ = hl_t(children[j])
            if nl in (1, 2): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch5.7 entire
    if current_ch == 5 and lvl == 2 and re.match(r'5\.7\.', text):
        j = i + 1
        while j < len(children):
            nl, _ = hl_t(children[j])
            if nl in (1, 2): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch5.5 mobile demo (will show 3 representative images instead)
    if current_ch == 5 and lvl == 3 and re.match(r'5\.5\.[2-5]\.', text):
        j = i
        while j < len(children):
            nl, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
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
print(f'v2 trim: {total} elements removed')

d.save(PATH)
print(f'Saved')
