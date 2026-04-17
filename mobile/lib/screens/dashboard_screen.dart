import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../providers/data_provider.dart';
import '../widgets/loading_widget.dart';
import 'employees_screen.dart';
import 'projects_screen.dart';
import 'tasks_screen.dart';
import 'attendance_screen.dart';
import 'ai_suggestions_screen.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  int _currentIndex = 0;

  final List<Widget> _screens = const [
    _DashboardHome(),
    EmployeesScreen(),
    ProjectsScreen(),
    TasksScreen(),
    AttendanceScreen(),
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _loadAll());
  }

  void _loadAll() {
    final data = context.read<DataProvider>();
    data.fetchEmployees();
    data.fetchProjects();
    data.fetchTasks();
    data.fetchAttendance();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(index: _currentIndex, children: _screens),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (i) => setState(() => _currentIndex = i),
        type: BottomNavigationBarType.fixed,
        selectedItemColor: Colors.blue,
        unselectedItemColor: Colors.grey,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.dashboard_outlined),
            activeIcon: Icon(Icons.dashboard),
            label: 'Tổng quan',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.people_outline),
            activeIcon: Icon(Icons.people),
            label: 'Nhân viên',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.folder_outlined),
            activeIcon: Icon(Icons.folder),
            label: 'Dự án',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.checklist_outlined),
            activeIcon: Icon(Icons.checklist),
            label: 'Công việc',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.access_time_outlined),
            activeIcon: Icon(Icons.access_time_filled),
            label: 'Chấm công',
          ),
        ],
      ),
    );
  }
}

class _DashboardHome extends StatelessWidget {
  const _DashboardHome();

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final data = context.watch<DataProvider>();

    final isLoading = data.loadingEmployees ||
        data.loadingProjects ||
        data.loadingTasks ||
        data.loadingAttendance;

    final today = DateTime.now();
    final todayStr =
        '${today.year}-${today.month.toString().padLeft(2, '0')}-${today.day.toString().padLeft(2, '0')}';
    final attendanceToday =
        data.attendance.where((a) => a.date == todayStr).length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Tổng quan'),
        actions: [
          IconButton(
            icon: const Icon(Icons.auto_awesome),
            tooltip: 'AI Gợi ý',
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(
                  builder: (_) => const AiSuggestionsScreen()),
            ),
          ),
          PopupMenuButton<String>(
            onSelected: (v) {
              if (v == 'logout') context.read<AuthProvider>().logout();
            },
            itemBuilder: (_) => [
              const PopupMenuItem(value: 'logout', child: Text('Đăng xuất')),
            ],
          ),
        ],
      ),
      body: isLoading
          ? const LoadingWidget(message: 'Đang tải dữ liệu...')
          : RefreshIndicator(
              onRefresh: () async {
                final d = context.read<DataProvider>();
                await Future.wait([
                  d.fetchEmployees(),
                  d.fetchProjects(),
                  d.fetchTasks(),
                  d.fetchAttendance(),
                ]);
              },
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  // Welcome card
                  Card(
                    color: Colors.blue,
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        children: [
                          const Icon(Icons.waving_hand, color: Colors.white, size: 32),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Xin chào, ${auth.user?.username ?? ''}!',
                                  style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 18,
                                      fontWeight: FontWeight.bold),
                                ),
                                const Text(
                                  'Chúc bạn một ngày làm việc hiệu quả',
                                  style: TextStyle(color: Colors.white70),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  // Stats grid
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.5,
                    children: [
                      _StatCard(
                        icon: Icons.people,
                        color: Colors.indigo,
                        title: 'Nhân viên',
                        value: '${data.employees.length}',
                      ),
                      _StatCard(
                        icon: Icons.folder,
                        color: Colors.teal,
                        title: 'Dự án',
                        value: '${data.projects.length}',
                      ),
                      _StatCard(
                        icon: Icons.checklist,
                        color: Colors.orange,
                        title: 'Công việc',
                        value: '${data.tasks.length}',
                      ),
                      _StatCard(
                        icon: Icons.access_time,
                        color: Colors.pink,
                        title: 'Chấm công hôm nay',
                        value: '$attendanceToday',
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  // Task status breakdown
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('Trạng thái công việc',
                              style: TextStyle(
                                  fontWeight: FontWeight.bold, fontSize: 16)),
                          const SizedBox(height: 12),
                          _TaskStatusRow(
                              label: 'Chờ xử lý',
                              count: data.tasks
                                  .where((t) => t.status == 'pending')
                                  .length,
                              color: Colors.blueGrey),
                          _TaskStatusRow(
                              label: 'Đang làm',
                              count: data.tasks
                                  .where((t) => t.status == 'in_progress')
                                  .length,
                              color: Colors.orange),
                          _TaskStatusRow(
                              label: 'Hoàn thành',
                              count: data.tasks
                                  .where((t) => t.status == 'completed')
                                  .length,
                              color: Colors.green),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}

class _StatCard extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String title;
  final String value;

  const _StatCard({
    required this.icon,
    required this.color,
    required this.title,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: color, size: 28),
            const SizedBox(height: 6),
            Text(
              value,
              style: TextStyle(
                  fontSize: 24, fontWeight: FontWeight.bold, color: color),
            ),
            Text(title,
                style: const TextStyle(fontSize: 12, color: Colors.grey),
                textAlign: TextAlign.center),
          ],
        ),
      ),
    );
  }
}

class _TaskStatusRow extends StatelessWidget {
  final String label;
  final int count;
  final Color color;

  const _TaskStatusRow(
      {required this.label, required this.count, required this.color});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Container(
              width: 12,
              height: 12,
              decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
          const SizedBox(width: 8),
          Expanded(child: Text(label)),
          Text('$count',
              style:
                  TextStyle(fontWeight: FontWeight.bold, color: color)),
        ],
      ),
    );
  }
}
