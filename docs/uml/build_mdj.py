# -*- coding: utf-8 -*-
"""
Sinh file StarUML (.mdj) — REVERSE từ code Java thật trong /backend.
  - Class Diagram (Domain Model): 7 entity + 2 enum + quan hệ JPA thật
  - ERD (PostgreSQL): 7 bảng theo @Table/@Column
  - Use Case tổng thể
autoResize=false để không phình đè; cạnh oblique (lineStyle=1); self-check chồng lấn.
Chạy: python build_mdj.py  ->  ../taskhub.mdj
"""
import json, itertools
from pathlib import Path

_c = itertools.count(1)
def nid(): return "ID%010d" % next(_c)
def ref(o): return {"$ref": o["_id"]}
def el(t, parent, **kw):
    d = {"_type": t, "_id": nid()}
    if parent is not None: d["_parent"] = ref(parent)
    d.update(kw); return d

_boxes = {}

def node_label(parent, model, top, left, w, font="Arial;13;0"):
    return el("NodeLabelView", parent, model=ref(model), visible=True,
              horizontalAlignment=2, verticalAlignment=5,
              left=left, top=top, width=w, height=14, font=font)
def comp_label(parent, model, top, left, w, font="Arial;13;1"):
    return el("LabelView", parent, model=ref(model), visible=True,
              horizontalAlignment=2, verticalAlignment=5,
              left=left, top=top, width=w, height=16, font=font)
def elabel(parent, model, vis=False):
    return el("EdgeLabelView", parent, model=ref(model), visible=vis,
              text="", alpha=1.5708, distance=20, hostEdge=ref(parent))
def textlabel(parent, model, text, vis=True):
    return el("EdgeLabelView", parent, model=ref(model), visible=vis,
              text=text, alpha=1.5708, distance=25, hostEdge=ref(parent))

def _border(v, ox, oy):
    cx = v["left"] + v["width"]/2; cy = v["top"] + v["height"]/2
    dx = ox-cx; dy = oy-cy
    if dx == 0 and dy == 0: return cx, cy
    hw = v["width"]/2; hh = v["height"]/2
    s = min(hw/abs(dx) if dx else 1e9, hh/abs(dy) if dy else 1e9)
    return round(cx+dx*s), round(cy+dy*s)
def pts(av, bv):
    acx = av["left"]+av["width"]/2; acy = av["top"]+av["height"]/2
    bcx = bv["left"]+bv["width"]/2; bcy = bv["top"]+bv["height"]/2
    x1, y1 = _border(av, bcx, bcy); x2, y2 = _border(bv, acx, acy)
    return "%d:%d;%d:%d" % (x1, y1, x2, y2)

# ============================================================ ROOT
project = {"_type": "Project", "_id": nid(), "name": "TaskManagementSystem",
           "ownedElements": []}
model = el("UMLModel", project, name="Model", ownedElements=[])
project["ownedElements"].append(model)

# ============================================================ 1) CLASS (reverse Java)
cd = el("UMLClassDiagram", model, name="Class Diagram - Domain (reverse Java)",
        defaultDiagram=True, ownedViews=[])
model["ownedElements"].append(cd)
_boxes["Class"] = []
CW = 225

