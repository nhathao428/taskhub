# -*- coding: utf-8 -*-
"""Đọc .mdj và vẽ preview từng sơ đồ (đúng toạ độ StarUML sẽ render)."""
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

MDJ = Path(__file__).resolve().parent.parent / "taskhub.mdj"
OUT = Path(__file__).resolve().parent.parent
data = json.loads(MDJ.read_text(encoding="utf-8"))

by_id = {}
def index(o):
    if isinstance(o, dict):
        if "_id" in o: by_id[o["_id"]] = o
        for v in o.values(): index(v)
    elif isinstance(o, list):
        for v in o: index(v)
index(data)

def deref(r): return by_id.get(r["$ref"]) if isinstance(r, dict) and "$ref" in r else None

def font(sz, bold=False):
    f = "C:/Windows/Fonts/%s" % ("arialbd.ttf" if bold else "arial.ttf")
    try: return ImageFont.truetype(f, sz)
    except Exception: return ImageFont.load_default()

F   = font(13); FB = font(13, True); FS = font(11)

def find_diagrams(o, acc):
    if isinstance(o, dict):
        if o.get("_type", "").endswith("Diagram"): acc.append(o)
        for v in o.values(): find_diagrams(v, acc)
    elif isinstance(o, list):
        for v in o: find_diagrams(v, acc)

diagrams = []; find_diagrams(data, diagrams)

NODE_TYPES = ("UMLActorView","UMLUseCaseView","UMLClassView","ERDEntityView","UMLEnumerationView")

def node_center(v):
    return (v["left"] + v["width"]/2, v["top"] + v["height"]/2)

for dg in diagrams:
    views = dg.get("ownedViews", [])
    nodes = [v for v in views if v.get("_type") in NODE_TYPES]
    if not nodes: continue
    maxx = max(v["left"]+v["width"] for v in nodes) + 60
    maxy = max(v["top"]+v["height"] for v in nodes) + 80
    img = Image.new("RGB", (int(maxx), int(maxy)), "white")
    d = ImageDraw.Draw(img)

    # cạnh trước — vẽ THEO ĐÚNG points trong file (giống StarUML render)
    for v in views:
        t = v.get("_type", "")
        if t.endswith("View") and v.get("tail") and v.get("head"):
            pstr = v.get("points", "")
            if pstr:
                pp = [tuple(map(float, seg.split(":"))) for seg in pstr.split(";") if ":" in seg]
                if len(pp) >= 2:
                    d.line(pp, fill="#555555", width=1)
                    continue
            a = deref(v["tail"]); b = deref(v["head"])
            if a in nodes and b in nodes:
                d.line([node_center(a), node_center(b)], fill="#555555", width=1)

    # node
    for v in nodes:
        x, y, w, h = v["left"], v["top"], v["width"], v["height"]
        t = v["_type"]; m = deref(v.get("model", {})) or {}
        name = m.get("name", "")
        if t == "UMLActorView":
            cx = x + w/2
            d.ellipse([cx-9, y, cx+9, y+18], outline="black")          # đầu
            d.line([cx, y+18, cx, y+42], fill="black")                  # thân
            d.line([cx-15, y+26, cx+15, y+26], fill="black")            # tay
            d.line([cx, y+42, cx-12, y+62], fill="black")               # chân
            d.line([cx, y+42, cx+12, y+62], fill="black")
            tb = d.textbbox((0,0), name, font=FB)
            d.text((cx-(tb[2]-tb[0])/2, y+66), name, fill="black", font=FB)
        elif t == "UMLUseCaseView":
            d.ellipse([x, y, x+w, y+h], outline="black", fill="#fdf6e3")
            # wrap chữ
            words = name.split(); lines=[]; cur=""
            for wd in words:
                test=(cur+" "+wd).strip()
                if d.textlength(test, font=F) > w-18: lines.append(cur); cur=wd
                else: cur=test
            if cur: lines.append(cur)
            ty = y + h/2 - len(lines)*7
            for ln in lines:
                tw = d.textlength(ln, font=F)
                d.text((x+w/2-tw/2, ty), ln, fill="black", font=F); ty += 14
        else:  # Class / ERDEntity / Enumeration
            isE = t == "ERDEntityView"; isEnum = t == "UMLEnumerationView"
            fill = "#eef4ff" if isE else ("#fff3e0" if isEnum else "#f4f4f4")
            hdr  = "#d9e6ff" if isE else ("#ffe0b2" if isEnum else "#e0e0e0")
            d.rectangle([x, y, x+w, y+h], outline="black", fill=fill)
            d.rectangle([x, y, x+w, y+20], outline="black", fill=hdr)
            if isEnum:
                s="«enumeration»"; sw=d.textlength(s,font=FS)
                d.text((x+w/2-sw/2, y+1), s, fill="#555", font=FS)
                tw=d.textlength(name,font=FB); d.text((x+w/2-tw/2, y+11), name, fill="black", font=FB)
                d.rectangle([x, y, x+w, y+34], outline="black"); rowy=y+38
            else:
                tw = d.textlength(name, font=FB); d.text((x+w/2-tw/2, y+3), name, fill="black", font=FB)
                d.line([x, y+20, x+w, y+20], fill="black"); rowy=y+24
            rows = []
            if t == "UMLClassView":
                for a in m.get("attributes", []):
                    rows.append(("- %s : %s" % (a.get("name"), a.get("type","")), False))
                for op in m.get("operations", []):
                    rows.append(("# %s() : void" % op.get("name"), True))
            elif isEnum:
                for lit in m.get("literals", []):
                    rows.append((lit.get("name",""), False))
            else:
                for c in m.get("columns", []):
                    mark = " (PK)" if c.get("primaryKey") else (" (FK)" if c.get("foreignKey") else "")
                    rows.append(("%s : %s%s" % (c.get("name"), c.get("type",""), mark), c.get("primaryKey")))
            ry = rowy; sep_done=False
            for txt,bold in rows:
                if t=="UMLClassView" and bold and not sep_done:
                    d.line([x, ry-1, x+w, ry-1], fill="black"); sep_done=True
                d.text((x+6, ry), txt, fill="black", font=FS if isE else F)
                ry += 15

    name = dg.get("name","diagram").replace(" ","_").replace("/","-")
    p = OUT / ("preview_%s.png" % name)
    img.save(p); print("->", p.name, img.size)
print("DONE")
