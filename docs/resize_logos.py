"""
Resize:
- HUTECH logo (rId10): 10 × 11.6 cm → 3 × 3.5 cm
- Mobile screenshots (rId38, 39 etc): 10 × 22 cm → 5 × 11 cm
- Tall vertical images > 14cm tall → cap at 14cm
"""
import docx
from docx.oxml.ns import qn

PATH = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
d = docx.Document(PATH)

EMU_PER_CM = 360000  # 914400 / 2.54

def emu(cm): return int(cm * EMU_PER_CM)

resized = 0
for p in d.paragraphs:
    for r in p.runs:
        for drawing in r._element.findall(qn('w:drawing')):
            # find blip rid to identify image
            for blip in drawing.iter('{http://schemas.openxmlformats.org/drawingml/2006/main}blip'):
                rid = blip.get('{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed')
                # find extent
                target_w_cm = None
                for extent in drawing.iter('{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}extent'):
                    cx = int(extent.get('cx'))
                    cy = int(extent.get('cy'))
                    w_cm = cx / EMU_PER_CM
                    h_cm = cy / EMU_PER_CM

                    # Rule 1: HUTECH logo
                    if rid == 'rId10':
                        target_w_cm = 3.0
                    # Rule 2: tall portrait images (h > 14cm) — cap
                    elif h_cm > 14:
                        # scale so h = 11
                        target_w_cm = w_cm * (11.0 / h_cm)
                    # Rule 3: other images keep
                    else:
                        target_w_cm = None

                    if target_w_cm:
                        ratio = cy / cx
                        new_cx = emu(target_w_cm)
                        new_cy = int(new_cx * ratio)
                        extent.set('cx', str(new_cx))
                        extent.set('cy', str(new_cy))
                        resized += 1

                # also resize drawingml main ext (inside pic)
                for ext in drawing.iter('{http://schemas.openxmlformats.org/drawingml/2006/main}ext'):
                    cx = int(ext.get('cx'))
                    cy = int(ext.get('cy'))
                    w_cm = cx / EMU_PER_CM
                    h_cm = cy / EMU_PER_CM
                    if rid == 'rId10':
                        ratio = cy / cx
                        new_cx = emu(3.0)
                        new_cy = int(new_cx * ratio)
                        ext.set('cx', str(new_cx))
                        ext.set('cy', str(new_cy))
                    elif h_cm > 14:
                        ratio = cy / cx
                        target = w_cm * (11.0 / h_cm)
                        new_cx = emu(target)
                        new_cy = int(new_cx * ratio)
                        ext.set('cx', str(new_cx))
                        ext.set('cy', str(new_cy))

print(f'Resized {resized} image instances')
d.save(PATH)
print('Saved')
