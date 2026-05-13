class Attendance {
  final int attendanceId;
  final int? employeeId;
  final String? employeeName;
  final String date;
  final String checkIn;
  final String? checkOut;

  /// Trạng thái duyệt geofence: APPROVED / PENDING_REVIEW / REJECTED.
  final String? reviewStatus;

  /// Tên office gần nhất gắn vào check-in (nếu có).
  final String? checkInOfficeName;

  /// Khoảng cách (m) tính tại thời điểm check-in.
  final int? checkInDistanceMeters;

  Attendance({
    required this.attendanceId,
    this.employeeId,
    this.employeeName,
    required this.date,
    required this.checkIn,
    this.checkOut,
    this.reviewStatus,
    this.checkInOfficeName,
    this.checkInDistanceMeters,
  });

  factory Attendance.fromJson(Map<String, dynamic> json) {
    final employee = json['employee'] as Map<String, dynamic>?;
    final office = json['checkInOffice'] as Map<String, dynamic>?;
    return Attendance(
      attendanceId: json['attendanceId'] as int,
      employeeId: employee?['employeeId'] as int?,
      employeeName: employee != null
          ? '${employee['firstName']} ${employee['lastName']}'
          : null,
      date: json['date'] as String,
      checkIn: json['checkIn'] as String,
      checkOut: json['checkOut'] as String?,
      reviewStatus: json['reviewStatus'] as String?,
      checkInOfficeName: office?['name'] as String?,
      checkInDistanceMeters: (json['checkInDistanceMeters'] as num?)?.toInt(),
    );
  }
}