def make_class(name, attrs, ops, x, y):
    c = el("UMLClass", model, name=name, attributes=[], operations=[])
    for an, at in attrs:
        c["attributes"].append(el("UMLAttribute", c, name=an, type=at, visibility="private"))
    for on, ot in ops:
        op = el("UMLOperation", c, name=on, visibility="protected", parameters=[])
        op["parameters"].append(el("UMLParameter", op, name="", type=ot, direction="return"))
        c["operations"].append(op)
    model["ownedElements"].append(c)
    h = 26 + 15*(len(attrs)+len(ops))
    cv = el("UMLClassView", cd, model=ref(c), left=x, top=y, width=CW, height=h,
            autoResize=False, fillColor="#ffffff", lineColor="#000000", subViews=[])
    namec = el("UMLNameCompartmentView", cv, model=ref(c), subViews=[], left=x, top=y, width=CW, height=22)
    nl = comp_label(namec, c, top=y+3, left=x, w=CW); namec["subViews"].append(nl); namec["nameLabel"] = ref(nl)
    ac = el("UMLAttributeCompartmentView", cv, model=ref(c), subViews=[], left=x, top=y+22, width=CW, height=15*max(1, len(attrs)))
    for a in c["attributes"]:
        ac["subViews"].append(el("UMLAttributeView", ac, model=ref(a), visible=True))
    oc = el("UMLOperationCompartmentView", cv, model=ref(c), subViews=[], left=x, top=y+22+15*len(attrs), width=CW, height=15*max(1, len(ops)))
    for o in c["operations"]:
        oc["subViews"].append(el("UMLOperationView", oc, model=ref(o), visible=True))
    cv["subViews"] = [namec, ac, oc]
    cv["nameCompartment"] = ref(namec); cv["attributeCompartment"] = ref(ac); cv["operationCompartment"] = ref(oc)
    cd["ownedViews"].append(cv)
    _boxes["Class"].append((name, x, y, CW, h))
    return c, cv

def make_enum(name, literals, x, y):
    e = el("UMLEnumeration", model, name=name, literals=[])
    for lit in literals:
        e["literals"].append(el("UMLEnumerationLiteral", e, name=lit))
    model["ownedElements"].append(e)
    h = 40 + 15*len(literals)
    ev = el("UMLEnumerationView", cd, model=ref(e), left=x, top=y, width=170, height=h,
            autoResize=False, fillColor="#ffffff", lineColor="#000000", subViews=[])
    namec = el("UMLNameCompartmentView", ev, model=ref(e), subViews=[], left=x, top=y, width=170, height=22)
    st = el("LabelView", namec, model=ref(e), visible=True, horizontalAlignment=2,
            text="«enumeration»", left=x, top=y+2, width=170, height=13, font="Arial;11;2")
    nl = comp_label(namec, e, top=y+12, left=x, w=170)
    namec["subViews"] = [st, nl]; namec["nameLabel"] = ref(nl); namec["stereotypeLabel"] = ref(st)
    lc = el("UMLEnumerationLiteralCompartmentView", ev, model=ref(e), subViews=[], left=x, top=y+36, width=170, height=15*len(literals))
    for lit in e["literals"]:
        lc["subViews"].append(el("UMLEnumerationLiteralView", lc, model=ref(lit), visible=True))
    ev["subViews"] = [namec, lc]
    ev["nameCompartment"] = ref(namec); ev["enumerationLiteralCompartment"] = ref(lc)
    cd["ownedViews"].append(ev)
    _boxes["Class"].append((name, x, y, 170, h))
    return e, ev

# ---- 7 entity (field không phải quan hệ; field quan hệ -> association) ----
cUser = make_class("User", [
    ("userId","Long"),("username","String"),("password","String"),
    ("email","String"),("role","String"),("createdAt","LocalDateTime")],
    [("onCreate","void")], 60, 60)
cEmp = make_class("Employee", [
    ("employeeId","Long"),("firstName","String"),("lastName","String"),
    ("position","String"),("department","String"),("hiredAt","LocalDateTime"),
    ("group","String"),("skills","String")], [("onCreate","void")], 380, 60)
cProj = make_class("Project", [
    ("projectId","Long"),("name","String"),("description","String"),
    ("startDate","LocalDate"),("endDate","LocalDate"),("status","String"),
    ("group","String")], [], 700, 60)
cOffice = make_class("OfficeLocation", [
    ("id","Long"),("name","String"),("address","String"),("latitude","Double"),
    ("longitude","Double"),("radiusMeters","Integer"),("status","Status"),
    ("createdAt","LocalDateTime")], [("onCreate","void")], 1020, 60)
