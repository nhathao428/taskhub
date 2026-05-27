class Task {
  final int taskId;
  final String title;
  final String? description;
  final String? dueDate;
  final String status;
  final int? projectId;
  final String? projectName;
  final int? assignedToId;
  final String? assignedToName;

  Task({
    required this.taskId,
    required this.title,
    this.description,
    this.dueDate,
    required this.status,
    this.projectId,
    this.projectName,
    this.assignedToId,
    this.assignedToName,
  });

  factory Task.fromJson(Map<String, dynamic> json) {
    final project = json['project'] as Map<String, dynamic>?;
    final assignedTo = json['assignedTo'] as Map<String, dynamic>?;
    return Task(
      taskId: json['taskId'] as int,
      title: json['title'] as String,
      description: json['description'] as String?,
      dueDate: json['dueDate'] as String?,
      status: json['status'] as String? ?? 'pending',
      projectId: project?['projectId'] as int?,
      projectName: project?['name'] as String?,
      assignedToId: assignedTo?['employeeId'] as int?,
      assignedToName: assignedTo != null
          ? '${assignedTo['firstName']} ${assignedTo['lastName']}'
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'title': title,
      if (description != null) 'description': description,
      if (dueDate != null) 'dueDate': dueDate,
      'status': status,
      if (projectId != null) 'projectId': projectId,
      if (assignedToId != null) 'assignedToId': assignedToId,
    };
  }
}
