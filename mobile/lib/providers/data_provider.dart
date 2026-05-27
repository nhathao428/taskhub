import 'package:flutter/material.dart';
import '../models/employee.dart';
import '../models/project.dart';
import '../models/task.dart';
import '../models/attendance.dart';
import '../models/employee_suggestion.dart';
import '../services/api_service.dart';

class DataProvider extends ChangeNotifier {
  List<Employee> _employees = [];
  List<Project> _projects = [];
  List<Task> _tasks = [];
  List<Attendance> _attendance = [];
  List<EmployeeSuggestion> _suggestions = [];

  bool _loadingEmployees = false;
  bool _loadingProjects = false;
  bool _loadingTasks = false;
  bool _loadingAttendance = false;
  bool _loadingSuggestions = false;

  String? _employeesError;
  String? _projectsError;
  String? _tasksError;
  String? _attendanceError;
  String? _suggestionsError;

  List<Employee> get employees => _employees;
  List<Project> get projects => _projects;
  List<Task> get tasks => _tasks;
  List<Attendance> get attendance => _attendance;
  List<EmployeeSuggestion> get suggestions => _suggestions;

  bool get loadingEmployees => _loadingEmployees;
  bool get loadingProjects => _loadingProjects;
  bool get loadingTasks => _loadingTasks;
  bool get loadingAttendance => _loadingAttendance;
  bool get loadingSuggestions => _loadingSuggestions;

  String? get employeesError => _employeesError;
  String? get projectsError => _projectsError;
  String? get tasksError => _tasksError;
  String? get attendanceError => _attendanceError;
  String? get suggestionsError => _suggestionsError;

  // ─── Employees ───────────────────────────────────────────────────────────────

  Future<void> fetchEmployees() async {
    _loadingEmployees = true;
    _employeesError = null;
    notifyListeners();
    try {
      _employees = await ApiService.getEmployees();
    } catch (e) {
      _employeesError = e.toString();
    } finally {
      _loadingEmployees = false;
      notifyListeners();
    }
  }

  Future<Employee?> addEmployee(Map<String, dynamic> body) async {
    try {
      final employee = await ApiService.createEmployee(body);
      _employees = [..._employees, employee];
      notifyListeners();
      return employee;
    } catch (e) {
      return null;
    }
  }

  // ─── Projects ────────────────────────────────────────────────────────────────

  Future<void> fetchProjects() async {
    _loadingProjects = true;
    _projectsError = null;
    notifyListeners();
    try {
      _projects = await ApiService.getProjects();
    } catch (e) {
      _projectsError = e.toString();
    } finally {
      _loadingProjects = false;
      notifyListeners();
    }
  }

  Future<Project?> addProject(Map<String, dynamic> body) async {
    try {
      final project = await ApiService.createProject(body);
      _projects = [..._projects, project];
      notifyListeners();
      return project;
    } catch (e) {
      return null;
    }
  }

  // ─── Tasks ───────────────────────────────────────────────────────────────────

  Future<void> fetchTasks() async {
    _loadingTasks = true;
    _tasksError = null;
    notifyListeners();
    try {
      _tasks = await ApiService.getTasks();
    } catch (e) {
      _tasksError = e.toString();
    } finally {
      _loadingTasks = false;
      notifyListeners();
    }
  }

  Future<Task?> addTask(Map<String, dynamic> body) async {
    try {
      final task = await ApiService.createTask(body);
      _tasks = [..._tasks, task];
      notifyListeners();
      return task;
    } catch (e) {
      return null;
    }
  }

  /// Self-service: update status of a task assigned to the current employee.
  Future<bool> updateMyTaskStatus(int taskId, String status) async {
    try {
      final updated = await ApiService.updateMyTaskStatus(taskId, status);
      _tasks = _tasks.map((t) => t.taskId == taskId ? updated : t).toList();
      notifyListeners();
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Manager-level full task update.
  Future<bool> updateTask(int taskId, Map<String, dynamic> body) async {
    try {
      final updated = await ApiService.updateTask(taskId, body);
      _tasks = _tasks.map((t) => t.taskId == taskId ? updated : t).toList();
      notifyListeners();
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Self-service: load tasks assigned to the current employee.
  Future<void> fetchMyTasks() async {
    _loadingTasks = true;
    _tasksError = null;
    notifyListeners();
    try {
      _tasks = await ApiService.getMyTasks();
    } catch (e) {
      _tasksError = e.toString();
    } finally {
      _loadingTasks = false;
      notifyListeners();
    }
  }

  // ─── Attendance ──────────────────────────────────────────────────────────────

  Future<void> fetchAttendance() async {
    _loadingAttendance = true;
    _attendanceError = null;
    notifyListeners();
    try {
      _attendance = await ApiService.getAttendance();
    } catch (e) {
      _attendanceError = e.toString();
    } finally {
      _loadingAttendance = false;
      notifyListeners();
    }
  }

  Future<Attendance?> checkIn(int employeeId) async {
    try {
      final record = await ApiService.checkIn(employeeId);
      _attendance = [..._attendance, record];
      notifyListeners();
      return record;
    } catch (e) {
      return null;
    }
  }

  Future<Attendance?> checkOut(int attendanceId) async {
    try {
      final updated = await ApiService.checkOut(attendanceId);
      _attendance = _attendance
          .map((a) => a.attendanceId == attendanceId ? updated : a)
          .toList();
      notifyListeners();
      return updated;
    } catch (e) {
      return null;
    }
  }

  /// Self-service: load attendance history for the current employee.
  Future<void> fetchMyAttendance() async {
    _loadingAttendance = true;
    _attendanceError = null;
    notifyListeners();
    try {
      _attendance = await ApiService.getMyAttendance();
    } catch (e) {
      _attendanceError = e.toString();
    } finally {
      _loadingAttendance = false;
      notifyListeners();
    }
  }

  /// Self check-in (employeeId resolved server-side from JWT).
  Future<Attendance?> checkInSelf() async {
    try {
      final record = await ApiService.checkInSelf();
      _attendance = [..._attendance, record];
      notifyListeners();
      return record;
    } catch (e) {
      _attendanceError = e.toString();
      notifyListeners();
      return null;
    }
  }

  /// Self check-out (closes today's open attendance).
  Future<Attendance?> checkOutSelf() async {
    try {
      final updated = await ApiService.checkOutSelf();
      _attendance = _attendance
          .map((a) => a.attendanceId == updated.attendanceId ? updated : a)
          .toList();
      notifyListeners();
      return updated;
    } catch (e) {
      _attendanceError = e.toString();
      notifyListeners();
      return null;
    }
  }

  // ─── AI Suggestions ──────────────────────────────────────────────────────────

  Future<void> fetchSuggestions({
    required String taskTitle,
    String? taskDescription,
  }) async {
    _loadingSuggestions = true;
    _suggestionsError = null;
    _suggestions = [];
    notifyListeners();
    try {
      _suggestions = await ApiService.recommendEmployees(
        taskTitle: taskTitle,
        taskDescription: taskDescription,
      );
    } catch (e) {
      _suggestionsError = e.toString();
    } finally {
      _loadingSuggestions = false;
      notifyListeners();
    }
  }

  void clearSuggestions() {
    _suggestions = [];
    _suggestionsError = null;
    notifyListeners();
  }
}
