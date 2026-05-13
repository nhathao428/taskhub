class OfficeLocation {
  final int id;
  final String name;
  final String? address;
  final double latitude;
  final double longitude;
  final int radiusMeters;
  final String status;

  OfficeLocation({
    required this.id,
    required this.name,
    this.address,
    required this.latitude,
    required this.longitude,
    required this.radiusMeters,
    required this.status,
  });

  factory OfficeLocation.fromJson(Map<String, dynamic> json) {
    return OfficeLocation(
      id: json['id'] as int,
      name: json['name'] as String,
      address: json['address'] as String?,
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      radiusMeters: (json['radiusMeters'] as num).toInt(),
      status: json['status'] as String? ?? 'ACTIVE',
    );
  }
}
