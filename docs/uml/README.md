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

Style chung dùng cho mọi sơ đồ nằm ở `src/_style.puml` (Times-like Arial 13, viền xanh `#2C5282`, nền trắng, không đổ bóng) — mỗi `.puml` `!include _style.puml` ngay sau `@startuml` để có look StarUML đồng nhất.
