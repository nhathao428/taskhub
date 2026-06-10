# -*- coding: utf-8 -*-
"""Sinh brochure giới thiệu sản phẩm TaskHub — 6 trang 1920x1080, render JPEG."""
import base64, os

HERE = os.path.dirname(os.path.abspath(__file__))
DOCS = os.path.dirname(HERE)
LOGO = os.path.join(DOCS, "hutech_logo.png")
logo_uri = "data:image/png;base64," + base64.b64encode(open(LOGO, "rb").read()).decode()
POSTER = os.path.join(DOCS, "demo_poster.png")
poster_uri = "data:image/png;base64," + base64.b64encode(open(POSTER, "rb").read()).decode()

CSS = """
@import url('https://fonts.googleapis.com/css2?family=Sora:wght@400;600;700;800&family=Inter:wght@400;500;600&family=JetBrains+Mono:wght@500&display=swap');
:root{
  --bg-0:#0a0e1a; --bg-1:#0f1730; --ink:#eef2ff; --muted:#9aa6c7;
  --accent:#5b8cff; --accent-2:#22d3ee; --accent-3:#a78bfa;
  --line:rgba(148,163,210,0.16); --card:rgba(255,255,255,0.045);
}
*{margin:0;padding:0;box-sizing:border-box;}
body{font-family:'Inter',system-ui,sans-serif;color:var(--ink);background:#05080f;}
.page{
  position:relative; width:1920px; height:1080px; overflow:hidden;
  background:
    radial-gradient(1100px 700px at 80% -8%, rgba(91,140,255,0.26), transparent 60%),
    radial-gradient(900px 700px at 6% 112%, rgba(167,139,250,0.18), transparent 55%),
    radial-gradient(700px 600px at 100% 100%, rgba(34,211,238,0.14), transparent 55%),
    linear-gradient(160deg, var(--bg-1), var(--bg-0) 72%);
}
.page::before{content:"";position:absolute;inset:0;
  background-image:linear-gradient(rgba(148,163,210,0.045) 1px,transparent 1px),linear-gradient(90deg,rgba(148,163,210,0.045) 1px,transparent 1px);
  background-size:56px 56px; mask-image:radial-gradient(1200px 820px at 60% 40%,#000 30%,transparent 80%);}
.stage{position:relative;width:100%;height:100%;padding:70px 92px;display:flex;flex-direction:column;}

/* header common */
.hdr{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;}
.hbrand{display:flex;align-items:center;gap:16px;}
.hbrand .g{width:46px;height:46px;border-radius:13px;background:linear-gradient(145deg,var(--accent),var(--accent-3));box-shadow:0 10px 26px rgba(91,140,255,.4),inset 0 1px 0 rgba(255,255,255,.4);position:relative;}
.hbrand .g::before{content:"";position:absolute;inset:0;border-radius:13px;
  background:linear-gradient(#fff,#fff) 13px 15px/15px 4.5px no-repeat,linear-gradient(#fff,#fff) 13px 23px/22px 4.5px no-repeat,linear-gradient(#fff,#fff) 13px 31px/18px 4.5px no-repeat;opacity:.95;}
.hbrand .w{font-family:'Sora';font-weight:800;font-size:27px;letter-spacing:-.5px;}
.hbrand .w b{color:var(--accent-2);}
.hpage{font-family:'JetBrains Mono',monospace;font-size:14px;letter-spacing:3px;color:var(--muted);text-transform:uppercase;}

.eyebrow{display:inline-flex;align-items:center;gap:12px;color:var(--muted);font-family:'JetBrains Mono',monospace;font-size:15px;letter-spacing:4px;text-transform:uppercase;margin-bottom:18px;}
.eyebrow .dot{width:9px;height:9px;border-radius:50%;background:var(--accent-2);box-shadow:0 0 16px var(--accent-2);}
.title{font-family:'Sora';font-weight:800;font-size:58px;letter-spacing:-1.5px;line-height:1.05;}
.title .hl{background:linear-gradient(120deg,#fff,var(--accent-2));-webkit-background-clip:text;background-clip:text;color:transparent;}
.lead{font-size:22px;color:var(--muted);line-height:1.6;max-width:1100px;margin-top:18px;}

.foot-mini{position:absolute;left:92px;right:92px;bottom:46px;display:flex;justify-content:space-between;align-items:center;
  font-family:'JetBrains Mono',monospace;font-size:13px;letter-spacing:2px;color:var(--muted);text-transform:uppercase;border-top:1px solid var(--line);padding-top:18px;}
"""

