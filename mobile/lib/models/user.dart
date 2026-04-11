class User {
  final String token;
  final String username;
  final String email;

  User({required this.token, required this.username, required this.email});

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      token: json['token'] as String,
      username: json['username'] as String,
      email: json['email'] as String,
    );
  }
}
