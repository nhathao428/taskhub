"""
Thay các ảnh embedded trong báo cáo .docx bằng screenshots mới (đã ghi đè
trong docs/screenshots/).
Map: rId → screenshot file (xác định từ thứ tự + tên rId).
"""
import os, zipfile, shutil, re
from collections import OrderedDict
import xml.etree.ElementTree as ET

DOCX = r'C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx'
SCREENSHOTS_DIR = r'C:\Users\Admin\taskhub\docs\screenshots'

# Map rId → screenshot file name (based on insertion order from add_screenshots.py)
# rId25=01_login (paras 121,124) — but actually we inserted in this exact order:
# 01,02,03,04,05,06,07,08,09,10,11,12,m01,m02
SCREENSHOT_ORDER = [
    '01_login.png',
    '02_register.png',
    '03_dashboard.png',
    '04_employees.png',
    '05_projects.png',
    '06_tasks.png',
    '07_attendance.png',
    '08_ai_suggestions.png',
    '09_ai_result.png',  # may not exist; will skip if not found
    '10_emp_dashboard.png',
    '11_emp_my_tasks.png',
    '12_emp_my_attendance.png',
    'mobile/m01_login.png',
    'mobile/m02_register.png',
]

# rIds were rId25 through rId39 per earlier inspection
RID_START = 25

# Read docx, find relationships, replace media files
tmp_dir = '/tmp/docx_unzip'
if os.path.exists(tmp_dir):
    shutil.rmtree(tmp_dir)
os.makedirs(tmp_dir)

with zipfile.ZipFile(DOCX, 'r') as z:
    z.extractall(tmp_dir)

# Read document relationships to know which rId → image file
rels_xml = os.path.join(tmp_dir, 'word/_rels/document.xml.rels')
ns_rel = 'http://schemas.openxmlformats.org/package/2006/relationships'
tree = ET.parse(rels_xml)
root = tree.getroot()
rid_to_target = {}
for rel in root:
    rid = rel.get('Id')
    target = rel.get('Target')
    if target and target.startswith('media/'):
        rid_to_target[rid] = target

print(f'Image rels found: {len(rid_to_target)}')

# Map by rId number ascending
sorted_rids = sorted(rid_to_target.keys(), key=lambda r: int(r.replace('rId','')))
screenshot_rids = [r for r in sorted_rids if int(r.replace('rId','')) >= RID_START]
print(f'Screenshot rIds (>=rId{RID_START}): {screenshot_rids}')

# Replace media files in order
replaced = 0
for i, rid in enumerate(screenshot_rids):
    if i >= len(SCREENSHOT_ORDER): break
    target_rel = rid_to_target[rid]  # e.g. media/image25.png
    target_path = os.path.join(tmp_dir, 'word', target_rel)
    source = os.path.join(SCREENSHOTS_DIR, SCREENSHOT_ORDER[i].replace('/', os.sep))
    if not os.path.exists(source):
        print(f'  SKIP {rid}: source {source} not found')
        continue
    shutil.copy2(source, target_path)
    print(f'  {rid} ({target_rel}) ← {SCREENSHOT_ORDER[i]}')
    replaced += 1

print(f'\nReplaced {replaced} images')

# Re-zip back to docx
OUT = DOCX  # overwrite in place
# Build new zip
import io
with zipfile.ZipFile(OUT, 'w', zipfile.ZIP_DEFLATED) as zout:
    for root_dir, dirs, files in os.walk(tmp_dir):
        for f in files:
            full = os.path.join(root_dir, f)
            arc = os.path.relpath(full, tmp_dir).replace(os.sep, '/')
            zout.write(full, arc)

print(f'Saved → {OUT}')
