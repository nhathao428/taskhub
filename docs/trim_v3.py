"""
Trim v3 final: drastically reduce Ch1, Ch5, Ch4
- Ch1.4 nội dung đề tài: gộp 1.4.1, 1.4.2, 1.4.3 → 1 đoạn
- Ch1.5 phương pháp: gộp 1.5.1-1.5.5 → 1 bảng
- Ch5.4 demo: bỏ Heading 3 prefix (giữ ảnh + caption)
- Ch4.8 deploy detail
"""
import re
import docx
from docx.text.paragraph import Paragraph

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)
body = d.element.body
children = list(body)

def hl_t(c):
    if not c.tag.endswith('}p'): return (0, '', None)
    p = Paragraph(c, d); n = p.style.name; lvl = 0
    if n.startswith('Heading '):
        try: lvl = int(n.split(' ')[1])
        except: lvl = 0
    return (lvl, p.text.strip(), p)
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
    lvl, text, _ = hl_t(children[i])
    if lvl == 1:
        current_ch = chap(text); i += 1; continue
    # Ch1.4.x, 1.5.x detail: drop
    if current_ch == 1 and lvl == 3 and re.match(r'1\.[45]\.\d', text):
        j = i
        while j < len(children):
            nl, _, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch4.8 deploy detail subsections
    if current_ch == 4 and lvl == 3 and re.match(r'4\.8\.[123]\.', text):
        j = i
        while j < len(children):
            nl, _, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch5.4 demo: bỏ Heading 3 (giữ ảnh + caption)
    # 5.4.1 -> 5.4.10
    if current_ch == 5 and lvl == 3 and re.match(r'5\.4\.\d+\.', text):
        # Drop just the heading paragraph
        ranges.append((i, i)); i += 1; continue
    # Ch5.1 sub-detail: keep matrix table but drop H3
    if current_ch == 5 and lvl == 3 and re.match(r'5\.1\.[14]\.', text):
        # Drop H3 + first prose paragraph
        ranges.append((i, i)); i += 1; continue
    # Ch5.2 H2 entire (we kept summary table, drop the H2)
    if current_ch == 5 and lvl == 2 and re.match(r'5\.2\.', text):
        # Drop H2 + paragraphs until next H2
        j = i + 1
        # Keep first table (summary)
        seen_table = False
        while j < len(children):
            nl, _, _ = hl_t(children[j])
            if nl in (1, 2): break
            j += 1
        # Replace with brief: keep H2 + first table only
        # For simplicity, drop nothing here (already trimmed)
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
print(f'v3 trim: {total} elements removed')

d.save(PATH)
print(f'Saved')
