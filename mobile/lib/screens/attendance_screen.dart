import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';
import '../i18n/language_provider.dart';
import '../models/attendance.dart';
import '../models/office_location.dart';
import '../providers/data_provider.dart';
import '../services/api_service.dart';
import '../theme/app_theme.dart';

class AttendanceScreen extends StatefulWidget {
  /// mine=true: nhân viên chỉ xem lịch sử chấm công của chính mình
  /// (gọi /api/attendance/me). mine=false: quản lý xem toàn bộ.
  /// Check-in/out luôn là self-service (resolve employee từ JWT).
  final bool mine;

  const AttendanceScreen({super.key, this.mine = false});

  @override
  State<AttendanceScreen> createState() => _AttendanceScreenState();
}

class _AttendanceScreenState extends State<AttendanceScreen> {
  bool _busy = false;

  Future<void> _loadAttendance() => widget.mine
      ? context.read<DataProvider>().fetchMyAttendance()
      : context.read<DataProvider>().fetchAttendance();
  Position? _pos;
  bool _gpsLoading = false;
  String? _gpsError;
  List<OfficeLocation> _offices = [];
  final MapController _mapController = MapController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadAttendance();
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
    // Tra cứu tất cả message cần dùng trước khi await, vì context không thể
    // dùng qua async gap (use_build_context_synchronously).
    final msgService = context.trOnce('Vui lòng bật Dịch vụ Định vị (Location).');
    final msgDenied = context.trOnce('Bạn từ chối quyền truy cập vị trí.');
    final msgPermanent = context.trOnce('Quyền vị trí bị tắt vĩnh viễn. Hãy bật trong cài đặt.');
    setState(() {
      _gpsLoading = true;
      _gpsError = null;
    });
    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        throw msgService;
      }
      LocationPermission perm = await Geolocator.checkPermission();
      if (perm == LocationPermission.denied) {
        perm = await Geolocator.requestPermission();
      }
      if (perm == LocationPermission.denied) {
        throw msgDenied;
      }
      if (perm == LocationPermission.deniedForever) {
        throw msgPermanent;
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

  double _cos(double x) =>
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
      final d =
          _distance(_pos!.latitude, _pos!.longitude, o.latitude, o.longitude);
      if (d < bestDist) {
        bestDist = d;
        best = o;
      }
    }
    if (best == null) return null;
    return (
      office: best,
      distance: bestDist,
      within: bestDist <= best.radiusMeters
    );
  }

  Future<void> _doCheckInOut({required bool isCheckIn}) async {
    // Tra cứu translation trước await để tránh use_build_context_synchronously
    // ngay cả khi analyzer hiện chưa flag.
    final baseKey = isCheckIn ? 'Check-in' : 'Check-out';
    final msgSuccess = context.trOnce('$baseKey thành công');
    final msgPending = context.trOnce('$baseKey thành công – chờ quản lý duyệt');
    // Template "Lỗi: {error}" với placeholder marker để format lại sau catch.
    const errMarker = '__ERR__';
    final errorTemplate = context.trOnce('Lỗi: {error}', {'error': errMarker});
    final scaffold = ScaffoldMessenger.of(context);
    final dataProvider = context.read<DataProvider>();

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
      scaffold.showSnackBar(SnackBar(
        content: Text(pending ? msgPending : msgSuccess),
        backgroundColor:
            pending ? AppTheme.amber500 : AppTheme.emerald500,
      ));
      widget.mine ? dataProvider.fetchMyAttendance() : dataProvider.fetchAttendance();
    } catch (e) {
      if (!mounted) return;
      scaffold.showSnackBar(SnackBar(
        content: Text(errorTemplate.replaceFirst(errMarker, '$e')),
        backgroundColor: AppTheme.rose500,
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
    final todayList =
        data.attendance.where((a) => a.date == todayStr).toList()
          ..sort((a, b) => b.attendanceId.compareTo(a.attendanceId));
    final near = _nearest;

    return Scaffold(
      backgroundColor: AppTheme.slate50,
      appBar: AppBar(
        title: Text(context.tr('Chấm công')),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () {
              _loadAttendance();
              _loadOffices();
              _refreshPosition();
            },
          ),
          const SizedBox(width: 4),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(12, 4, 12, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // MAP
            Container(
              height: 240,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: AppTheme.slate200),
                boxShadow: AppTheme.softShadow,
              ),
              clipBehavior: Clip.antiAlias,
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
                    urlTemplate:
                        'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                    userAgentPackageName: 'com.example.taskmanagement',
                  ),
                  CircleLayer(
                    circles: _offices
                        .map((o) => CircleMarker(
                              point: LatLng(o.latitude, o.longitude),
                              radius: o.radiusMeters.toDouble(),
                              useRadiusInMeter: true,
                              color:
                                  AppTheme.brand500.withValues(alpha: 0.15),
                              borderColor: AppTheme.brand600,
                              borderStrokeWidth: 2,
                            ))
                        .toList(),
                  ),
                  MarkerLayer(
                    markers: [
                      ..._offices.map((o) => Marker(
                            point: LatLng(o.latitude, o.longitude),
                            width: 36,
                            height: 36,
                            child: const Icon(Icons.location_on_rounded,
                                color: AppTheme.brand600, size: 32),
                          )),
                      if (_pos != null)
                        Marker(
                          point: LatLng(_pos!.latitude, _pos!.longitude),
                          width: 40,
                          height: 40,
                          child: Container(
                            decoration: BoxDecoration(
                              color: AppTheme.rose500,
                              shape: BoxShape.circle,
                              border:
                                  Border.all(color: Colors.white, width: 3),
                              boxShadow: [
                                BoxShadow(
                                  color: AppTheme.rose500.withValues(alpha: 0.5),
                                  blurRadius: 14,
                                ),
                              ],
                            ),
                            child: const Icon(Icons.my_location_rounded,
                                color: Colors.white, size: 16),
                          ),
                        ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),

            // GPS status card
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: AppTheme.slate200),
                boxShadow: AppTheme.softShadow,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: AppTheme.brand50,
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: const Icon(Icons.my_location_rounded,
                            size: 16, color: AppTheme.brand600),
                      ),
                      const SizedBox(width: 10),
                      Text(context.tr('Vị trí GPS'),
                          style: const TextStyle(
                            fontWeight: FontWeight.w700,
                            color: AppTheme.slate900,
                            fontSize: 14.5,
                          )),
                      const Spacer(),
                      TextButton.icon(
                        onPressed: _gpsLoading ? null : _refreshPosition,
                        icon: const Icon(Icons.refresh_rounded, size: 16),
                        label: Text(context.tr('Cập nhật'),
                            style: const TextStyle(fontSize: 12.5)),
                      ),
                    ],
                  ),
                  if (_gpsLoading)
                    Padding(
                      padding: const EdgeInsets.only(top: 6, left: 42),
                      child: Text(context.tr('Đang lấy GPS…'),
                          style: const TextStyle(
                              color: AppTheme.slate500, fontSize: 12.5)),
                    ),
                  if (_gpsError != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 6, left: 42),
                      child: Text(_gpsError!,
                          style: const TextStyle(
                              color: AppTheme.rose500, fontSize: 12)),
                    ),
                  if (_pos != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 6, left: 42),
                      child: Text(
                        '${_pos!.latitude.toStringAsFixed(6)}, '
                        '${_pos!.longitude.toStringAsFixed(6)}'
                        '${_pos!.isMocked ? "  ⚠ mock detected" : ""}',
                        style: const TextStyle(
                            fontSize: 12,
                            fontFamily: 'monospace',
                            color: AppTheme.slate700),
                      ),
                    ),
                  if (near != null)
                    Container(
                      margin: const EdgeInsets.only(top: 12),
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: near.within
                            ? AppTheme.emerald100.withValues(alpha: 0.6)
                            : AppTheme.amber100.withValues(alpha: 0.6),
                        border: Border.all(
                            color: near.within
                                ? AppTheme.emerald500
                                : AppTheme.amber500),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          Icon(
                            near.within
                                ? Icons.check_circle_rounded
                                : Icons.warning_amber_rounded,
                            color: near.within
                                ? AppTheme.emerald500
                                : AppTheme.amber500,
                            size: 20,
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              near.within
                                  ? context.tr('{office} – {dist}m (trong vùng)', {
                                      'office': near.office.name,
                                      'dist': near.distance.round(),
                                    })
                                  : context.tr(
                                      '{office} – {dist}m / {max}m (ngoài vùng, sẽ chờ duyệt)',
                                      {
                                        'office': near.office.name,
                                        'dist': near.distance.round(),
                                        'max': near.office.radiusMeters,
                                      }),
                              style: TextStyle(
                                fontSize: 12.5,
                                fontWeight: FontWeight.w600,
                                color: near.within
                                    ? const Color(0xFF065F46)
                                    : const Color(0xFF92400E),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  if (_offices.isEmpty && !_gpsLoading)
                    Padding(
                      padding: const EdgeInsets.only(top: 8, left: 42),
                      child: Text(
                          context.tr('Chưa có văn phòng nào được cấu hình. Liên hệ quản lý.'),
                          style: const TextStyle(
                              color: AppTheme.slate500, fontSize: 12)),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 12),

            // Action buttons
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    icon: const Icon(Icons.login_rounded),
                    label: const Text('Check-in'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppTheme.emerald500,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14)),
                    ),
                    onPressed:
                        _busy ? null : () => _doCheckInOut(isCheckIn: true),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    icon: const Icon(Icons.logout_rounded),
                    label: const Text('Check-out'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppTheme.amber500,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14)),
                    ),
                    onPressed:
                        _busy ? null : () => _doCheckInOut(isCheckIn: false),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),

            _SectionHeader(
                icon: Icons.today_rounded,
                title: context.tr('Chấm công hôm nay'),
                count: todayList.length),
            const SizedBox(height: 8),
            if (todayList.isEmpty)
              _EmptyHint(text: context.tr('Chưa có bản ghi hôm nay'))
            else
              ...todayList.map((a) => _AttendanceCard(attendance: a)),

            const SizedBox(height: 20),
            _SectionHeader(
                icon: Icons.history_rounded,
                title: context.tr('Lịch sử chấm công'),
                count: data.attendance.length),
            const SizedBox(height: 8),
            if (data.attendance.isEmpty)
              _EmptyHint(text: context.tr('Không có dữ liệu chấm công'))
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

