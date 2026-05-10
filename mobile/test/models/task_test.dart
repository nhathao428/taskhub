import 'package:flutter_test/flutter_test.dart';
import 'package:task_management_system/models/task.dart';

void main() {
  group('Task', () {
    test('parses minimal JSON', () {
      final task = Task.fromJson({
        'taskId': 1,
        'title': 'Review code',
      });

      expect(task.taskId, 1);
      expect(task.title, 'Review code');
      expect(task.status, 'pending');
      expect(task.projectId, isNull);
      expect(task.assignedToId, isNull);
    });

    test('parses nested project and assignedTo', () {
      final task = Task.fromJson({
        'taskId': 5,
        'title': 'Build API',
        'description': 'REST endpoints',
        'dueDate': '2026-06-01',
        'status': 'in_progress',
        'project': {'projectId': 10, 'name': 'CRM'},
        'assignedTo': {
          'employeeId': 3,
          'firstName': 'Anh',
          'lastName': 'Tuấn',
        },
      });

      expect(task.projectId, 10);
      expect(task.projectName, 'CRM');
      expect(task.assignedToId, 3);
      expect(task.assignedToName, 'Anh Tuấn');
      expect(task.status, 'in_progress');
    });

    test('toJson omits null fields', () {
      final task = Task(taskId: 1, title: 'Test', status: 'pending');
      final json = task.toJson();

      expect(json['title'], 'Test');
      expect(json['status'], 'pending');
      expect(json.containsKey('description'), isFalse);
      expect(json.containsKey('projectId'), isFalse);
    });
  });
}
