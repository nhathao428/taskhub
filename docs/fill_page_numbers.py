# -*- coding: utf-8 -*-
"""
Hậu xử lý: mở BAO_CAO_DO_AN_CO_SO.docx bằng Word (COM), cập nhật mục lục,
đọc số trang thực tế của từng caption "Bảng X.Y" / "Hình X.Y" rồi điền vào
cột "Trang" của Danh mục bảng và Danh mục hình. Chạy SAU build_baocao_coban.py.
"""
import os, re, sys
import win32com.client as win32

DOCX = r"C:\Users\Admin\taskhub\docs\BAO_CAO_DO_AN_CO_SO.docx"

WD_ADJUSTED_PAGE = 1  # wdActiveEndAdjustedPageNumber (số trang hiển thị, theo định dạng section)

cap_re = re.compile(r'^(Bảng|Hình)\s+(\d+\.\d+)\.')      # caption trong thân bài: "Bảng 2.1. ..."
key_re = re.compile(r'^(Bảng|Hình)\s+(\d+\.\d+)\s*$')    # ô số hiệu trong danh mục: "Bảng 2.1"


def clean(t):
    return t.replace('\r', '').replace('\x07', '').replace('\x0c', '').strip()


def main():
    started = False
    try:
        word = win32.GetActiveObject('Word.Application')  # bám vào Word đang mở của user
    except Exception:
        word = win32.gencache.EnsureDispatch('Word.Application')
        started = True
    word.DisplayAlerts = False

    # Tìm doc đang mở (bản user đã thu nhỏ ảnh); nếu chưa mở thì mở từ đĩa
    doc = None
    target = os.path.basename(DOCX).lower()
    for d in word.Documents:
        try:
            if d.Name.lower() == target:
                doc = d; break
        except Exception:
            pass
    opened_here = False
    if doc is None:
        doc = word.Documents.Open(DOCX)
        opened_here = True
    try:
        # Cập nhật toàn bộ field (mục lục) + phân trang lại
        for toc in doc.TablesOfContents:
            toc.Update()
        doc.Fields.Update()
        doc.Repaginate()

        # 1) Quét caption trong thân bài -> số trang
        page_of = {}
        for p in doc.Paragraphs:
            txt = clean(p.Range.Text)
            m = cap_re.match(txt)
            if m:
                key = f"{m.group(1)} {m.group(2)}"
                if key not in page_of:  # lần xuất hiện đầu = caption thật
                    try:
                        page = int(p.Range.Information(WD_ADJUSTED_PAGE))
                        page_of[key] = page
                    except Exception:
                        pass
        print(f"Tìm thấy {len(page_of)} caption có số trang")

        # 2) Điền vào cột Trang của các bảng danh mục
        filled = 0
        for tbl in doc.Tables:
            ncol = tbl.Columns.Count
            if ncol < 3:
                continue
            # kiểm tra header có 'Số hiệu' không
            header = clean(tbl.Cell(1, 1).Range.Text)
            if 'Số hiệu' not in header:
                continue
            for r in range(2, tbl.Rows.Count + 1):
                key = clean(tbl.Cell(r, 1).Range.Text)
                m = key_re.match(key)
                if not m:
                    continue
                k = f"{m.group(1)} {m.group(2)}"
                if k in page_of:
                    cell = tbl.Cell(r, ncol)
                    # xóa nội dung cũ ('…') rồi ghi số trang
                    rng = cell.Range
                    rng.End = rng.End - 1  # bỏ cell mark
                    rng.Text = str(page_of[k])
                    filled += 1
        print(f"Đã điền {filled} số trang vào danh mục")

        doc.Fields.Update()
        doc.Save()
    finally:
        # Chỉ đóng nếu chính script này mở file; KHÔNG đụng tới Word của user
        if opened_here:
            doc.Close(SaveChanges=True)
        if started:
            word.Quit()
    print("Done.")


if __name__ == "__main__":
    main()
