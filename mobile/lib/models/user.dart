class User {
  final String token;
  final String username;
  final String email;
  final String role;

  User({
    required this.token,
    required this.username,
    required this.email,
    this.role = 'EMPLOYEE',
  });

  /// MANAGER hoặc ADMIN — dùng để gate giao diện cấp quản lý (khớp backend).
  bool get isManagerOrAdmin => role == 'MANAGER' || role == 'ADMIN';

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      token: json['token'] as String,
      username: json['username'] as String,
      email: json['email'] as String,
      // Backend AuthResponse có trả 'role'; fallback EMPLOYEE nếu thiếu.
      role: (json['role'] as String?)?.toUpperCase() ?? 'EMPLOYEE',
    );
  }
}