cSug = make_class("Suggestion", [
    ("suggestionId","Long"),("suggestionText","String"),("feedback","String"),
    ("createdAt","LocalDateTime")], [("onCreate","void")], 60, 420)
cAtt = make_class("Attendance", [
    ("attendanceId","Long"),("date","LocalDate"),("checkIn","LocalTime"),
    ("checkOut","LocalTime"),("checkInLat","Double"),("checkInLng","Double"),
    ("checkOutLat","Double"),("checkOutLng","Double"),
    ("checkInDistanceMeters","Integer"),("reviewStatus","ReviewStatus"),
    ("isMocked","Boolean")], [], 380, 420)
cTask = make_class("Task", [
    ("taskId","Long"),("title","String"),("description","String"),
    ("requiredSkills","String"),("dueDate","LocalDate"),("status","String"),
    ("completedAt","LocalDateTime")], [], 700, 420)
# enums
eReview = make_enum("ReviewStatus", ["APPROVED","PENDING_REVIEW","REJECTED"], 380, 760)
eStatus = make_enum("Status", ["ACTIVE","INACTIVE"], 1020, 420)

def cassoc(src, sv, tgt, tv, mult_src, mult_tgt, role):
    """Association có hướng (navigable -> tgt), kèm role + multiplicity (đúng JPA)."""
    rel = el("UMLAssociation", model)
    rel["end1"] = el("UMLAssociationEnd", rel, reference=ref(src), multiplicity=mult_src, navigable=False)
    rel["end2"] = el("UMLAssociationEnd", rel, reference=ref(tgt), name=role, multiplicity=mult_tgt, navigable=True)
    model["ownedElements"].append(rel)
    v = el("UMLAssociationView", cd, model=ref(rel), tail=ref(sv), head=ref(tv),
           lineStyle=1, points=pts(sv, tv), subViews=[])
    nm   = elabel(v, rel, False)
    tR   = elabel(v, rel["end1"], False)
    hR   = textlabel(v, rel["end2"], role, True)
    tP   = textlabel(v, rel["end1"], mult_src, True)
    hP   = textlabel(v, rel["end2"], mult_tgt, True)
    v["subViews"] = [nm, tR, hR, tP, hP]
    v["nameLabel"]=ref(nm); v["tailRoleLabel"]=ref(tR); v["headRoleLabel"]=ref(hR)
    v["tailPropertyLabel"]=ref(tP); v["headPropertyLabel"]=ref(hP)
    cd["ownedViews"].append(v)

cassoc(cEmp[0],cEmp[1], cUser[0],cUser[1], "1","0..1","user")          # @OneToOne
cassoc(cTask[0],cTask[1], cProj[0],cProj[1], "0..*","1","project")     # @ManyToOne
cassoc(cTask[0],cTask[1], cEmp[0],cEmp[1], "0..*","0..1","assignedTo") # @ManyToOne
cassoc(cAtt[0],cAtt[1], cEmp[0],cEmp[1], "0..*","1","employee")        # @ManyToOne
cassoc(cAtt[0],cAtt[1], cOffice[0],cOffice[1], "0..*","0..1","checkInOffice")
cassoc(cSug[0],cSug[1], cUser[0],cUser[1], "0..*","1","user")          # @ManyToOne

def dep(src, sv, tgt, tv):
    d = el("UMLDependency", model, source=ref(src), target=ref(tgt))
    model["ownedElements"].append(d)
    v = el("UMLDependencyView", cd, model=ref(d), tail=ref(sv), head=ref(tv),
           lineStyle=1, points=pts(sv, tv), subViews=[])
    lb = elabel(v, d, False); v["subViews"].append(lb); v["nameLabel"] = ref(lb)
    cd["ownedViews"].append(v)
dep(cAtt[0],cAtt[1], eReview[0],eReview[1])     # reviewStatus : ReviewStatus
dep(cOffice[0],cOffice[1], eStatus[0],eStatus[1])  # status : Status

