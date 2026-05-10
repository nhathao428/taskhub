import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/data_provider.dart';
import '../models/attendance.dart';
import '../widgets/loading_widget.dart';

class AttendanceScreen extends StatefulWidget {
  const AttendanceScreen({super.key});

  @override
  State<AttendanceScreen> createState() => _AttendanceScreenState();
}

class _AttendanceScreenState extends State<AttendanceScreen> {
  final _employeeIdCtrl = TextEditingController();
  bool _checkInLoading = false;
  bool _checkOutLoading = false;
  int? _lastAttendanceId;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (context.read<DataProvider>().attendance.isEmpty) {
        context.read<DataProvider>().fetchAttendance();
      }
    });
  }

  @override
  void dispose() {
    _employeeIdCtrl.dispose();
    super.dispose();
  }

  Future<void> _checkIn() async {
    final empIdText = _employeeIdCtrl.text.trim();
    if (empIdText.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng nhập mã nhân viên')),
      );
      return;
    }
    final empId = int.tryParse(empIdText);
    if (empId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Mã nhân viên phải là số')),
      );
      return;
    }
    setState(() => _checkInLoading = true);
    final record = await context.read<DataProvider>().checkIn(empId);
    if (!mounted) return;
    setState(() {
      _checkInLoading = false;
      if (record != null) _lastAttendanceId = record.attendanceId;
    });
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(record != null
          ? 'Check-in thành công lúc ${record.checkIn}'
          : 'Check-in thất bại'),
      backgroundColor: record != null ? Colors.green : Colors.red,
    ));
  }

  Future<void> _checkOut() async {
    final dataProvider = context.read<DataProvider>();
    if (_lastAttendanceId == null) {
      final attendanceIdText = await showDialog<String>(
        context: context,
        builder: (ctx) {
          final ctrl = TextEditingController();
          return AlertDialog(
            title: const Text('Nhập mã chấm công'),
            content: TextField(
              controller: ctrl,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(labelText: 'Attendance ID'),
            ),
            actions: [
              TextButton(
                  onPressed: () => Navigator.pop(ctx), child: const Text('Huỷ')),
              ElevatedButton(
                onPressed: () => Navigator.pop(ctx, ctrl.text),
                child: const Text('Check-out'),
              ),
            ],
          );
        },
      );
      if (attendanceIdText == null) return;
      _lastAttendanceId = int.tryParse(attendanceIdText.trim());
    }
    if (_lastAttendanceId == null) return;

    setState(() => _checkOutLoading = true);
    final record = await dataProvider.checkOut(_lastAttendanceId!);
    if (!mounted) return;
    setState(() {
      _checkOutLoading = false;
      if (record?.checkOut != null) _lastAttendanceId = null;
    });
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(record != null
          ? 'Check-out thành công lúc ${record.checkOut}'
          : 'Check-out thất bại'),
      backgroundColor: record != null ? Colors.green : Colors.red,
    ));
  }

  @override
  Widget build(BuildContext context) {
    final data = context.watch<DataProvider>();
    final today = DateTime.now();
    final todayStr =
        '${today.year}-${today.month.toString().padLeft(2, '0')}-${today.day.toString().padLeft(2, '0')}';
    final todayList = data.attendance
        .where((a) => a.date == todayStr)
        .toList()
      ..sort((a, b) => b.attendanceId.compareTo(a.attendanceId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Chấm công'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => context.read<DataProvider>().fetchAttendance(),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Check-in / Check-out panel
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Text('Mã nhân viên',
                        style: TextStyle(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    TextField(
                      controller: _employeeIdCtrl,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        hintText: 'Nhập mã nhân viên',
                        border: OutlineInputBorder(),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton.icon(
                            icon: _checkInLoading
                                ? const SizedBox(
                                    width: 16,
                                    height: 16,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2, color: Colors.white))
                                : const Icon(Icons.login),
                            label: const Text('Check-in'),
                            style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.green),
                            onPressed:
                                (_checkInLoading || _checkOutLoading)
                                    ? null
                                    : _checkIn,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: ElevatedButton.icon(
                            icon: _checkOutLoading
                                ? const SizedBox(
                                    width: 16,
                                    height: 16,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2, color: Colors.white))
                                : const Icon(Icons.logout),
                            label: const Text('Check-out'),
                            style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.orange),
                            onPressed:
                                (_checkInLoading || _checkOutLoading)
                                    ? null
                                    : _checkOut,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            // Today's attendance
            const Text('Chấm công hôm nay',
                style:
                    TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 8),
            if (data.loadingAttendance)
              const LoadingWidget(message: 'Đang tải...')
            else if (todayList.isEmpty)
              const Padding(
                padding: EdgeInsets.all(16),
                child: Text('Chưa có chấm công hôm nay',
                    style: TextStyle(color: Colors.grey)),
              )
            else
              ...todayList.map((a) => _AttendanceCard(attendance: a)),
            const SizedBox(height: 16),
            // Full history
            const Text('Lịch sử chấm công',
                style:
                    TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 8),
            if (data.loadingAttendance)
              const LoadingWidget(message: 'Đang tải...')
            else if (data.attendance.isEmpty)
              const Padding(
                padding: EdgeInsets.all(16),
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
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 4),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: attendance.checkOut != null
              ? Colors.green.shade100
              : Colors.orange.shade100,
          child: Icon(
            attendance.checkOut != null ? Icons.check_circle : Icons.pending,
            color: attendance.checkOut != null ? Colors.green : Colors.orange,
          ),
        ),
        title: Text(
          attendance.employeeName ?? 'Nhân viên #${attendance.employeeId}',
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        subtitle: Text(attendance.date),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text('In: ${attendance.checkIn}',
                style: const TextStyle(color: Colors.green, fontSize: 12)),
            if (attendance.checkOut != null)
              Text('Out: ${attendance.checkOut}',
                  style: const TextStyle(color: Colors.orange, fontSize: 12))
            else
              const Text('Chưa check-out',
                  style: TextStyle(color: Colors.grey, fontSize: 11)),
          ],
        ),
        isThreeLine: false,
      ),
    );
  }
}
