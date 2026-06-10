"""
Trim Ch5 cuối cùng:
- Bỏ section 5.8 (mở rộng + Constitution - quá meta)
- Bỏ section 5.2.x test detail tables (giữ 1 summary)
- Bỏ section 5.7.4 (quy trình demo - meta)
"""
import re
import docx
from docx.text.paragraph import Paragraph
from docx.table import Table

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)

# Walk body, identify ranges to delete
body = d.element.body
children = list(body)

def get_para(child):
    return Paragraph(child, d)

def hl_text(child):
    if not child.tag.endswith('}p'): return (0, '')
    p = get_para(child)
    n = p.style.name
    lvl = 0
    if n.startswith('Heading '):
        try: lvl = int(n.split(' ')[1])
        except: lvl = 0
    return (lvl, p.text.strip())

# Find ranges to drop
ranges_to_drop = []  # list of (start_idx, end_idx) inclusive
i = 0
while i < len(children):
    lvl, text = hl_text(children[i])
    if lvl == 2 and re.match(r'5\.8\.', text):
        # Drop until next Heading 1 or 2
        j = i + 1
        while j < len(children):
            nlvl, ntext = hl_text(children[j])
            if nlvl in (1, 2): break
            j += 1
        ranges_to_drop.append((i, j - 1))
        i = j
        continue
    if lvl == 3 and re.match(r'5\.7\.4\.', text):
        j = i + 1
        while j < len(children):
            nlvl, _ = hl_text(children[j])
            if nlvl in (1, 2, 3): break
            j += 1
        ranges_to_drop.append((i, j - 1))
        i = j
        continue
    # Drop 5.2.x test case tables (keep heading + 1 intro para)
    if lvl == 3 and re.match(r'5\.2\.[1-7]\.', text):
        # skip heading + 1 paragraph; then drop everything until next Heading
        j = i + 1
        kept_intro = False
        while j < len(children):
            nlvl, ntxt = hl_text(children[j])
            if nlvl in (1, 2, 3): break
            if children[j].tag.endswith('}p'):
                # keep first non-empty para
                if not kept_intro and ntxt:
                    kept_intro = True
                    j += 1; continue
                ranges_to_drop.append((j, j))
            elif children[j].tag.endswith('}tbl'):
                ranges_to_drop.append((j, j))
            j += 1
        i = j
        continue
    i += 1

# Merge overlapping/adjacent ranges
ranges_to_drop.sort()
merged = []
for s, e in ranges_to_drop:
    if merged and s <= merged[-1][1] + 1:
        merged[-1] = (merged[-1][0], max(merged[-1][1], e))
    else:
        merged.append((s, e))

# Delete from the end to preserve indices
total = 0
for s, e in reversed(merged):
    for k in range(e, s - 1, -1):
        body.remove(children[k])
        total += 1
print(f'Ch5 trimmed: {total} elements removed (Section 5.8 + 5.7.4 + 5.2.x test details)')

d.save(PATH)
print(f'\nSaved → {PATH}')