# ============================================================ 2) ERD (real schema)
erm = el("ERDDataModel", model, name="ERD - PostgreSQL", ownedElements=[])
model["ownedElements"].append(erm)
erd = el("ERDDiagram", erm, name="ERD - CSDL", ownedViews=[])
erm["ownedElements"].append(erd)
_boxes["ERD"] = []
EW = 270

def make_entity(name, cols, x, y):
    e = el("ERDEntity", erm, name=name, columns=[])
    for cn, ct, pk, fk, nn, uq in cols:
        e["columns"].append(el("ERDColumn", e, name=cn, type=ct, primaryKey=bool(pk),
                               foreignKey=bool(fk), nullable=not nn, unique=bool(uq)))
    erm["ownedElements"].append(e)
    h = 26 + 15*len(cols)
    v = el("ERDEntityView", erd, model=ref(e), left=x, top=y, width=EW, height=h,
           autoResize=False, fillColor="#ffffff", lineColor="#000000", subViews=[])
    nl = comp_label(v, e, top=y+3, left=x, w=EW)
    cc = el("ERDColumnCompartmentView", v, model=ref(e), subViews=[], left=x, top=y+22, width=EW, height=15*len(cols))
    for col in e["columns"]:
        cc["subViews"].append(el("ERDColumnView", cc, model=ref(col), visible=True))
    v["subViews"] = [nl, cc]; v["nameLabel"] = ref(nl); v["columnCompartment"] = ref(cc)
    erd["ownedViews"].append(v)
    _boxes["ERD"].append((name, x, y, EW, h))
    return e, v

tUsers = make_entity("users", [
    ("user_id","BIGSERIAL",1,0,1,0),("username","VARCHAR(50)",0,0,1,1),
    ("password","VARCHAR(255)",0,0,1,0),("email","VARCHAR(100)",0,0,1,1),
    ("role","VARCHAR(20)",0,0,1,0),("created_at","TIMESTAMP",0,0,0,0)], 60, 60)
tEmp = make_entity("employees", [
    ("employee_id","BIGSERIAL",1,0,1,0),("user_id","BIGINT",0,1,0,1),
    ("first_name","VARCHAR(50)",0,0,1,0),("last_name","VARCHAR(50)",0,0,1,0),
    ("position","VARCHAR(50)",0,0,0,0),("department","VARCHAR(50)",0,0,0,0),
    ("hired_at","TIMESTAMP",0,0,0,0),("employee_group","VARCHAR(100)",0,0,0,0),
    ("skills","TEXT",0,0,0,0)], 400, 60)
tProj = make_entity("projects", [
    ("project_id","BIGSERIAL",1,0,1,0),("name","VARCHAR(100)",0,0,1,0),
    ("description","TEXT",0,0,0,0),("start_date","DATE",0,0,1,0),
    ("end_date","DATE",0,0,0,0),("status","VARCHAR(50)",0,0,0,0),
    ("project_group","VARCHAR(100)",0,0,0,0)], 740, 60)
tOffice = make_entity("office_locations", [
    ("id","BIGSERIAL",1,0,1,0),("name","VARCHAR(200)",0,0,1,0),
    ("address","VARCHAR(500)",0,0,0,0),("latitude","DOUBLE",0,0,1,0),
    ("longitude","DOUBLE",0,0,1,0),("radius_meters","INTEGER",0,0,1,0),
    ("status","VARCHAR(20)",0,0,1,0),("created_at","TIMESTAMP",0,0,0,0)], 1080, 60)
tSug = make_entity("suggestions", [
    ("suggestion_id","BIGSERIAL",1,0,1,0),("user_id","BIGINT",0,1,0,0),
    ("suggestion_text","TEXT",0,0,1,0),("feedback","TEXT",0,0,0,0),
    ("created_at","TIMESTAMP",0,0,0,0)], 60, 430)
