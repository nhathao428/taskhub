"""
Sort tài liệu tham khảo theo thứ tự A-Z (từ điển), re-number [1] [2] ...
"""
import re
import docx
from docx.text.paragraph import Paragraph

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)

# Find the Heading 1 "TÀI LIỆU THAM KHẢO" and the range until next H1
paragraphs = list(d.paragraphs)
start = None
end = None
for i, p in enumerate(paragraphs):
    if p.style.name == 'Heading 1' and 'TÀI LIỆU THAM KHẢO' in p.text.upper():
        start = i + 1
        # Find next H1
        for j in range(i + 1, len(paragraphs)):
            if paragraphs[j].style.name == 'Heading 1':
                end = j
                break
        if end is None:
            end = len(paragraphs)
        break

if start is None:
    print('Could not find TÀI LIỆU THAM KHẢO heading')
    raise SystemExit(1)

# Collect entries (each entry is [n] text). Each may span multiple paragraphs.
ref_paragraphs = paragraphs[start:end]
ref_texts = []
current = []
for p in ref_paragraphs:
    t = p.text.strip()
    if re.match(r'^\[\d+\]', t):
        if current:
            ref_texts.append(' '.join(current))
        current = [t]
    elif t:
        current.append(t)
if current:
    ref_texts.append(' '.join(current))

# Sort by author name (after [N] prefix)
def sort_key(s):
    m = re.match(r'^\[\d+\]\s*(.+)', s)
    name = m.group(1).strip() if m else s
    # First non-space, lowercase
    return name.lower()

sorted_refs = sorted(ref_texts, key=sort_key)

# Re-number
renumbered = []
for i, r in enumerate(sorted_refs, 1):
    new = re.sub(r'^\[\d+\]\s*', f'[{i}] ', r)
    renumbered.append(new)

print(f'Sorted {len(renumbered)} references')
for r in renumbered[:5]:
    print(f'  {r[:120]}')

# Replace content
# Remove all current paragraphs in the range
to_remove = []
for p in ref_paragraphs:
    if p.text.strip():
        to_remove.append(p)

# Use first non-empty paragraph as anchor, then insert sorted refs around it
if to_remove:
    anchor = to_remove[0]._element
    body = anchor.getparent()
    # Insert sorted refs BEFORE the first ref para
    from copy import deepcopy
    template = deepcopy(to_remove[0]._element)
    # Strip text from template
    for r in template.findall('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}r'):
        template.remove(r)

    # Insert new paragraphs
    from docx.oxml import OxmlElement
    from docx.oxml.ns import qn
    cursor = anchor
    new_paragraphs = []
    for text in renumbered:
        new_p = deepcopy(template)
        run = OxmlElement('w:r')
        t = OxmlElement('w:t')
        t.text = text
        t.set(qn('xml:space'), 'preserve')
        run.append(t)
        new_p.append(run)
        cursor.addprevious(new_p)

    # Remove original ref paragraphs
    for p in to_remove:
        el = p._element
        if el.getparent() is not None:
            el.getparent().remove(el)

d.save(PATH)
print('Saved')
