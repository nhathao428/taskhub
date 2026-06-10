"""
Update 2 trang bìa (bìa chính + bìa phụ) cho khớp template DACS:
- Thêm dòng "Chuyên ngành" (template DACS yêu cầu)
- Giữ nguyên content khác (Ngành, Lớp, GVHD, SVTH, MSSV, năm)
"""
import docx
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)

# Insert "Chuyên ngành" line right after "Ngành" line — for both bìa chính + bìa phụ.
inserted = 0
for i, p in enumerate(d.paragraphs):
    if i > 40:
        break  # only target front bìa pages
    if p.text.strip().startswith('Ngành:'):
        # Insert chuyên ngành after this paragraph
        new_p = OxmlElement('w:p')
        # Copy pPr from "Ngành" paragraph to keep same formatting
        ng_pPr = p._element.find(qn('w:pPr'))
        if ng_pPr is not None:
            from copy import deepcopy
            new_p.append(deepcopy(ng_pPr))
        # Build run with text mimicking "Ngành" line spacing
        ng_text = p.text
        # Extract leading whitespace after "Ngành:" colon
        chuyen_text = 'Chuyên ngành:              CÔNG NGHỆ THÔNG TIN'
        r = OxmlElement('w:r')
        # Copy rPr from first run of "Ngành" para for matching font
        if p.runs:
            r_first = p.runs[0]._element
            r_rPr = r_first.find(qn('w:rPr'))
            if r_rPr is not None:
                r.append(deepcopy(r_rPr))
        t = OxmlElement('w:t'); t.text = chuyen_text
        t.set(qn('xml:space'), 'preserve')
        r.append(t); new_p.append(r)
        p._element.addnext(new_p)
        inserted += 1

d.save(PATH)
print(f'Inserted "Chuyên ngành" line in {inserted} bìa page(s)')
