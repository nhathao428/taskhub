import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user.dart';
import '../services/api_service.dart';

/// Đọc claim "role" từ payload JWT (nguồn chân lý, khớp backend). Trả [fallback]
/// nếu token sai định dạng hoặc thiếu claim (vd token cũ phát hành trước khi thêm).
String roleFromToken(String token, {String fallback = 'EMPLOYEE'}) {
  try {
    final parts = token.split('.');
    if (parts.length != 3) return fallback;
    final payload =
        utf8.decode(base64Url.decode(base64Url.normalize(parts[1])));
    final map = jsonDecode(payload) as Map<String, dynamic>;
    final role = (map['role'] as String?)?.toUpperCase();
    return (role == null || role.isEmpty) ? fallback : role;
  } catch (_) {
    return fallback;
  }
}

class AuthProvider extends ChangeNotifier {
  User? _user;
  bool _loading = false;
  String? _error;

  User? get user => _user;
  bool get loading => _loading;
  String? get error => _error;
  bool get isAuthenticated => _user != null;

  AuthProvider() {
    _restoreSession();
  }

  Future<void> _restoreSession() async {
    final token = await ApiService.getToken();
    if (token != null) {
      final prefs = await SharedPreferences.getInstance();
      final username = prefs.getString('username') ?? '';
      final email = prefs.getString('email') ?? '';
      // Ưu tiên role từ chính token; fallback prefs (token cũ) rồi EMPLOYEE.
      final role =
          roleFromToken(token, fallback: prefs.getString('role') ?? 'EMPLOYEE');
      _user = User(token: token, username: username, email: email, role: role);
      notifyListeners();
    }
  }

  Future<bool> login(String email, String password) async {
    _loading = true;
    _error = null;
    notifyListeners();
    try {
      final data = await ApiService.login(email, password);
      final user = User.fromJson(data);
      await ApiService.saveToken(user.token);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('username', user.username);
      await prefs.setString('email', user.email);
      await prefs.setString('role', user.role);
      _user = user;
      return true;
    } catch (e) {
      _error = e.toString();
      return false;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<bool> register(String username, String email, String password) async {
    _loading = true;
    _error = null;
    notifyListeners();
    try {
      await ApiService.register(username, email, password);
      return true;
    } catch (e) {
      _error = e.toString();
      return false;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    await ApiService.clearToken();
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('username');
    await prefs.remove('email');
    await prefs.remove('role');
    _user = null;
    notifyListeners();
  }
}