class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String title;
  final int count;
  const _SectionHeader(
      {required this.icon, required this.title, required this.count});
  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 18, color: AppTheme.slate700),
        const SizedBox(width: 8),
        Text(title,
            style: const TextStyle(
              fontWeight: FontWeight.w700,
              fontSize: 14,
              color: AppTheme.slate900,
            )),
        const SizedBox(width: 8),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 1),
          decoration: BoxDecoration(
            color: AppTheme.slate100,
            borderRadius: BorderRadius.circular(999),
          ),
          child: Text('$count',
              style: const TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w700,
                color: AppTheme.slate500,
              )),
        ),
      ],
    );
  }
}

class _EmptyHint extends StatelessWidget {
  final String text;
  const _EmptyHint({required this.text});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border:
            Border.all(color: AppTheme.slate200, style: BorderStyle.solid),
      ),
      child: Row(
        children: [
          const Icon(Icons.inbox_outlined,
              size: 18, color: AppTheme.slate400),
          const SizedBox(width: 8),
          Expanded(
            child: Text(text,
                style: const TextStyle(color: AppTheme.slate500, fontSize: 13)),
          ),
        ],
      ),
    );
  }
}

class _AttendanceCard extends StatelessWidget {
  final Attendance attendance;
  const _AttendanceCard({required this.attendance});

