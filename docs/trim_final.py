"""
Trim cuối:
- Phụ lục: bỏ TẤT CẢ Heading 3 + nội dung (chỉ giữ Heading 2)
- Ch3: bỏ sequence + activity diagram sections (giữ Use Case + ERD)
- Ch1: gộp 1.6.x thành 1 đoạn
- Ch5: bỏ section 5.6 và 5.7 subsections phụ
- Ch6.3.x: gộp lại
- Ch2: chỉ giữ Heading 2 + 1 đoạn (đã làm nhưng còn dư)
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

# Determine current chapter as we iterate
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
        current_ch = chap(text)
        i += 1; continue
    # Ch3: drop sequence (3.8) + activity (3.9) entirely
    if current_ch == 3 and lvl == 2 and re.match(r'3\.[89]\.', text):
        j = i + 1
        while j < len(children):
            nl, _ = hl_t(children[j])
            if nl in (1, 2): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch1: gộp 1.6.x — drop 1.6.1, 1.6.2, 1.6.3 (giữ 1.6 heading + 1 đoạn ngắn)
    if current_ch == 1 and lvl == 3 and re.match(r'1\.6\.[123]\.', text):
        j = i
        while j < len(children):
            nl, t = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch5.6.x detail subsections
    if current_ch == 5 and lvl == 3 and re.match(r'5\.6\.[2-6]\.', text):
        j = i
        while j < len(children):
            nl, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Ch5.7.x bỏ 5.7.2, 5.7.3 (giữ 5.7.1 platform matrix)
    if current_ch == 5 and lvl == 3 and re.match(r'5\.7\.[23]\.', text):
        j = i
        while j < len(children):
            nl, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    # Phụ lục: bỏ tất cả Heading 3 content
    if current_ch == 8 and lvl == 3:
        j = i
        while j < len(children):
            nl, _ = hl_t(children[j])
            if j > i and nl in (1, 2, 3): break
            j += 1
        ranges.append((i, j - 1)); i = j; continue
    i += 1

# Merge ranges
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

print(f'Final trim: {total} elements removed')

d.save(PATH)
print(f'Saved → {PATH}')