# ---------- PAGE 1: COVER ----------
P1 = f"""
<section class="page" id="p1"><div class="stage" style="padding:64px 84px;">
  <div class="hdr" style="margin-bottom:0;">
    <div class="hbrand"><img src="{logo_uri}" style="height:70px;filter:drop-shadow(0 6px 20px rgba(0,0,0,.5));">
      <div style="line-height:1.35;"><div style="font-weight:700;font-size:20px;letter-spacing:.5px;">TRƯỜNG ĐẠI HỌC CÔNG NGHỆ TP.HCM</div>
      <div style="font-size:15px;color:var(--muted);letter-spacing:2px;text-transform:uppercase;">Khoa Công nghệ Thông tin</div></div></div>
    <div style="font-family:'JetBrains Mono',monospace;font-size:14px;letter-spacing:3px;color:var(--accent-2);border:1px solid var(--line);border-radius:999px;padding:10px 20px;background:var(--card);text-transform:uppercase;">Hồ sơ sản phẩm · 2026</div>
  </div>
  <div style="flex:1;display:grid;grid-template-columns:1.05fr .95fr;gap:54px;align-items:center;margin-top:14px;">
    <div>
      <div class="eyebrow"><span class="dot"></span> Giới thiệu sản phẩm</div>
      <div style="display:flex;align-items:center;gap:24px;margin-bottom:20px;">
        <div style="width:92px;height:92px;border-radius:25px;background:linear-gradient(145deg,var(--accent),var(--accent-3));box-shadow:0 18px 50px rgba(91,140,255,.45),inset 0 1px 0 rgba(255,255,255,.4);position:relative;">
          <span style="position:absolute;inset:0;border-radius:25px;background:linear-gradient(#fff,#fff) 25px 30px/29px 8.5px no-repeat,linear-gradient(#fff,#fff) 25px 47px/42px 8.5px no-repeat,linear-gradient(#fff,#fff) 25px 64px/34px 8.5px no-repeat;"></span></div>
        <div style="font-family:'Sora';font-weight:800;font-size:98px;letter-spacing:-2px;line-height:.95;">Task<b style="background:linear-gradient(120deg,#fff,#c9d6ff 45%,var(--accent-2));-webkit-background-clip:text;background-clip:text;color:transparent;">Hub</b></div>
      </div>
      <div style="font-family:'Sora';font-weight:700;font-size:35px;line-height:1.22;margin-bottom:16px;">Hệ thống <span style="color:var(--accent-2);">Quản lý Công việc</span><br>cho Doanh nghiệp Nhỏ Đa ngành · Tích hợp AI</div>
      <div class="lead" style="margin-top:0;margin-bottom:30px;">Nền tảng hợp nhất quản lý nhân sự, chấm công, dự án và tiến độ công việc — kèm trợ lý AI tự động gợi ý nhân viên phù hợp cho từng nhiệm vụ.</div>
      <div style="display:flex;align-items:center;gap:14px;flex-wrap:wrap;">
        <span style="font-family:'JetBrains Mono',monospace;font-size:13px;letter-spacing:2px;color:var(--muted);text-transform:uppercase;">Tech stack</span>
        {''.join(f'<span style="font-family:JetBrains Mono,monospace;font-size:15px;color:#dfe7ff;border:1px solid var(--line);border-radius:9px;padding:8px 13px;background:rgba(91,140,255,.08);">{t}</span>' for t in ['Spring Boot','React','Flutter','PostgreSQL','Redis','Docker','Gemini AI'])}
      </div>
    </div>
    <div>
      <div style="height:540px;border-radius:24px;overflow:hidden;background:linear-gradient(160deg,rgba(20,28,54,.95),rgba(12,18,38,.95));border:1px solid var(--line);box-shadow:0 40px 90px rgba(0,0,0,.55),inset 0 1px 0 rgba(255,255,255,.06);">
        <div style="display:flex;align-items:center;gap:9px;padding:18px 22px;border-bottom:1px solid var(--line);">
          <i style="width:13px;height:13px;border-radius:50%;background:#ff5f57;display:inline-block;"></i><i style="width:13px;height:13px;border-radius:50%;background:#febc2e;display:inline-block;"></i><i style="width:13px;height:13px;border-radius:50%;background:#28c840;display:inline-block;"></i>
          <span style="margin-left:14px;font-size:15px;color:var(--muted);font-family:'JetBrains Mono',monospace;">taskhub · dashboard</span></div>
        <div style="padding:26px;display:grid;grid-template-columns:repeat(2,1fr);gap:18px;">
          {''.join(f'<div style="background:var(--card);border:1px solid var(--line);border-radius:16px;padding:20px 22px;"><div style="font-size:14px;color:var(--muted);letter-spacing:1px;text-transform:uppercase;">{k}</div><div style="font-family:Sora;font-weight:800;font-size:38px;margin-top:8px;color:{c};">{v}</div></div>' for k,v,c in [('Dự án','24','var(--accent-2)'),('Công việc','187','var(--accent-3)'),('Nhân viên','42','#fff'),('Đúng hạn','93%','#7df2c0')])}
          <div style="grid-column:1/3;background:linear-gradient(120deg,rgba(91,140,255,.16),rgba(167,139,250,.12));border:1px solid rgba(123,150,255,.32);border-radius:16px;padding:22px 24px;">
            <div style="display:flex;align-items:center;gap:10px;font-weight:600;font-size:18px;margin-bottom:12px;">✨ AI gợi ý nhân viên <span style="font-family:JetBrains Mono,monospace;font-size:12px;color:#0a0e1a;background:var(--accent-2);padding:4px 9px;border-radius:6px;letter-spacing:1px;">GEMINI</span></div>
            <div style="display:flex;justify-content:space-between;padding:11px 0;font-size:16px;"><span>Thiết kế UI màn hình chấm công</span><span style="font-family:JetBrains Mono,monospace;color:var(--accent-2);">96%</span></div>
            <div style="display:flex;justify-content:space-between;padding:11px 0;border-top:1px dashed var(--line);font-size:16px;"><span style="color:var(--muted);">Nguyễn Văn A · Frontend</span><span style="font-family:JetBrains Mono,monospace;color:var(--accent-2);">↑ rảnh</span></div>
          </div>
        </div></div>
    </div>
  </div>
  <div class="foot-mini"><span>GVHD: ThS. Dương Thành Phết</span><span>SVTH: Nguyễn Nhật Hào · 2380612688 · 23DTHC1</span><span>TP.HCM · 2026</span></div>
</div></section>
"""

