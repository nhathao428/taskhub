# UML Diagrams (PlantUML)

Toàn bộ sơ đồ UML trong báo cáo và `UML_DIAGRAMS.md` được sinh tự động từ source PlantUML ở `src/*.puml`, render ra PNG ở `png/*.png`.

## Yêu cầu

- Java 11+ (project hiện dùng Java 25)
- Graphviz (đã đi kèm trong plantuml.jar cho hầu hết diagram, riêng class diagram cần dot)
- `plantuml.jar` — tải về `docs/uml/` (file này đã được .gitignore vì nặng ~26 MB):

  ```bash
  curl -fsSL -o docs/uml/plantuml.jar \
    https://github.com/plantuml/plantuml/releases/download/v1.2026.0/plantuml-1.2026.0.jar
  ```

## Render lại toàn bộ sơ đồ

```bash
java -jar docs/uml/plantuml.jar -charset UTF-8 -tpng -o "../png" "docs/uml/src/*.puml"
```

Render 1 file:

```bash
java -jar docs/uml/plantuml.jar -charset UTF-8 -tpng -o "../png" docs/uml/src/erd.puml
```

## Style

Style chung dùng cho mọi sơ đồ nằm ở `src/_style.puml` — tái hiện look StarUML cổ điển: thân vàng nhạt `#FFFFC8`, viền đen `#000000`, góc vuông, font Arial 12, visibility hiển thị dạng ký tự (`-` / `+` / `#` / `~`). Mỗi `.puml` `!include _style.puml` ngay sau `@startuml`.
