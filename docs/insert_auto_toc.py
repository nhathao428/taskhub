"""
Thay placeholder "Mục lục" + danh mục bảng/hình bằng Word TOC field tự động.
Khi mở trong Word, F9 (hoặc auto on open) sẽ build TOC từ Heading 1/2/3 + caption.
"""
import docx
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)


def make_toc_field(switches: str) -> OxmlElement:
    """Build a Word TOC field paragraph element."""
    p = OxmlElement('w:p')
    # field begin
    r1 = OxmlElement('w:r')
    fc1 = OxmlElement('w:fldChar')
    fc1.set(qn('w:fldCharType'), 'begin')
    fc1.set(qn('w:dirty'), 'true')  # mark dirty so Word auto-updates on open
    r1.append(fc1)
    p.append(r1)
    # instruction
    r2 = OxmlElement('w:r')
    instr = OxmlElement('w:instrText')
    instr.set(qn('xml:space'), 'preserve')
    instr.text = f' {switches} '
    r2.append(instr)
    p.append(r2)
    # separate
    r3 = OxmlElement('w:r')
    fc2 = OxmlElement('w:fldChar')
    fc2.set(qn('w:fldCharType'), 'separate')
    r3.append(fc2)
    p.append(r3)
    # placeholder result text
    r4 = OxmlElement('w:r')
    t = OxmlElement('w:t')
    t.text = 'Nhấn F9 trong Word để cập nhật mục lục.'
    r4.append(t)
    p.append(r4)
    # field end
    r5 = OxmlElement('w:r')
    fc3 = OxmlElement('w:fldChar')
    fc3.set(qn('w:fldCharType'), 'end')
    r5.append(fc3)
    p.append(r5)
    return p


def replace_placeholder_after(heading_text: str, switches: str) -> bool:
    paragraphs = d.paragraphs
    for i, p in enumerate(paragraphs):
        if p.style.name == 'Heading 1' and heading_text.upper() in p.text.upper():
            # Remove placeholder paragraphs until next Heading 1
            j = i + 1
            to_remove = []
            while j < len(paragraphs):
                np = paragraphs[j]
                if np.style.name == 'Heading 1':
                    break
                to_remove.append(np._element)
                j += 1
            for el in to_remove:
                el.getparent().remove(el)
            # Insert TOC field after heading
            anchor = p._element
            field_p = make_toc_field(switches)
            anchor.addnext(field_p)
            return True
    return False


# Mục lục: TOC từ Heading 1-3, có page numbers, hyperlinked
ok1 = replace_placeholder_after('MỤC LỤC', 'TOC \\o "1-3" \\h \\z \\u')
# Danh mục bảng: TOC of Table captions (Bảng X.Y)
ok2 = replace_placeholder_after('DANH MỤC CÁC BẢNG', 'TOC \\h \\z \\c "Bảng"')
# Danh mục hình ảnh, sơ đồ: TOC of Figure captions (Hình X.Y)
ok3 = replace_placeholder_after('DANH MỤC CÁC HÌNH', 'TOC \\h \\z \\c "Hình"')

print(f'MỤC LỤC: {ok1}')
print(f'DANH MỤC CÁC BẢNG: {ok2}')
print(f'DANH MỤC CÁC HÌNH: {ok3}')

d.save(PATH)
print(f'\nSaved → {PATH}')
