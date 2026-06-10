"""
Trim thêm Ch5 (demo + test details), Ch6 (kết luận), Ch1 (Tổng quan dài).
"""
import re
import docx

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)

def hl(p):
    n = p.style.name
    if n.startswith('Heading '):
        try: return int(n.split(' ')[1])
        except: return 0
    return 0

def chap(t):
    t = t.upper()
    m = re.match(r'CHƯƠNG (\d)', t)
    if m: return int(m.group(1))
    if 'KẾT LUẬN' in t: return 6
    if 'TÀI LIỆU THAM KHẢO' in t: return 7
    if 'PHỤ LỤC' in t: return 8
    return None

# Trim helper: keep N paragraphs per Heading 3 inside chapter range
def trim_chapter(target_chapter, h3_para_keep=2, drop_lists=False):
    in_chap = False
    para_count = 0
    removed = 0
    for p in list(d.paragraphs):
        t = p.text.strip()
        lvl = hl(p)
        if lvl == 1:
            c = chap(t)
            in_chap = (c == target_chapter)
            para_count = 0
            continue
        if not in_chap: continue
        if lvl in (2, 3):
            para_count = 0
            continue
        if not t:
            continue
        if drop_lists and p.style.name.startswith('List'):
            el = p._element
            if el.getparent() is not None:
                el.getparent().remove(el); removed += 1
            continue
        para_count += 1
        if para_count > h3_para_keep:
            el = p._element
            if el.getparent() is not None:
                el.getparent().remove(el); removed += 1
    return removed

r1 = trim_chapter(1, h3_para_keep=2, drop_lists=False)
r5 = trim_chapter(5, h3_para_keep=2, drop_lists=False)
r6 = trim_chapter(6, h3_para_keep=2, drop_lists=False)
r8 = trim_chapter(8, h3_para_keep=1, drop_lists=False)
print(f'Ch1: {r1}  Ch5: {r5}  Ch6: {r6}  Ch8 (Phụ lục): {r8}')

# Cũng cắt long tables trong Ch8 (phụ lục dài)
in_ch8 = False
removed_tables = 0
body = d.element.body
for child in list(body):
    if child.tag.endswith('}p'):
        from docx.text.paragraph import Paragraph
        p = Paragraph(child, d)
        t = p.text.strip()
        if hl(p) == 1:
            c = chap(t)
            in_ch8 = (c == 8)
    elif in_ch8 and child.tag.endswith('}tbl'):
        # remove tables in phụ lục (they're code listings)
        body.remove(child); removed_tables += 1
print(f'Phụ lục tables removed: {removed_tables}')

d.save(PATH)
print(f'\nSaved → {PATH}')
