import 'dart:ui';
import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

/// Background "aurora mesh" — slate-950 + 3 radial blob (indigo, fuchsia, sky)
/// + grid texture mờ. Tái sử dụng cho Login, Register, Dashboard hero.
///
/// Cách dùng:
/// ```dart
/// AuroraBackground(child: ...);
/// ```
class AuroraBackground extends StatelessWidget {
  final Widget child;

  /// Nếu true → background full slate-950 với blob mạnh (dùng làm trang Login).
  /// Nếu false → blob mờ nhẹ trên transparent (overlay cho hero card).
  final bool fullScreen;

  const AuroraBackground({
    super.key,
    required this.child,
    this.fullScreen = true,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        if (fullScreen) const Positioned.fill(child: ColoredBox(color: AppTheme.slate950)),
        // Indigo blob top-left
        Positioned(
          top: -120,
          left: -100,
          child: _Blob(
            color: AppTheme.brand500.withValues(alpha: fullScreen ? 0.42 : 0.55),
            size: 360,
          ),
        ),
        // Fuchsia blob top-right
        Positioned(
          top: -80,
          right: -120,
          child: _Blob(
            color: AppTheme.fuchsia500.withValues(alpha: fullScreen ? 0.32 : 0.45),
            size: 320,
          ),
        ),
        // Sky blob bottom-center
        Positioned(
          bottom: -140,
          left: 40,
          right: 40,
          child: Center(
            child: _Blob(
              color: AppTheme.sky500.withValues(alpha: fullScreen ? 0.28 : 0.40),
              size: 360,
            ),
          ),
        ),
        // Grid texture (chỉ áp dụng cho fullscreen mode)
        if (fullScreen)
          const Positioned.fill(
            child: IgnorePointer(child: _GridTexture()),
          ),
        // Nội dung
        child,
      ],
    );
  }
}

class _Blob extends StatelessWidget {
  final Color color;
  final double size;
  const _Blob({required this.color, required this.size});

  @override
  Widget build(BuildContext context) {
    return ImageFiltered(
      imageFilter: ImageFilter.blur(sigmaX: 60, sigmaY: 60),
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(color: color, shape: BoxShape.circle),
      ),
    );
  }
}

class _GridTexture extends StatelessWidget {
  const _GridTexture();
  @override
  Widget build(BuildContext context) {
    return Opacity(
      opacity: 0.04,
      child: CustomPaint(painter: _GridPainter(), size: Size.infinite),
    );
  }
}

class _GridPainter extends CustomPainter {
  static const double cell = 40;
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.white
      ..strokeWidth = 1;
    for (double x = 0; x < size.width; x += cell) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += cell) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
