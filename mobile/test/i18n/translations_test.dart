import 'package:flutter_test/flutter_test.dart';
import 'package:task_management_system/i18n/translations.dart';

void main() {
  group('translate()', () {
    test('returns key as-is for locale "vi"', () {
      expect(translate('vi', 'Đăng nhập'), 'Đăng nhập');
    });

    test('returns English translation for locale "en"', () {
      expect(translate('en', 'Đăng nhập'), 'Log in');
    });

    test('falls back to key when English translation missing', () {
      expect(translate('en', 'Một chuỗi không tồn tại 12345'),
          'Một chuỗi không tồn tại 12345');
    });

    test('interpolates {name}-style placeholders', () {
      expect(translate('en', 'Xin chào, {name}', {'name': 'Hảo'}), 'Hello, Hảo');
      expect(translate('vi', 'Xin chào, {name}', {'name': 'Hảo'}), 'Xin chào, Hảo');
    });

    test('interpolates multiple placeholders', () {
      expect(translate('en', 'Tỷ lệ hoàn thành: {pct}%', {'pct': 75}),
          'Completion rate: 75%');
    });
  });
}
