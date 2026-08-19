class Employee {
  final int employeeId;
  final String firstName;
  final String lastName;
  final String? position;
  final String? department;
  final String? hiredAt;

  Employee({
    required this.employeeId,
    required this.firstName,
    required this.lastName,
    this.position,
    this.department,
    this.hiredAt,
  });

  String get fullName => '$firstName $lastName'.trim();

  factory Employee.fromJson(Map<String, dynamic> json) {
    return Employee(
      employeeId: json['employeeId'] as int,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      position: json['position'] as String?,
      department: json['department'] as String?,
      hiredAt: json['hiredAt'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'firstName': firstName,
      'lastName': lastName,
      'position': position,
      'department': department,
    };
  }
}