tAtt = make_entity("attendance", [
    ("attendance_id","BIGSERIAL",1,0,1,0),("employee_id","BIGINT",0,1,0,0),
    ("date","DATE",0,0,1,0),("check_in","TIME",0,0,1,0),("check_out","TIME",0,0,0,0),
    ("check_in_lat","DOUBLE",0,0,0,0),("check_in_lng","DOUBLE",0,0,0,0),
    ("check_out_lat","DOUBLE",0,0,0,0),("check_out_lng","DOUBLE",0,0,0,0),
    ("check_in_office_id","BIGINT",0,1,0,0),("check_in_distance_m","INTEGER",0,0,0,0),
    ("review_status","VARCHAR(20)",0,0,0,0),("is_mocked","BOOLEAN",0,0,0,0)], 400, 430)
tTask = make_entity("tasks", [
    ("task_id","BIGSERIAL",1,0,1,0),("project_id","BIGINT",0,1,0,0),
    ("assigned_to","BIGINT",0,1,0,0),("title","VARCHAR(100)",0,0,1,0),
    ("description","TEXT",0,0,0,0),("required_skills","TEXT",0,0,0,0),
    ("due_date","DATE",0,0,0,0),("status","VARCHAR(50)",0,0,0,0),
    ("completed_at","TIMESTAMP",0,0,0,0)], 780, 430)

def erel(a, av, b, bv, label):
    rel = el("ERDRelationship", erm, name=label, identifying=False)
    rel["end1"] = el("ERDRelationshipEnd", rel, reference=ref(a), cardinality="1")
    rel["end2"] = el("ERDRelationshipEnd", rel, reference=ref(b), cardinality="0..*")
    erm["ownedElements"].append(rel)
    v = el("ERDRelationshipView", erd, model=ref(rel), tail=ref(av), head=ref(bv),
           lineStyle=1, points=pts(av, bv), subViews=[])
    nl = textlabel(v, rel, label, True); v["subViews"].append(nl); v["nameLabel"] = ref(nl)
    erd["ownedViews"].append(v)
erel(tUsers[0],tUsers[1], tEmp[0],tEmp[1], "user_id")
erel(tProj[0],tProj[1], tTask[0],tTask[1], "project_id")
erel(tEmp[0],tEmp[1], tTask[0],tTask[1], "assigned_to")
erel(tEmp[0],tEmp[1], tAtt[0],tAtt[1], "employee_id")
erel(tOffice[0],tOffice[1], tAtt[0],tAtt[1], "check_in_office_id")
erel(tUsers[0],tUsers[1], tSug[0],tSug[1], "user_id")

# ============================================================ 3) USE CASE
ucd = el("UMLUseCaseDiagram", model, name="Use Case - Tổng thể", ownedViews=[])
model["ownedElements"].append(ucd)
_boxes["UseCase"] = []

def make_actor(name, x, y):
    a = el("UMLActor", model, name=name); model["ownedElements"].append(a)
    v = el("UMLActorView", ucd, model=ref(a), left=x, top=y, width=40, height=64,
           autoResize=False, fillColor="#ffffff", lineColor="#000000", subViews=[])
    lbl = node_label(v, a, top=y+68, left=x-80, w=200, font="Arial;13;1")
    v["subViews"].append(lbl); v["nameLabel"] = ref(lbl)
    ucd["ownedViews"].append(v); return a, v

aUser,  vUser  = make_actor("Người dùng (EMPLOYEE)", 60, 170)
aMgr,   vMgr   = make_actor("Quản lý (MANAGER)", 60, 470)
aAdmin, vAdmin = make_actor("Quản trị (ADMIN)", 60, 770)

