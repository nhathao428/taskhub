import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:task_management_system/widgets/status_badge.dart';

void main() {
  group('StatusBadge', () {
    Future<void> pumpBadge(WidgetTester tester, String status) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: StatusBadge(status: status),
          ),
        ),
      );
    }

    testWidgets('renders pending label for "pending"', (tester) async {
      await pumpBadge(tester, 'pending');
      expect(find.text('Chờ xử lý'), findsOneWidget);
    });

    testWidgets('renders in-progress label for "in_progress"', (tester) async {
      await pumpBadge(tester, 'in_progress');
      expect(find.text('Đang làm'), findsOneWidget);
    });

    testWidgets('renders completed label for "completed"', (tester) async {
      await pumpBadge(tester, 'completed');
      expect(find.text('Hoàn thành'), findsOneWidget);
    });

    testWidgets('renders raw text for unknown status', (tester) async {
      await pumpBadge(tester, 'unknown');
      expect(find.text('unknown'), findsOneWidget);
    });
  });
}
