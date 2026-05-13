import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';
import '../models/attendance.dart';
import '../models/office_location.dart';
import '../providers/data_provider.dart';
import '../services/api_service.dart';

class AttendanceScreen extends StatefulWidget {
  const AttendanceScreen({super.key});

  @override
  State<AttendanceScreen> createState() => _AttendanceScreenState();
}

class _AttendanceScreenState extends State<AttendanceScreen> {
  bool _busy = false;
  Position? _pos;
  bool _gpsLoading = false;
  String? _gpsError;
  List<OfficeLocation> _offices = [];
  final MapController _mapController = MapController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DataProvider>().fetchAttendance();
      _loadOffices();
      _refreshPosition();
    });
  }

  Future<void> _loadOffices() async {
    try {
      final list = await ApiService.getOfficeLocations(activeOnly: true);
      if (!mounted) return;
      setState(() => _offices = list);
    } catch (_) {/* ignore */}
  }

  Future<void> _refreshPosition() async {
    setState(() {
      _gpsLoading = true;
      _gpsError = null;
    });
    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        throw 'Vui lòng bật Dịch vụ Định vị (Location).';
      }
      LocationPermission perm = await Geolocator.checkPermission();
      if (perm == LocationPermission.denied) {
        perm = await Geolocator.requestPermission();
      }
      if (perm == LocationPermission.denied) {
        throw 'Bạn từ chối quyền truy cập vị trí.';
      }
      if (perm == LocationPermission.deniedForever) {
        throw 'Quyền vị trí bị tắt vĩnh viễn. Hãy bật trong cài đặt.';
      }
      final p = await Geolocator.getCurrentPosition(
        locationSettings:
            const LocationSettings(accuracy: LocationAccuracy.high),
      );
      if (!mounted) return;
      setState(() {
        _pos = p;
        _gpsLoading = false;
      });
      _mapController.move(LatLng(p.latitude, p.longitude), 16);
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _gpsError = e.toString();
        _gpsLoading = false;
      });
    }
  }

  /// Khoảng cách Haversine (mét) giữa 2 toạ độ.
  double _distance(double lat1, double lng1, double lat2, double lng2) {
    const r = 6371000.0;
    final dLat = (lat2 - lat1) * 3.141592653589793 / 180;
    final dLng = (lng2 - lng1) * 3.141592653589793 / 180;
    final a = (1 - _cos(dLat)) / 2 +
        _cos(lat1 * 3.141592653589793 / 180) *
            _cos(lat2 * 3.141592653589793 / 180) *
            (1 - _cos(dLng)) /
            2;
    return 2 * r * _asin(_sqrt(a));
  }

  double _cos(double x) => _polyCos(x);
  double _polyCos(double x) =>
      1 - x * x / 2 + x * x * x * x / 24 - x * x * x * x * x * x / 720;
  double _sqrt(double x) => x <= 0 ? 0 : _sqrtIter(x, x / 2);
  double _sqrtIter(double x, double g) {
    for (var i = 0; i < 12; i++) {
      g = (g + x / g) / 2;
    }
    return g;
  }
  double _asin(double x) =>
      x + x * x * x / 6 + 3 * x * x * x * x * x / 40;

  ({OfficeLocation office, double distance, bool within})? get _nearest {
    if (_pos == null || _offices.isEmpty) return null;
    OfficeLocation? best;
    double bestDist = double.infinity;
    for (final o in _offices) {
      final d = _distance(_pos!.latitude, _pos!.longitude, o.latitude, o.longitude);
      if (d < bestDist) {
        bestDist = d;
        best = o;
      }
    }
    if (best == null) return null;
    return (office: best, distance: bestDist, within: bestDist <= best.radiusMeters);
  }

  Future<void> _doCheckInOut({required bool isCheckIn}) async {
    setState(() => _busy = true);
    try {
      Attendance? rec;
      final lat = _pos?.latitude;
      final lng = _pos?.longitude;
      final mocked = _pos?.isMocked == true;
      if (isCheckIn) {
        rec = await ApiService.checkInSelf(
          latitude: lat, longitude: lng, isMocked: mocked,
        );
      } else {
        rec = await ApiService.checkOutSelf(
          latitude: lat, longitude: lng, isMocked: mocked,
        );
      }
      if (!mounted) return;
      final pending = rec.reviewStatus == 'PENDING_REVIEW';
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(pending
            ? '${isCheckIn ? "Check-in" : "Check-out"} thành công – chờ quản lý duyệt'
            : '${isCheckIn ? "Check-in" : "Check-out"} thành công'),
        backgroundColor: pending ? Colors.orange : Colors.green,
      ));
      context.read<DataProvider>().fetchAttendance();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text('Lỗi: $e'),
        backgroundColor: Colors.red,
      ));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final data = context.watch<DataProvider>();
    final today = DateTime.now();
    final todayStr =
        '${today.year}-${today.month.toString().padLeft(2, '0')}-${today.day.toString().padLeft(2, '0')}';
    final todayList = data.attendance.where((a) => a.date == todayStr).toList()
      ..sort((a, b) => b.attendanceId.compareTo(a.attendanceId));
    final near = _nearest;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Chấm công'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              context.read<DataProvider>().fetchAttendance();
              _loadOffices();
              _refreshPosition();
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // MAP
            SizedBox(
              height: 280,
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: FlutterMap(
                  mapController: _mapController,
                  options: MapOptions(
                    initialCenter: _pos != null
                        ? LatLng(_pos!.latitude, _pos!.longitude)
                        : _offices.isNotEmpty
                            ? LatLng(_offices[0].latitude, _offices[0].longitude)
                            : const LatLng(10.7769, 106.7009),
                    initialZoom: 15,
                  ),
                  children: [
                    TileLayer(
                      urlTemplate: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                      userAgentPackageName: 'com.example.taskmanagement',
                    ),
                    CircleLayer(
                      circles: _offices
                          .map((o) => CircleMarker(
                                point: LatLng(o.latitude, o.longitude),
                                radius: o.radiusMeters.toDouble(),
                                useRadiusInMeter: true,
                                color: Colors.blue.withOpacity(0.18),
                                borderColor: Colors.blue,
                                borderStrokeWidth: 2,
                              ))
                          .toList(),
                    ),
                    MarkerLayer(
                      markers: [
                        ..._offices.map((o) => Marker(
                              point: LatLng(o.latitude, o.longitude),
                              width: 40,
                              height: 40,
                              child: const Icon(Icons.location_on,
                                  color: Colors.blue, size: 36),
                            )),
                        if (_pos != null)
                          Marker(
                            point: LatLng(_pos!.latitude, _pos!.longitude),
                            width: 40,
                            height: 40,
                            child: const Icon(Icons.my_location,
                                color: Colors.red, size: 32),
                          ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),

            // GPS status card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Text('Vị trí GPS',
                            style: TextStyle(fontWeight: FontWeight.bold)),
                        const Spacer(),
                        TextButton.icon(
                          onPressed: _gpsLoading ? null : _refreshPosition,
                          icon: const Icon(Icons.my_location, size: 18),
                          label: const Text('Cập nhật'),
                        ),
                      ],
                    ),
                    if (_gpsLoading)
                      const Text('Đang lấy GPS…',
                          style: TextStyle(color: Colors.grey, fontSize: 12)),
                    if (_gpsError != null)
                      Text(_gpsError!,
                          style: const TextStyle(
                              color: Colors.red, fontSize: 12)),
                    if (_pos != null)
                      Text(
                        '${_pos!.latitude.toStringAsFixed(6)}, '
                        '${_pos!.longitude.toStringAsFixed(6)}'
                        '${_pos!.isMocked ? "  ⚠ mock detected" : ""}',
                        style: const TextStyle(fontSize: 12),
                      ),
                    if (near != null)
                      Container(
                        margin: const EdgeInsets.only(top: 8),
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          color: near.within
                              ? Colors.green.shade50
                              : Colors.orange.shade50,
                          border: Border.all(
                              color: near.within
                                  ? Colors.green
                                  : Colors.orange),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          near.within
                              ? '✓ ${near.office.name} – '
                                  '${near.distance.round()}m '
                                  '(trong vùng)'
                              : '⚠ ${near.office.name} – '
                                  '${near.distance.round()}m / '
                                  '${near.office.radiusMeters}m '
                                  '(ngoài vùng, sẽ chờ duyệt)',
                          style: TextStyle(
                              fontSize: 12,
                              color: near.within
                                  ? Colors.green.shade800
                                  : Colors.orange.shade800),
                        ),
                      ),
                    if (_offices.isEmpty && !_gpsLoading)
                      const Padding(
                        padding: EdgeInsets.only(top: 8),
                        child: Text(
                            'Chưa có văn phòng nào được cấu hình. Liên hệ quản lý.',
                            style: TextStyle(color: Colors.grey, fontSize: 12)),
                      ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Action buttons
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    icon: const Icon(Icons.login),
                    label: const Text('Check-in'),
                    style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.green,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14)),
                    onPressed: _busy ? null : () => _doCheckInOut(isCheckIn: true),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    icon: const Icon(Icons.logout),
                    label: const Text('Check-out'),
                    style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.orange,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14)),
                    onPressed: _busy ? null : () => _doCheckInOut(isCheckIn: false),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            const Text('Chấm công hôm nay',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 8),
            if (todayList.isEmpty)
              const Padding(
                padding: EdgeInsets.all(8),
                child: Text('Chưa có bản ghi hôm nay',
                    style: TextStyle(color: Colors.grey)),
              )
            else
              ...todayList.map((a) => _AttendanceCard(attendance: a)),

            const SizedBox(height: 16),
            const Text('Lịch sử chấm công',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 8),
            if (data.attendance.isEmpty)
              const Padding(
                padding: EdgeInsets.all(8),
                child: Text('Chưa có dữ liệu chấm công',
                    style: TextStyle(color: Colors.grey)),
              )
            else
              ...data.attendance.reversed
                  .take(20)
                  .map((a) => _AttendanceCard(attendance: a)),
          ],
        ),
      ),
    );
  }
}

