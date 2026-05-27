import 'package:flutter/material.dart';

/// Bảng màu & theme dùng chung — đồng bộ với bản web (Tailwind brand palette).
/// Phong cách: brand indigo-600, slate-50 background, soft shadows, rounded-xl.
class AppTheme {
  AppTheme._();

  // ===== Brand palette (khớp tailwind.config.js bên frontend) =====
  static const Color brand50 = Color(0xFFEEF2FF);
  static const Color brand100 = Color(0xFFE0E7FF);
  static const Color brand500 = Color(0xFF6366F1);
  static const Color brand600 = Color(0xFF4F46E5); // primary
  static const Color brand700 = Color(0xFF4338CA);

  // Accent colors (khớp StatCard palette web)
  static const Color sky500 = Color(0xFF0EA5E9);
  static const Color sky100 = Color(0xFFE0F2FE);
  static const Color emerald500 = Color(0xFF10B981);
  static const Color emerald100 = Color(0xFFD1FAE5);
  static const Color amber500 = Color(0xFFF59E0B);
  static const Color amber100 = Color(0xFFFEF3C7);
  static const Color fuchsia500 = Color(0xFFD946EF);
  static const Color rose500 = Color(0xFFF43F5E);

  // Slate (text + bg)
  static const Color slate950 = Color(0xFF020617);
  static const Color slate900 = Color(0xFF0F172A);
  static const Color slate800 = Color(0xFF1E293B);
  static const Color slate700 = Color(0xFF334155);
  static const Color slate500 = Color(0xFF64748B);
  static const Color slate400 = Color(0xFF94A3B8);
  static const Color slate200 = Color(0xFFE2E8F0);
  static const Color slate100 = Color(0xFFF1F5F9);
  static const Color slate50 = Color(0xFFF8FAFC);

  // Aliases để code cũ vẫn chạy
  static const Color primary = brand600;
  static const Color accent = Color(0xFF7C3AED);
  static const Color bg = slate50;
  static const Color surface = Color(0xFFFFFFFF);
  static const Color textMain = slate900;
  static const Color textMuted = slate500;
  static const Color border = slate200;

  /// Gradient thương hiệu (brand 500 → brand 700) — dùng cho nút loading hoặc badge.
  static const LinearGradient brandGradient = LinearGradient(
    colors: [brand500, brand700],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  /// Aurora mesh gradient (giống bg Login web): indigo + fuchsia + sky trên slate-950.
  /// Dùng làm hero Dashboard hoặc Login background.
  static const LinearGradient auroraDark = LinearGradient(
    colors: [
      Color(0xFF1E1B4B), // indigo-950
      Color(0xFF312E81), // indigo-900
      Color(0xFF581C87), // purple-900
    ],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  /// Đổ bóng mềm cho thẻ (tương đương shadow-soft của Tailwind).
  static List<BoxShadow> get softShadow => [
        BoxShadow(
          color: slate900.withValues(alpha: 0.05),
          blurRadius: 12,
          offset: const Offset(0, 4),
        ),
      ];

  /// Bóng trung bình (shadow-soft-md).
  static List<BoxShadow> get softShadowMd => [
        BoxShadow(
          color: slate900.withValues(alpha: 0.08),
          blurRadius: 18,
          offset: const Offset(0, 8),
        ),
      ];

  /// Glow theo brand (shadow-brand-glow) — dùng cho nút chính + logo.
  static List<BoxShadow> get brandGlow => [
        BoxShadow(
          color: brand600.withValues(alpha: 0.30),
          blurRadius: 22,
          offset: const Offset(0, 10),
        ),
      ];

  static ThemeData get light {
    final scheme = ColorScheme.fromSeed(
      seedColor: brand600,
      primary: brand600,
      brightness: Brightness.light,
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: slate50,
      // Subtle letter-spacing để typography hợp với Inter-style của web
      textTheme: const TextTheme(
        headlineSmall: TextStyle(
          fontWeight: FontWeight.bold,
          color: slate900,
          letterSpacing: -0.4,
        ),
        titleLarge: TextStyle(
          fontWeight: FontWeight.w700,
          color: slate900,
          letterSpacing: -0.3,
        ),
        titleMedium: TextStyle(
          fontWeight: FontWeight.w600,
          color: slate900,
          letterSpacing: -0.2,
        ),
        bodyMedium: TextStyle(color: slate700, height: 1.45),
        bodySmall: TextStyle(color: slate500, height: 1.4),
      ),
      appBarTheme: const AppBarTheme(
        centerTitle: false,
        elevation: 0,
        scrolledUnderElevation: 0,
        backgroundColor: slate50,
        foregroundColor: slate900,
        surfaceTintColor: Colors.transparent,
        titleTextStyle: TextStyle(
          color: slate900,
          fontSize: 18,
          fontWeight: FontWeight.w700,
          letterSpacing: -0.2,
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: surface,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(18),
          side: const BorderSide(color: slate200, width: 1),
        ),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: brand600,
          foregroundColor: Colors.white,
          elevation: 0,
          shadowColor: brand600.withValues(alpha: 0.3),
          minimumSize: const Size.fromHeight(50),
          textStyle: const TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.1,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: brand600,
          minimumSize: const Size.fromHeight(50),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: brand600,
          textStyle: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: brand600,
        foregroundColor: Colors.white,
        elevation: 0,
        focusElevation: 0,
        hoverElevation: 0,
        highlightElevation: 0,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: slate50,
        hintStyle: const TextStyle(color: slate400),
        labelStyle: const TextStyle(color: slate500),
        prefixIconColor: brand600,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: slate200),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: brand500, width: 1.6),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: rose500),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: rose500, width: 1.6),
        ),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: Colors.white,
        selectedColor: brand600,
        secondarySelectedColor: brand600,
        labelStyle: const TextStyle(
          color: slate700,
          fontWeight: FontWeight.w600,
          fontSize: 12,
        ),
        secondaryLabelStyle: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.w600,
          fontSize: 12,
        ),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: const BorderSide(color: slate200),
        ),
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 0),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: surface,
        elevation: 0,
        height: 68,
        indicatorColor: brand100,
        labelTextStyle: WidgetStateProperty.all(
          const TextStyle(fontSize: 11.5, fontWeight: FontWeight.w600),
        ),
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: surface,
        selectedItemColor: brand600,
        unselectedItemColor: slate400,
        elevation: 0,
        type: BottomNavigationBarType.fixed,
        selectedLabelStyle:
            TextStyle(fontWeight: FontWeight.w700, fontSize: 11.5),
        unselectedLabelStyle:
            TextStyle(fontWeight: FontWeight.w500, fontSize: 11.5),
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        backgroundColor: slate900,
        contentTextStyle: const TextStyle(color: Colors.white),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: Colors.white,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
        titleTextStyle: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.w700,
          color: slate900,
        ),
      ),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
        ),
      ),
      dividerTheme: const DividerThemeData(color: slate200, thickness: 1),
    );
  }
}
