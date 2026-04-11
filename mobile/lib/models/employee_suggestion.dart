class EmployeeSuggestion {
  final int employeeId;
  final String firstName;
  final String lastName;
  final String? department;
  final double skillMatchScore;
  final double workloadScore;
  final double performanceScore;
  final double attendanceScore;
  final double overallScore;
  final String? reasoning;

  EmployeeSuggestion({
    required this.employeeId,
    required this.firstName,
    required this.lastName,
    this.department,
    required this.skillMatchScore,
    required this.workloadScore,
    required this.performanceScore,
    required this.attendanceScore,
    required this.overallScore,
    this.reasoning,
  });

  String get fullName => '$firstName $lastName';

  factory EmployeeSuggestion.fromJson(Map<String, dynamic> json) {
    return EmployeeSuggestion(
      employeeId: json['employeeId'] as int,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      department: json['department'] as String?,
      skillMatchScore: (json['skillMatchScore'] as num).toDouble(),
      workloadScore: (json['workloadScore'] as num).toDouble(),
      performanceScore: (json['performanceScore'] as num).toDouble(),
      attendanceScore: (json['attendanceScore'] as num).toDouble(),
      overallScore: (json['overallScore'] as num).toDouble(),
      reasoning: json['reasoning'] as String?,
    );
  }
}
