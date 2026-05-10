import 'package:flutter_test/flutter_test.dart';
import 'package:task_management_system/models/employee_suggestion.dart';

void main() {
  group('EmployeeSuggestion', () {
    test('parses backend JSON with rank + reasoning', () {
      final json = {
        'employeeId': 7,
        'firstName': 'Nguyễn',
        'lastName': 'Văn A',
        'department': 'Kỹ thuật',
        'rank': 1,
        'reasoning': 'Hoàn thành 9/10 task đúng hạn, đi làm đầy đủ.',
      };

      final suggestion = EmployeeSuggestion.fromJson(json);

      expect(suggestion.employeeId, 7);
      expect(suggestion.firstName, 'Nguyễn');
      expect(suggestion.lastName, 'Văn A');
      expect(suggestion.department, 'Kỹ thuật');
      expect(suggestion.rank, 1);
      expect(suggestion.reasoning, contains('Hoàn thành'));
    });

    test('fullName concatenates firstName and lastName', () {
      final suggestion = EmployeeSuggestion(
        employeeId: 1,
        firstName: 'Trần',
        lastName: 'Thị B',
        rank: 2,
      );

      expect(suggestion.fullName, 'Trần Thị B');
    });

    test('handles missing optional fields gracefully', () {
      final suggestion = EmployeeSuggestion.fromJson({
        'employeeId': 5,
        'firstName': 'Lê',
        'lastName': 'C',
      });

      expect(suggestion.department, isNull);
      expect(suggestion.reasoning, isNull);
      expect(suggestion.rank, 0);
    });

    test('rank coerces num to int', () {
      final suggestion = EmployeeSuggestion.fromJson({
        'employeeId': 1,
        'firstName': 'A',
        'lastName': 'B',
        'rank': 3.0,
      });

      expect(suggestion.rank, 3);
    });
  });
}
