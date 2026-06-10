class EmployeeSuggestion {
  final int employeeId;
  final String firstName;
  final String lastName;
  final String? department;
  final int rank;
  final String? reasoning;

  EmployeeSuggestion({
    required this.employeeId,
    required this.firstName,
    required this.lastName,
    this.department,
    required this.rank,
    this.reasoning,
  });

  String get fullName => '$firstName $lastName'.trim();

  factory EmployeeSuggestion.fromJson(Map<String, dynamic> json) {
    return EmployeeSuggestion(
      employeeId: json['employeeId'] as int,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      department: json['department'] as String?,
      rank: (json['rank'] as num?)?.toInt() ?? 0,
      reasoning: json['reasoning'] as String?,
    );
  }
}
