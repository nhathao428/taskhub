import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'translations.dart';

/// Quản lý locale toàn app. Mặc định 'vi'. Lưu vào SharedPreferences để giữ lại
/// giữa các phiên. Sử dụng từ widget: `context.tr('...')` hoặc đọc trực tiếp
/// `context.watch<LanguageProvider>().locale`.
class LanguageProvider extends ChangeNotifier {
  static const _prefsKey = 'app.locale';
  static const supported = ['vi', 'en'];

  String _locale = 'vi';

  LanguageProvider() {
    _restore();
  }

  String get locale => _locale;
  bool get isVietnamese => _locale == 'vi';

  Future<void> _restore() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final saved = prefs.getString(_prefsKey);
      if (saved != null && supported.contains(saved)) {
        _locale = saved;
        notifyListeners();
      }
    } catch (_) {
      // ignore: phiên đầu tiên hoặc storage không khả dụng — dùng default 'vi'.
    }
  }

  Future<void> setLocale(String locale) async {
    if (!supported.contains(locale) || locale == _locale) return;
    _locale = locale;
    notifyListeners();
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_prefsKey, locale);
    } catch (_) {
      // ignore
    }
  }

  String tr(String key, [Map<String, Object?> params = const {}]) =>
      translate(_locale, key, params);
}

extension TranslateExt on BuildContext {
  /// Dịch [key] theo locale hiện tại. Đăng ký rebuild khi locale đổi.
  String tr(String key, [Map<String, Object?> params = const {}]) {
    return watch<LanguageProvider>().tr(key, params);
  }

  /// Dịch [key] không đăng ký rebuild (dùng trong callback, không trong build).
  String trOnce(String key, [Map<String, Object?> params = const {}]) {
    return read<LanguageProvider>().tr(key, params);
  }
}