def header(num, total=7):
    return f"""<div class="hdr"><div class="hbrand"><span class="g"></span><span class="w">Task<b>Hub</b></span></div>
    <div class="hpage">0{num} / 0{total}</div></div>"""

# ---------- PAGE 2: PROBLEM / SOLUTION ----------
pains = [
    ("📑","Dữ liệu rời rạc","Excel, Zalo, giấy tờ — thông tin nhân sự & công việc nằm rải rác, khó tổng hợp."),
    ("🎯","Phân công cảm tính","Giao việc theo thói quen, không dựa trên năng lực hay khối lượng hiện có của nhân viên."),
    ("⏱️","Chấm công thủ công","Theo dõi giờ giấc bằng tay, dễ sai sót, khó kiểm soát nhân viên làm từ xa / nhiều chi nhánh."),
    ("📉","Mất dấu tiến độ","Không biết dự án đang tới đâu, ai trễ hạn, hiệu suất nhóm ra sao."),
]
pain_html = "".join(f"""<div style="display:flex;gap:18px;align-items:flex-start;background:var(--card);border:1px solid var(--line);border-radius:18px;padding:24px 26px;">
  <div style="font-size:30px;">{i}</div><div><div style="font-family:Sora;font-weight:700;font-size:23px;margin-bottom:6px;">{t}</div>
  <div style="color:var(--muted);font-size:17px;line-height:1.55;">{d}</div></div></div>""" for i,t,d in pains)

