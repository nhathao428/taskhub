import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/employee.dart';
import '../models/project.dart';
import '../models/task.dart';
import '../models/attendance.dart';
import '../models/employee_suggestion.dart';

class ApiService {
  static const String _baseUrl =
      String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080');
  static const String _tokenKey = 'jwt_token';

  // ─── Token management ───────────────────────────────────────────────────────

  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  static Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token);
  }

  static Future<void> clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
  }

  // ─── HTTP helpers ────────────────────────────────────────────────────────────

  static Future<Map<String, String>> _authHeaders() async {
    final token = await getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  static dynamic _unwrap(http.Response response) {
    final body = jsonDecode(utf8.decode(response.bodyBytes));
    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (body is Map<String, dynamic>) {
        return body['data'] ?? body;
      }
      return body;
    }
    final errorMsg = (body is Map<String, dynamic>)
        ? (body['error'] ?? body['message'] ?? 'Lỗi không xác định')
        : 'Lỗi ${response.statusCode}';
    throw ApiException(errorMsg.toString(), response.statusCode);
  }

  // ─── Auth ────────────────────────────────────────────────────────────────────

  static Future<Map<String, dynamic>> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );
    return _unwrap(response) as Map<String, dynamic>;
  }

  static Future<Map<String, dynamic>> register(
      String username, String email, String password) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/auth/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'username': username, 'email': email, 'password': password}),
    );
    return _unwrap(response) as Map<String, dynamic>;
  }

  // ─── Employees ───────────────────────────────────────────────────────────────

  static Future<List<Employee>> getEmployees() async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/employees'),
      headers: await _authHeaders(),
    );
    final data = _unwrap(response) as List<dynamic>;
    return data.map((e) => Employee.fromJson(e as Map<String, dynamic>)).toList();
  }

  static Future<Employee> createEmployee(Map<String, dynamic> body) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/employees'),
      headers: await _authHeaders(),
      body: jsonEncode(body),
    );
    return Employee.fromJson(_unwrap(response) as Map<String, dynamic>);
  }

  // ─── Projects ────────────────────────────────────────────────────────────────

  static Future<List<Project>> getProjects() async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/projects'),
      headers: await _authHeaders(),
    );
    final data = _unwrap(response) as List<dynamic>;
    return data.map((e) => Project.fromJson(e as Map<String, dynamic>)).toList();
  }

  static Future<Project> createProject(Map<String, dynamic> body) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/projects'),
      headers: await _authHeaders(),
      body: jsonEncode(body),
    );
    return Project.fromJson(_unwrap(response) as Map<String, dynamic>);
  }

  // ─── Tasks ───────────────────────────────────────────────────────────────────

  static Future<List<Task>> getTasks() async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/tasks'),
      headers: await _authHeaders(),
    );
    final data = _unwrap(response) as List<dynamic>;
    return data.map((e) => Task.fromJson(e as Map<String, dynamic>)).toList();
  }

  static Future<Task> createTask(Map<String, dynamic> body) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/tasks'),
      headers: await _authHeaders(),
      body: jsonEncode(body),
    );
    return Task.fromJson(_unwrap(response) as Map<String, dynamic>);
  }

  static Future<Task> updateTaskStatus(int taskId, String status) async {
    final response = await http.put(
      Uri.parse('$_baseUrl/api/tasks/$taskId'),
      headers: await _authHeaders(),
      body: jsonEncode({'status': status}),
    );
    return Task.fromJson(_unwrap(response) as Map<String, dynamic>);
  }

  // ─── Attendance ──────────────────────────────────────────────────────────────

  static Future<List<Attendance>> getAttendance() async {
    final response = await http.get(
      Uri.parse('$_baseUrl/api/attendance'),
      headers: await _authHeaders(),
    );
    final data = _unwrap(response) as List<dynamic>;
    return data.map((e) => Attendance.fromJson(e as Map<String, dynamic>)).toList();
  }

  static Future<Attendance> checkIn(int employeeId) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/attendance/checkin'),
      headers: await _authHeaders(),
      body: jsonEncode({'employeeId': employeeId}),
    );
    return Attendance.fromJson(_unwrap(response) as Map<String, dynamic>);
  }

  static Future<Attendance> checkOut(int attendanceId) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/attendance/checkout'),
      headers: await _authHeaders(),
      body: jsonEncode({'attendanceId': attendanceId}),
    );
    return Attendance.fromJson(_unwrap(response) as Map<String, dynamic>);
  }

  // ─── AI Suggestions ──────────────────────────────────────────────────────────

  static Future<List<EmployeeSuggestion>> recommendEmployees({
    required String taskTitle,
    String? taskDescription,
    List<String>? requiredSkills,
  }) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/api/suggestions/recommend'),
      headers: await _authHeaders(),
      body: jsonEncode({
        'taskTitle': taskTitle,
        if (taskDescription != null) 'taskDescription': taskDescription,
        'requiredSkills': requiredSkills ?? [],
      }),
    );
    final data = _unwrap(response) as List<dynamic>;
    return data
        .map((e) => EmployeeSuggestion.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}

class ApiException implements Exception {
  final String message;
  final int statusCode;

  ApiException(this.message, this.statusCode);

  @override
  String toString() => message;
}