  @override
  Widget build(BuildContext context) {
    Color statusColor = AppTheme.emerald500;
    Color statusBg = AppTheme.emerald100;
    String statusLabel = context.trOnce('Đã duyệt');
    if (attendance.reviewStatus == 'PENDING_REVIEW') {
      statusColor = AppTheme.amber500;
      statusBg = AppTheme.amber100;
      statusLabel = context.trOnce('Chờ duyệt');
    } else if (attendance.reviewStatus == 'REJECTED') {
      statusColor = AppTheme.rose500;
      statusBg = AppTheme.rose500.withValues(alpha: 0.12);
      statusLabel = context.trOnce('Từ chối');
    }
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 4),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppTheme.slate200),
        boxShadow: AppTheme.softShadow,
      ),
      child: Row(
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: statusBg,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              attendance.checkOut != null
                  ? Icons.check_circle_rounded
                  : Icons.pending_rounded,
              color: statusColor,
              size: 22,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  attendance.employeeName ??
                      context.trOnce('Nhân viên #{id}', {'id': attendance.employeeId}),
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    fontSize: 13.5,
                    color: AppTheme.slate900,
                  ),
                ),
                const SizedBox(height: 2),
                Text(attendance.date,
                    style: const TextStyle(
                      color: AppTheme.slate500,
                      fontSize: 11.5,
                    )),
                if (attendance.checkInOfficeName != null ||
                    attendance.checkInDistanceMeters != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 2),
                    child: Text(
                      [
                        if (attendance.checkInOfficeName != null)
                          attendance.checkInOfficeName!,
                        if (attendance.checkInDistanceMeters != null)
                          '${attendance.checkInDistanceMeters}m',
                      ].join(' · '),
                      style: const TextStyle(
                          fontSize: 11, color: AppTheme.slate400),
                    ),
                  ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text('In  ${attendance.checkIn}',
                  style: const TextStyle(
                    color: AppTheme.emerald500,
                    fontSize: 11.5,
                    fontWeight: FontWeight.w700,
                  )),
              if (attendance.checkOut != null)
                Padding(
                  padding: const EdgeInsets.only(top: 2),
                  child: Text('Out  ${attendance.checkOut}',
                      style: const TextStyle(
                        color: AppTheme.amber500,
                        fontSize: 11.5,
                        fontWeight: FontWeight.w700,
                      )),
                ),
              const SizedBox(height: 4),
              Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 7, vertical: 2),
                decoration: BoxDecoration(
                  color: statusBg,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(statusLabel,
                    style: TextStyle(
                        fontSize: 10,
                        color: statusColor,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.2)),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