P2 = f"""
<section class="page" id="p2"><div class="stage">
  {header(2)}
  <div class="eyebrow" style="margin-top:30px;"><span class="dot"></span> Bài toán</div>
  <div class="title">Doanh nghiệp nhỏ đang <span class="hl">vận hành công việc</span> ra sao?</div>
  <div style="display:grid;grid-template-columns:1.15fr .85fr;gap:46px;margin-top:40px;flex:1;align-items:center;">
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:18px;">{pain_html}</div>
    <div style="background:linear-gradient(150deg,rgba(91,140,255,.18),rgba(34,211,238,.10));border:1px solid rgba(123,150,255,.34);border-radius:24px;padding:42px 40px;box-shadow:0 30px 70px rgba(0,0,0,.45);">
      <div style="font-family:JetBrains Mono,monospace;font-size:14px;letter-spacing:3px;color:var(--accent-2);text-transform:uppercase;margin-bottom:18px;">Giải pháp</div>
      <div style="font-family:Sora;font-weight:800;font-size:40px;line-height:1.15;margin-bottom:20px;">Một nền tảng<br>thay cho tất cả.</div>
      <div style="font-size:19px;color:#d7deff;line-height:1.65;">TaskHub gom nhân sự, chấm công, dự án và công việc về một nơi — thêm lớp <b style="color:var(--accent-2);">AI</b> giúp phân công đúng người, đúng việc, đúng thời điểm.</div>
      <div style="margin-top:28px;display:flex;gap:12px;flex-wrap:wrap;">
        <span style="background:rgba(255,255,255,.08);border:1px solid var(--line);border-radius:999px;padding:9px 16px;font-size:15px;">Tập trung</span>
        <span style="background:rgba(255,255,255,.08);border:1px solid var(--line);border-radius:999px;padding:9px 16px;font-size:15px;">Tự động hóa</span>
        <span style="background:rgba(255,255,255,.08);border:1px solid var(--line);border-radius:999px;padding:9px 16px;font-size:15px;">Realtime</span>
      </div>
    </div>
  </div>
</div></section>
"""

# ---------- PAGE 3: FEATURES BENTO ----------
feats = [
    ("👥","Quản lý nhân sự","CRUD nhân viên, hồ sơ phòng ban / chức vụ / nhóm làm việc.","span2"),
    ("📋","Dự án & công việc","Tạo dự án, gắn task, theo dõi trạng thái và deadline trực quan.",""),
    ("📍","Chấm công + GPS","Check-in/out kèm geofence định vị, duyệt công, lịch sử chấm công.",""),
    ("📊","Dashboard & báo cáo","Biểu đồ Chart.js: tiến độ, hiệu suất, tỉ lệ đúng hạn theo thời gian.",""),
    ("🔐","Phân quyền JWT","3 vai trò Admin / Manager / Employee, bảo mật Spring Security.",""),
    ("📱","Đa nền tảng","Web React + ứng dụng di động Flutter dùng chung một REST API.","span2"),
]
def fcard(i,t,d,cls):
    span = "grid-column:span 2;" if cls=="span2" else ""
    return f"""<div style="{span}background:var(--card);border:1px solid var(--line);border-radius:20px;padding:30px 32px;display:flex;flex-direction:column;gap:12px;">
    <div style="font-size:34px;">{i}</div><div style="font-family:Sora;font-weight:700;font-size:25px;">{t}</div>
    <div style="color:var(--muted);font-size:17px;line-height:1.55;">{d}</div></div>"""
feat_html = "".join(fcard(*f) for f in feats)

P3 = f"""
<section class="page" id="p3"><div class="stage">
  {header(3)}
  <div class="eyebrow" style="margin-top:30px;"><span class="dot"></span> Tính năng</div>
  <div class="title">Mọi thứ để <span class="hl">điều phối công việc</span></div>
  <div style="display:grid;grid-template-columns:repeat(3,1fr);grid-auto-rows:1fr;gap:20px;margin-top:42px;flex:1;">{feat_html}</div>
</div></section>
"""

# ---------- PAGE 4: AI FEATURE ----------
steps = [
    ("1","Đọc bối cảnh","Hệ thống tổng hợp kỹ năng, khối lượng việc hiện tại và lịch sử hiệu suất của từng nhân viên."),
    ("2","Hỏi Gemini","Gửi yêu cầu công việc + dữ liệu nhân sự lên Google Gemini qua backend (API key được giấu kín)."),
    ("3","Gợi ý xếp hạng","Trả về danh sách nhân viên phù hợp kèm điểm số & lý do, manager chỉ việc xác nhận."),
]
step_html = "".join(f"""<div style="display:flex;gap:20px;align-items:flex-start;">
  <div style="flex:none;width:50px;height:50px;border-radius:14px;background:linear-gradient(145deg,var(--accent),var(--accent-3));display:flex;align-items:center;justify-content:center;font-family:Sora;font-weight:800;font-size:24px;">{n}</div>
  <div><div style="font-family:Sora;font-weight:700;font-size:24px;margin-bottom:6px;">{t}</div><div style="color:var(--muted);font-size:18px;line-height:1.55;max-width:560px;">{d}</div></div></div>""" for n,t,d in steps)