UCW, UCH = 230, 60
def make_uc(code, title, x, y):
    name = "%s - %s" % (code, title)
    uc = el("UMLUseCase", model, name=name); model["ownedElements"].append(uc)
    v = el("UMLUseCaseView", ucd, model=ref(uc), left=x, top=y, width=UCW, height=UCH,
           autoResize=False, fillColor="#ffffff", lineColor="#000000", subViews=[])
    lbl = node_label(v, uc, top=y+UCH//2-8, left=x+6, w=UCW-12)
    v["subViews"].append(lbl); v["nameLabel"] = ref(lbl)
    ucd["ownedViews"].append(v); _boxes["UseCase"].append((code, x, y, UCW, UCH))
    return uc, v

emp_col = [("UC-01","Đăng nhập"),("UC-02","Đăng ký"),("UC-03","Đăng xuất"),
           ("UC-06","Xem công việc của tôi"),("UC-07","Cập nhật trạng thái"),
           ("UC-09","Chấm công vào/ra")]
mgr_col = [("UC-04","Quản lý nhân viên"),("UC-05","Quản lý dự án"),
           ("UC-08","Tạo & gán công việc"),("UC-10","Xem báo cáo chấm công"),
           ("UC-11","Gợi ý nhân viên bằng AI"),("UC-12","Phân quyền tài khoản"),
           ("UC-13","Xem logs hệ thống"),("UC-14","Quản lý cấu hình")]
ucs = {}
for i,(c,t) in enumerate(emp_col): ucs[c] = make_uc(c, t, 360, 120+i*100)
for i,(c,t) in enumerate(mgr_col): ucs[c] = make_uc(c, t, 720, 110+i*95)

def assoc_uc(actor, av, uc):
    ucm, ucv = uc
    a = el("UMLAssociation", model)
    a["end1"] = el("UMLAssociationEnd", a, reference=ref(actor))
    a["end2"] = el("UMLAssociationEnd", a, reference=ref(ucm))
    model["ownedElements"].append(a)
    v = el("UMLAssociationView", ucd, model=ref(a), tail=ref(av), head=ref(ucv),
           lineStyle=1, points=pts(av, ucv), subViews=[])
    for nm in ("nameLabel","tailRoleLabel","headRoleLabel","tailPropertyLabel","headPropertyLabel"):
        lb = elabel(v, a, False); v["subViews"].append(lb); v[nm] = ref(lb)
    ucd["ownedViews"].append(v)
for c in ["UC-01","UC-02","UC-03","UC-06","UC-07","UC-09"]: assoc_uc(aUser, vUser, ucs[c])
for c in ["UC-04","UC-05","UC-08","UC-10","UC-11"]: assoc_uc(aMgr, vMgr, ucs[c])
for c in ["UC-12","UC-13","UC-14"]: assoc_uc(aAdmin, vAdmin, ucs[c])

def gen(child, cv, parent, pv):
    g = el("UMLGeneralization", model, source=ref(child), target=ref(parent))
    model["ownedElements"].append(g)
    v = el("UMLGeneralizationView", ucd, model=ref(g), tail=ref(cv), head=ref(pv),
           lineStyle=1, points=pts(cv, pv), subViews=[])
    lb = elabel(v, g, False); v["subViews"].append(lb); v["nameLabel"] = ref(lb)
    ucd["ownedViews"].append(v)
gen(aMgr, vMgr, aUser, vUser)
gen(aAdmin, vAdmin, aMgr, vMgr)

# ============================================================ SELF-CHECK
def ov(a, b):
    _,ax,ay,aw,ah=a; _,bx,by,bw,bh=b
    return not (ax+aw<=bx or bx+bw<=ax or ay+ah<=by or by+bh<=ay)
bad = 0
for dg, bxs in _boxes.items():
    for i in range(len(bxs)):
        for j in range(i+1, len(bxs)):
            if ov(bxs[i], bxs[j]):
                bad += 1; print("  CHỒNG [%s]: %s <-> %s" % (dg, bxs[i][0], bxs[j][0]))
print("Self-check chồng lấn:", "OK" if bad == 0 else "%d cặp" % bad)

out = Path(__file__).resolve().parent.parent / "taskhub.mdj"
out.write_text(json.dumps(project, ensure_ascii=False, indent=2), encoding="utf-8")
print("OK ->", out)
print("Class: 7 entity + 2 enum + 6 association + 2 dependency | ERD: 7 bảng | Use Case")
