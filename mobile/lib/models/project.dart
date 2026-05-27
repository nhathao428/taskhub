class Project {
  final int projectId;
  final String name;
  final String? description;
  final String startDate;
  final String? endDate;
  final String status;

  Project({
    required this.projectId,
    required this.name,
    this.description,
    required this.startDate,
    this.endDate,
    required this.status,
  });

  factory Project.fromJson(Map<String, dynamic> json) {
    return Project(
      projectId: json['projectId'] as int,
      name: json['name'] as String,
      description: json['description'] as String?,
      startDate: json['startDate'] as String,
      endDate: json['endDate'] as String?,
      status: json['status'] as String? ?? 'ongoing',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
      'startDate': startDate,
      if (endDate != null) 'endDate': endDate,
      'status': status,
    };
  }
}