P4 = f"""
<section class="page" id="p4"><div class="stage">
  {header(4)}
  <div style="display:grid;grid-template-columns:1fr .95fr;gap:56px;flex:1;align-items:center;margin-top:24px;">
    <div>
      <div class="eyebrow"><span class="dot"></span> Điểm nhấn · AI</div>
      <div class="title">Trợ lý AI <span class="hl">gợi ý phân công</span></div>
      <div class="lead" style="margin-bottom:40px;">Không còn giao việc theo cảm tính — TaskHub dùng Google Gemini để đề xuất nhân viên phù hợp nhất cho mỗi nhiệm vụ.</div>
      <div style="display:flex;flex-direction:column;gap:28px;">{step_html}</div>
    </div>
    <div style="background:linear-gradient(160deg,rgba(20,28,54,.96),rgba(12,18,38,.96));border:1px solid rgba(123,150,255,.30);border-radius:24px;padding:34px;box-shadow:0 40px 90px rgba(0,0,0,.55);">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:24px;">
        <div style="font-family:Sora;font-weight:700;font-size:24px;">Gợi ý cho task mới</div>
        <span style="font-family:JetBrains Mono,monospace;font-size:13px;color:#0a0e1a;background:var(--accent-2);padding:6px 12px;border-radius:8px;letter-spacing:1px;">GEMINI 2.5</span></div>
      <div style="font-size:16px;color:var(--muted);margin-bottom:8px;">Nhiệm vụ</div>
      <div style="background:var(--card);border:1px solid var(--line);border-radius:14px;padding:18px 20px;font-size:18px;margin-bottom:24px;">“Thiết kế giao diện màn hình chấm công có bản đồ GPS”</div>
      <div style="font-size:16px;color:var(--muted);margin-bottom:12px;">Nhân viên được đề xuất</div>
      {''.join(f'''<div style="display:flex;align-items:center;gap:16px;background:var(--card);border:1px solid var(--line);border-radius:14px;padding:16px 18px;margin-bottom:12px;">
        <div style="width:42px;height:42px;border-radius:50%;background:linear-gradient(145deg,{c1},{c2});flex:none;"></div>
        <div style="flex:1;"><div style="font-size:18px;font-weight:600;">{nm}</div><div style="font-size:14px;color:var(--muted);">{role}</div></div>
        <div style="text-align:right;"><div style="font-family:JetBrains Mono,monospace;font-size:22px;color:var(--accent-2);">{sc}</div><div style="font-size:12px;color:var(--muted);">phù hợp</div></div></div>'''
        for nm,role,sc,c1,c2 in [('Nguyễn Văn A','Frontend · React','96%','#5b8cff','#22d3ee'),('Trần Thị B','Mobile · Flutter','88%','#a78bfa','#5b8cff'),('Lê Văn C','Fullstack','74%','#22d3ee','#7df2c0')])}
    </div>
  </div>
</div></section>
"""

# ---------- PAGE 5: ARCHITECTURE ----------
def tier(title, color, items, chips):
    its = "".join(f'<div style="font-size:17px;color:#dbe2ff;margin:6px 0;">• {x}</div>' for x in items)
    chs = "".join(f'<span style="font-family:JetBrains Mono,monospace;font-size:14px;color:#dfe7ff;border:1px solid var(--line);border-radius:8px;padding:6px 11px;background:rgba(91,140,255,.08);">{c}</span>' for c in chips)
    return f"""<div style="flex:1;background:var(--card);border:1px solid var(--line);border-top:3px solid {color};border-radius:18px;padding:28px;">
      <div style="font-family:Sora;font-weight:700;font-size:24px;margin-bottom:14px;color:{color};">{title}</div>{its}
      <div style="display:flex;gap:8px;flex-wrap:wrap;margin-top:16px;">{chs}</div></div>"""

