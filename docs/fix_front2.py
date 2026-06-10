"""
Fix v2:
- Heading 1 mặc định có page-break-before. Cách 1: override style với pageBreakBefore = false
- Cách 2: hợp nhất các section ngắn (Mục lục, Danh mục) cho gọn

Vẫn giữ chương 1-6 bắt đầu trang mới.
Front matter (Lời cảm ơn, Cam đoan, Mục lục, Danh mục) → cho phép chảy liền nhau.
"""
import docx
from docx.text.paragraph import Paragraph
from docx.oxml.ns import qn

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)

# Inspect Heading 1 style
h1 = d.styles['Heading 1']
print(f'Heading 1 style:')
print(f'  font: {h1.font.name} {h1.font.size}')

# Get paragraph format from style XML
h1_el = h1.element
pPr = h1_el.find(qn('w:pPr'))
if pPr is not None:
    pbb = pPr.find(qn('w:pageBreakBefore'))
    print(f'  pageBreakBefore on style: {pbb is not None}')

# Strategy: directly remove pageBreakBefore from style + add manual page break before chapter headings
# Step 1: remove from style
if pPr is not None:
    pbb = pPr.find(qn('w:pageBreakBefore'))
    if pbb is not None:
        pPr.remove(pbb)
        print('Removed pageBreakBefore from Heading 1 style')

# Step 2: add manual page break ONLY before chapter headings (CHƯƠNG x)
import re
added_breaks = 0
for p in d.paragraphs:
    if p.style.name == 'Heading 1':
        t = p.text.strip().upper()
        is_chapter = bool(re.match(r'CHƯƠNG \d', t)) or 'KẾT LUẬN' in t or 'TÀI LIỆU THAM KHẢO' in t or 'PHỤ LỤC' in t
        if is_chapter:
            # Insert page break in the heading paragraph's first run
            # Set pageBreakBefore on this individual paragraph
            ppr = p._element.find(qn('w:pPr'))
            if ppr is None:
                ppr = p._element.makeelement(qn('w:pPr'), {})
                p._element.insert(0, ppr)
            from docx.oxml import OxmlElement
            pbb = OxmlElement('w:pageBreakBefore')
            ppr.append(pbb)
            added_breaks += 1

print(f'Added pageBreakBefore to {added_breaks} chapter/back-matter headings')

d.save(PATH)
print('Saved')
