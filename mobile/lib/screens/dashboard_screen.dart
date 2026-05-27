import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../providers/data_provider.dart';
import '../theme/app_theme.dart';
import '../widgets/aurora_background.dart';
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
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          border: Border(
            top: BorderSide(color: AppTheme.slate200, width: 1),
          ),
        ),
        child: BottomNavigationBar(
          currentIndex: _currentIndex,
          onTap: (i) => setState(() => _currentIndex = i),
          type: BottomNavigationBarType.fixed,
          backgroundColor: Colors.white,
          selectedItemColor: AppTheme.brand600,
          unselectedItemColor: AppTheme.slate400,
          elevation: 0,
          items: const [
            BottomNavigationBarItem(
              icon: Icon(Icons.dashboard_outlined),
              activeIcon: Icon(Icons.dashboard_rounded),
              label: 'Tổng quan',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.people_outline),
              activeIcon: Icon(Icons.people_rounded),
              label: 'Nhân viên',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.folder_outlined),
              activeIcon: Icon(Icons.folder_rounded),
              label: 'Dự án',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.checklist_outlined),
              activeIcon: Icon(Icons.checklist_rounded),
              label: 'Công việc',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.access_time_outlined),
              activeIcon: Icon(Icons.access_time_filled_rounded),
              label: 'Chấm công',
            ),
          ],
        ),
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

    final totalTasks = data.tasks.length;
    final doneTasks =
        data.tasks.where((t) => t.status == 'completed').length;
    final pct = totalTasks > 0 ? (doneTasks * 100 ~/ totalTasks) : 0;

    return Scaffold(
      backgroundColor: AppTheme.slate50,
      appBar: AppBar(
        title: const Text('Bảng điều khiển'),
        actions: [
          IconButton(
            icon: Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: AppTheme.brand50,
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.auto_awesome_rounded,
                  color: AppTheme.brand600, size: 18),
            ),
            tooltip: 'AI Gợi ý',
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(
                  builder: (_) => const AiSuggestionsScreen()),
            ),
          ),
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert_rounded),
            onSelected: (v) {
              if (v == 'logout') context.read<AuthProvider>().logout();
            },
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(14)),
            itemBuilder: (_) => [
              const PopupMenuItem(
                value: 'logout',
                child: Row(
                  children: [
                    Icon(Icons.logout_rounded, size: 18),
                    SizedBox(width: 10),
                    Text('Đăng xuất'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: isLoading
          ? const LoadingWidget(message: 'Đang tải dữ liệu...')
          : RefreshIndicator(
              color: AppTheme.brand600,
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
                  _HeroCard(
                    username: auth.user?.username ?? '',
                    totalTasks: totalTasks,
                    doneTasks: doneTasks,
                    pct: pct,
                  ),
                  const SizedBox(height: 16),
                  // Stats grid
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.45,
                    children: [
                      _StatCard(
                        icon: Icons.people_rounded,
                        color: AppTheme.brand600,
                        bgColor: AppTheme.brand50,
                        title: 'Nhân viên',
                        value: '${data.employees.length}',
                      ),
                      _StatCard(
                        icon: Icons.folder_rounded,
                        color: AppTheme.sky500,
                        bgColor: AppTheme.sky100,
                        title: 'Dự án',
                        value: '${data.projects.length}',
                      ),
                      _StatCard(
                        icon: Icons.checklist_rounded,
                        color: AppTheme.emerald500,
                        bgColor: AppTheme.emerald100,
                        title: 'Công việc',
                        value: '${data.tasks.length}',
                      ),
                      _StatCard(
                        icon: Icons.access_time_filled_rounded,
                        color: AppTheme.amber500,
                        bgColor: AppTheme.amber100,
                        title: 'Chấm công hôm nay',
                        value: '$attendanceToday',
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  _TaskBreakdownCard(tasks: data.tasks),
                ],
              ),
            ),
    );
  }
}

/// Hero gradient mesh dạng "aurora" — giống hero Dashboard.jsx web.
class _HeroCard extends StatelessWidget {
  final String username;
  final int totalTasks;
  final int doneTasks;
  final int pct;
  const _HeroCard({
    required this.username,
    required this.totalTasks,
    required this.doneTasks,
    required this.pct,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 168,
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(22),
        boxShadow: AppTheme.softShadowMd,
      ),
      child: Stack(
        children: [
          // Aurora background mạnh hơn so với fullscreen mode
          const Positioned.fill(
            child: AuroraBackground(
              fullScreen: true,
              child: SizedBox.shrink(),
            ),
          ),
          // Nội dung
          Padding(
            padding: const EdgeInsets.all(18),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Text(
                        'Tổng quan hệ thống',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 18,
                          fontWeight: FontWeight.w700,
                          letterSpacing: -0.3,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Xin chào, ${username.isEmpty ? 'bạn' : username}!',
                        style: TextStyle(
                          color: Colors.white.withValues(alpha: 0.85),
                          fontSize: 13,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '$totalTasks công việc · $doneTasks đã hoàn thành',
                        style: TextStyle(
                          color: Colors.white.withValues(alpha: 0.7),
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
                // Hoàn thành % pill
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.18),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: Colors.white.withValues(alpha: 0.3),
                    ),
                  ),
                  child: Column(
                    children: [
                      const Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.trending_up_rounded,
                              color: Colors.white, size: 16),
                          SizedBox(width: 6),
                          Text(
                            'HOÀN THÀNH',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              letterSpacing: 0.8,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '$pct%',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                          letterSpacing: -0.5,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StatCard extends StatelessWidget {
  final IconData icon;
  final Color color;
  final Color bgColor;
  final String title;
  final String value;

  const _StatCard({
    required this.icon,
    required this.color,
    required this.bgColor,
    required this.title,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppTheme.slate200),
        boxShadow: AppTheme.softShadow,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                title.toUpperCase(),
                style: const TextStyle(
                  fontSize: 10,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 0.6,
                  color: AppTheme.slate500,
                ),
              ),
              Container(
                width: 32,
                height: 32,
                decoration: BoxDecoration(
                  color: bgColor,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(icon, color: color, size: 18),
              ),
            ],
          ),
          Text(
            value,
            style: const TextStyle(
              fontSize: 28,
              fontWeight: FontWeight.bold,
              color: AppTheme.slate900,
              letterSpacing: -0.6,
            ),
          ),
        ],
      ),
    );
  }
}

class _TaskBreakdownCard extends StatelessWidget {
  final List tasks;
  const _TaskBreakdownCard({required this.tasks});

  @override
  Widget build(BuildContext context) {
    final pending = tasks.where((t) => t.status == 'pending').length;
    final progress = tasks.where((t) => t.status == 'in_progress').length;
    final done = tasks.where((t) => t.status == 'completed').length;

    return Container(
      padding: const EdgeInsets.all(18),
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
                child: const Icon(Icons.pie_chart_rounded,
                    color: AppTheme.brand600, size: 18),
              ),
              const SizedBox(width: 10),
              const Text(
                'Phân bố trạng thái công việc',
                style: TextStyle(
                  fontWeight: FontWeight.w700,
                  fontSize: 15,
                  color: AppTheme.slate900,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          _TaskStatusRow(
              label: 'Chờ xử lý',
              count: pending,
              color: AppTheme.amber500,
              bgColor: AppTheme.amber100),
          const SizedBox(height: 8),
          _TaskStatusRow(
              label: 'Đang thực hiện',
              count: progress,
              color: AppTheme.brand600,
              bgColor: AppTheme.brand50),
          const SizedBox(height: 8),
          _TaskStatusRow(
              label: 'Hoàn thành',
              count: done,
              color: AppTheme.emerald500,
              bgColor: AppTheme.emerald100),
        ],
      ),
    );
  }
}

class _TaskStatusRow extends StatelessWidget {
  final String label;
  final int count;
  final Color color;
  final Color bgColor;

  const _TaskStatusRow({
    required this.label,
    required this.count,
    required this.color,
    required this.bgColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: bgColor.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              label,
              style: const TextStyle(
                color: AppTheme.slate700,
                fontWeight: FontWeight.w600,
                fontSize: 13,
              ),
            ),
          ),
          Container(
            padding:
                const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(999),
            ),
            child: Text(
              '$count',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: color,
                fontSize: 13,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