P5 = f"""
<section class="page" id="p5"><div class="stage">
  {header(5)}
  <div class="eyebrow" style="margin-top:30px;"><span class="dot"></span> Kiến trúc</div>
  <div class="title">Kiến trúc <span class="hl">3 tầng</span> hiện đại</div>
  <div style="display:flex;align-items:stretch;gap:26px;margin-top:48px;">
    {tier('Client','#22d3ee',['Web app (React + Vite + Tailwind)','Mobile app (Flutter)','Giao tiếp REST qua Axios + JWT'],['React 18','Flutter 3','Chart.js'])}
    <div style="display:flex;align-items:center;font-size:40px;color:var(--muted);">→</div>
    {tier('Application','#5b8cff',['Spring Boot REST API (port 5000)','Bảo mật JWT + Spring Security','Module AI gọi Google Gemini'],['Spring Boot 3.5','Java 17','JWT'])}
    <div style="display:flex;align-items:center;font-size:40px;color:var(--muted);">→</div>
    {tier('Data','#a78bfa',['PostgreSQL 16 lưu dữ liệu chính','Redis 7 cache + rate limit','Đóng gói bằng Docker Compose'],['PostgreSQL 16','Redis 7','Docker'])}
  </div>
  <div style="margin-top:44px;background:linear-gradient(120deg,rgba(167,139,250,.14),rgba(34,211,238,.10));border:1px solid var(--line);border-radius:18px;padding:26px 30px;display:flex;align-items:center;gap:22px;">
    <div style="font-size:30px;">🤖</div>
    <div style="font-size:19px;color:#e7ecff;line-height:1.5;">Module AI nằm ở tầng Application — gọi <b style="color:var(--accent-2);">Google Gemini (gemini-2.5-flash)</b> để gợi ý nhân viên. API key chỉ ở backend, không bao giờ lộ ra client.</div>
  </div>
  <div style="flex:1;"></div>
</div></section>
"""

# ---------- PAGE 6: DEMO (browser frame + poster; video phủ lên trong pptx) ----------
# Khung video (px trong canvas 1920x1080) — pptx sẽ phủ native movie đúng vùng ảnh poster:
#   frame: left=735 top=300 w=1100 h=700 ; titlebar≈58px → vùng poster: left=735 top=358 w=1100 h=642
demo_steps = [
    ("Đăng nhập & phân quyền", "Đăng nhập JWT, vào dashboard theo vai trò."),
    ("Quản lý dự án & công việc", "Tạo dự án, giao task, theo dõi tiến độ."),
    ("AI gợi ý nhân viên", "Chọn task → nhận đề xuất từ Gemini."),
    ("Chấm công GPS & báo cáo", "Check-in geofence, dashboard biểu đồ."),
]
demo_steps_html = "".join(f"""<div>
  <div style="font-family:Sora;font-weight:700;font-size:22px;color:#eef2ff;"><span style="color:var(--accent-2);">▶</span>&nbsp; {t}</div>
  <div style="color:var(--muted);font-size:16px;margin-top:4px;">{d}</div></div>""" for t, d in demo_steps)

P_DEMO = f"""
<section class="page" id="pdemo"><div class="stage">
  {header(6)}
  <div class="eyebrow" style="margin-top:30px;"><span class="dot"></span> Demo sản phẩm</div>
  <div class="title">Xem <span class="hl">TaskHub</span> vận hành</div>
  <div style="position:absolute;left:92px;top:330px;width:560px;display:flex;flex-direction:column;gap:30px;">{demo_steps_html}</div>
  <div style="position:absolute;left:735px;top:300px;width:1100px;height:700px;border-radius:22px;overflow:hidden;background:linear-gradient(160deg,#141c36,#0c1226);border:1px solid var(--line);box-shadow:0 40px 90px rgba(0,0,0,.55);">
    <div style="display:flex;align-items:center;gap:9px;height:58px;padding:0 22px;border-bottom:1px solid var(--line);box-sizing:border-box;">
      <i style="width:13px;height:13px;border-radius:50%;background:#ff5f57;display:inline-block;"></i>
      <i style="width:13px;height:13px;border-radius:50%;background:#febc2e;display:inline-block;"></i>
      <i style="width:13px;height:13px;border-radius:50%;background:#28c840;display:inline-block;"></i>
      <span style="margin-left:14px;font-size:15px;color:var(--muted);font-family:'JetBrains Mono',monospace;">taskhub · demo</span></div>
    <div style="position:relative;width:100%;height:642px;">
      <img src="{poster_uri}" style="width:100%;height:100%;object-fit:cover;display:block;">
      <div style="position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);width:96px;height:96px;border-radius:50%;background:rgba(91,140,255,.85);box-shadow:0 12px 40px rgba(0,0,0,.5);display:flex;align-items:center;justify-content:center;">
        <span style="color:#fff;font-size:40px;margin-left:6px;">▶</span></div>
    </div>
  </div>
  <div class="foot-mini"><span>TASKHUB · DEMO SẢN PHẨM</span><span>bấm ▶ để phát video</span></div>
</div></section>
"""