class _AttendanceCard extends StatelessWidget {
  final Attendance attendance;
  const _AttendanceCard({required this.attendance});

  @override
  Widget build(BuildContext context) {
    Color statusColor = Colors.green;
    String statusLabel = 'Đã duyệt';
    if (attendance.reviewStatus == 'PENDING_REVIEW') {
      statusColor = Colors.orange;
      statusLabel = 'Chờ duyệt';
    } else if (attendance.reviewStatus == 'REJECTED') {
      statusColor = Colors.red;
      statusLabel = 'Từ chối';
    }
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 4),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: statusColor.withOpacity(0.15),
          child: Icon(
            attendance.checkOut != null ? Icons.check_circle : Icons.pending,
            color: statusColor,
          ),
        ),
        title: Text(
          attendance.employeeName ?? 'Nhân viên #${attendance.employeeId}',
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(attendance.date),
            if (attendance.checkInOfficeName != null ||
                attendance.checkInDistanceMeters != null)
              Text(
                [
                  if (attendance.checkInOfficeName != null)
                    attendance.checkInOfficeName!,
                  if (attendance.checkInDistanceMeters != null)
                    '${attendance.checkInDistanceMeters}m',
                ].join(' – '),
                style: const TextStyle(fontSize: 11, color: Colors.grey),
              ),
          ],
        ),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text('In: ${attendance.checkIn}',
                style: const TextStyle(color: Colors.green, fontSize: 12)),
            if (attendance.checkOut != null)
              Text('Out: ${attendance.checkOut}',
                  style: const TextStyle(color: Colors.orange, fontSize: 12)),
            const SizedBox(height: 2),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              decoration: BoxDecoration(
                color: statusColor.withOpacity(0.12),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(statusLabel,
                  style: TextStyle(
                      fontSize: 10,
                      color: statusColor,
                      fontWeight: FontWeight.bold)),
            ),
          ],
        ),
      ),
    );
  }
}