# ---------- PAGE 7: AUDIENCE / CLOSING ----------
who = [
    ("🏪","Doanh nghiệp nhỏ đa ngành","Cần một công cụ quản trị công việc gọn nhẹ, không cồng kềnh như ERP."),
    ("👔","Quản lý / chủ doanh nghiệp","Muốn nhìn toàn cảnh nhân sự & tiến độ, phân công nhanh và có cơ sở."),
    ("🧑‍💻","Nhân viên","Tự xem việc được giao, cập nhật trạng thái, tự chấm công minh bạch."),
]
who_html = "".join(f"""<div style="background:var(--card);border:1px solid var(--line);border-radius:20px;padding:30px 30px;">
  <div style="font-size:34px;margin-bottom:14px;">{i}</div><div style="font-family:Sora;font-weight:700;font-size:23px;margin-bottom:8px;">{t}</div>
  <div style="color:var(--muted);font-size:17px;line-height:1.55;">{d}</div></div>""" for i,t,d in who)

P6 = f"""
<section class="page" id="p6"><div class="stage">
  {header(7)}
  <div class="eyebrow" style="margin-top:30px;"><span class="dot"></span> Đối tượng sử dụng</div>
  <div class="title">Dành cho <span class="hl">ai?</span></div>
  <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:22px;margin-top:42px;">{who_html}</div>
  <div style="flex:1;"></div>
  <div style="display:flex;align-items:center;gap:26px;background:linear-gradient(120deg,rgba(91,140,255,.18),rgba(167,139,250,.12));border:1px solid rgba(123,150,255,.32);border-radius:24px;padding:36px 42px;">
    <div style="width:84px;height:84px;border-radius:22px;background:linear-gradient(145deg,var(--accent),var(--accent-3));flex:none;box-shadow:0 14px 40px rgba(91,140,255,.45);position:relative;">
      <span style="position:absolute;inset:0;border-radius:22px;background:linear-gradient(#fff,#fff) 22px 27px/26px 7.5px no-repeat,linear-gradient(#fff,#fff) 22px 42px/38px 7.5px no-repeat,linear-gradient(#fff,#fff) 22px 57px/30px 7.5px no-repeat;"></span></div>
    <div style="flex:1;">
      <div style="font-family:Sora;font-weight:800;font-size:38px;margin-bottom:6px;">TaskHub — Quản lý công việc thông minh hơn.</div>
      <div style="font-size:18px;color:#d7deff;">Đồ án cơ sở · Khoa CNTT · Trường Đại học Công nghệ TP.HCM</div>
    </div>
    <div style="text-align:right;font-size:16px;line-height:1.7;color:#e7ecff;border-left:1px solid var(--line);padding-left:30px;">
      <div><b>GVHD:</b> ThS. Dương Thành Phết</div>
      <div><b>SVTH:</b> Nguyễn Nhật Hào</div>
      <div style="color:var(--muted);">MSSV 2380612688 · Lớp 23DTHC1</div>
      <div style="color:var(--muted);">TP. Hồ Chí Minh · Tháng 5/2026</div>
    </div>
  </div>
</div></section>
"""

HTML = f"""<!DOCTYPE html><html lang="vi"><head><meta charset="utf-8"><style>{CSS}</style></head>
<body>{P1}{P2}{P3}{P4}{P5}{P_DEMO}{P6}</body></html>"""

out = os.path.join(HERE, "brochure.html")
open(out, "w", encoding="utf-8").write(HTML)
print("wrote", out, len(HTML), "bytes")
