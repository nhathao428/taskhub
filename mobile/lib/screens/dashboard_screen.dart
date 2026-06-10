import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../i18n/language_provider.dart';
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

  /// Màn hình của quản lý/admin: toàn bộ nhân viên, dự án, công việc, chấm công.
  static const List<Widget> _managerScreens = [
    _DashboardHome(),
    EmployeesScreen(),
    ProjectsScreen(),
    TasksScreen(),
    AttendanceScreen(),
  ];

  /// Màn hình của nhân viên: chỉ dữ liệu của chính mình (self-service).
  static const List<Widget> _employeeScreens = [
    _DashboardHome(),
    ProjectsScreen(canManage: false),
    TasksScreen(mine: true),
    AttendanceScreen(mine: true),
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _loadAll());
  }

  void _loadAll() {
    final data = context.read<DataProvider>();
    final isManager =
        context.read<AuthProvider>().user?.isManagerOrAdmin ?? false;
    data.fetchProjects();
    if (isManager) {
      // Quản lý: toàn bộ nhân viên + công việc + chấm công.
      data.fetchEmployees();
      data.fetchTasks();
      data.fetchAttendance();
    } else {
      // Nhân viên: chỉ công việc được giao + chấm công của mình
      // (tránh gọi endpoint manager-only -> 403).
      data.fetchMyTasks();
      data.fetchMyAttendance();
    }
  }

  @override
  Widget build(BuildContext context) {
    final isManager =
        context.watch<AuthProvider>().user?.isManagerOrAdmin ?? false;
    final screens = isManager ? _managerScreens : _employeeScreens;
    // Đề phòng index lệch khi vai trò đổi (vd hot-reload demo).
    final index = _currentIndex.clamp(0, screens.length - 1);

    final items = <BottomNavigationBarItem>[
      BottomNavigationBarItem(
        icon: const Icon(Icons.dashboard_outlined),
        activeIcon: const Icon(Icons.dashboard_rounded),
        label: context.tr('Tổng quan'),
      ),
      if (isManager)
        BottomNavigationBarItem(
          icon: const Icon(Icons.people_outline),
          activeIcon: const Icon(Icons.people_rounded),
          label: context.tr('Nhân viên'),
        ),
      BottomNavigationBarItem(
        icon: const Icon(Icons.folder_outlined),
        activeIcon: const Icon(Icons.folder_rounded),
        label: context.tr('Dự án'),
      ),
      BottomNavigationBarItem(
        icon: const Icon(Icons.checklist_outlined),
        activeIcon: const Icon(Icons.checklist_rounded),
        label: context.tr(isManager ? 'Công việc' : 'Công việc của tôi'),
      ),
      BottomNavigationBarItem(
        icon: const Icon(Icons.access_time_outlined),
        activeIcon: const Icon(Icons.access_time_filled_rounded),
        label: context.tr('Chấm công'),
      ),
    ];

    return Scaffold(
      body: IndexedStack(index: index, children: screens),
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          border: Border(
            top: BorderSide(color: AppTheme.slate200, width: 1),
          ),
        ),
        child: BottomNavigationBar(
          currentIndex: index,
          onTap: (i) => setState(() => _currentIndex = i),
          type: BottomNavigationBarType.fixed,
          backgroundColor: Colors.white,
          selectedItemColor: AppTheme.brand600,
          unselectedItemColor: AppTheme.slate400,
          elevation: 0,
          items: items,
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
    final isManager = auth.user?.isManagerOrAdmin ?? false;

    final isLoading = isManager
        ? (data.loadingEmployees ||
            data.loadingProjects ||
            data.loadingTasks ||
            data.loadingAttendance)
        : (data.loadingTasks || data.loadingAttendance);

    final today = DateTime.now();
    final todayStr =
        '${today.year}-${today.month.toString().padLeft(2, '0')}-${today.day.toString().padLeft(2, '0')}';
    final monthPrefix = todayStr.substring(0, 7);
    final attendanceToday =
        data.attendance.where((a) => a.date == todayStr).length;
    final attendanceThisMonth =
        data.attendance.where((a) => a.date.startsWith(monthPrefix)).length;

    final totalTasks = data.tasks.length;
    final doneTasks =
        data.tasks.where((t) => t.status == 'completed').length;
    final pendingTasks =
        data.tasks.where((t) => t.status == 'pending').length;
    final inProgressTasks =
        data.tasks.where((t) => t.status == 'in_progress').length;
    final pct = totalTasks > 0 ? (doneTasks * 100 ~/ totalTasks) : 0;

    return Scaffold(
      backgroundColor: AppTheme.slate50,
      appBar: AppBar(
        title: Text(context.tr('Bảng điều khiển')),
        actions: [
          // Toggle ngôn ngữ vi ↔ en (Principle III: UX consistency)
          IconButton(
            tooltip: context.tr('Đổi ngôn ngữ'),
            icon: Text(
              context.watch<LanguageProvider>().isVietnamese ? 'EN' : 'VI',
              style: const TextStyle(
                color: AppTheme.brand600,
                fontWeight: FontWeight.w700,
                fontSize: 13,
              ),
            ),
            onPressed: () {
              final lp = context.read<LanguageProvider>();
              lp.setLocale(lp.isVietnamese ? 'en' : 'vi');
            },
          ),
          // AI gợi ý phân công là chức năng cấp quản lý (gọi /api/suggestions
          // -> MANAGER/ADMIN). Ẩn với nhân viên để tránh nút lỗi 403.
          if (isManager)
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
              tooltip: context.tr('AI Gợi ý'),
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
              PopupMenuItem(
                value: 'logout',
                child: Row(
                  children: [
                    const Icon(Icons.logout_rounded, size: 18),
                    const SizedBox(width: 10),
                    Text(context.trOnce('Đăng xuất')),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: isLoading
          ? LoadingWidget(message: context.tr('Đang tải dữ liệu...'))
          : RefreshIndicator(
              color: AppTheme.brand600,
              onRefresh: () async {
                final d = context.read<DataProvider>();
                await Future.wait([
                  d.fetchProjects(),
                  if (isManager) ...[
                    d.fetchEmployees(),
                    d.fetchTasks(),
                    d.fetchAttendance(),
                  ] else ...[
                    d.fetchMyTasks(),
                    d.fetchMyAttendance(),
                  ],
                ]);
              },
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  _HeroCard(
                    title: context.tr(
                        isManager ? 'Tổng quan hệ thống' : 'Công việc của tôi'),
                    username: auth.user?.username ?? '',
                    totalTasks: totalTasks,
                    doneTasks: doneTasks,
                    pct: pct,
                  ),
                  const SizedBox(height: 16),
                  // Stats grid — khác nhau giữa quản lý và nhân viên.
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.45,
                    children: isManager
                        ? [
                            _StatCard(
                              icon: Icons.people_rounded,
                              color: AppTheme.brand600,
                              bgColor: AppTheme.brand50,
                              title: context.tr('Nhân viên'),
                              value: '${data.employees.length}',
                            ),
                            _StatCard(
                              icon: Icons.folder_rounded,
                              color: AppTheme.sky500,
                              bgColor: AppTheme.sky100,
                              title: context.tr('Dự án'),
                              value: '${data.projects.length}',
                            ),
                            _StatCard(
                              icon: Icons.checklist_rounded,
                              color: AppTheme.emerald500,
                              bgColor: AppTheme.emerald100,
                              title: context.tr('Công việc'),
                              value: '${data.tasks.length}',
                            ),
                            _StatCard(
                              icon: Icons.access_time_filled_rounded,
                              color: AppTheme.amber500,
                              bgColor: AppTheme.amber100,
                              title: context.tr('Chấm công hôm nay'),
                              value: '$attendanceToday',
                            ),
                          ]
                        : [
                            _StatCard(
                              icon: Icons.hourglass_empty_rounded,
                              color: AppTheme.amber500,
                              bgColor: AppTheme.amber100,
                              title: context.tr('Chờ xử lý'),
                              value: '$pendingTasks',
                            ),
                            _StatCard(
                              icon: Icons.play_arrow_rounded,
                              color: AppTheme.brand600,
                              bgColor: AppTheme.brand50,
                              title: context.tr('Đang làm'),
                              value: '$inProgressTasks',
                            ),
                            _StatCard(
                              icon: Icons.task_alt_rounded,
                              color: AppTheme.emerald500,
                              bgColor: AppTheme.emerald100,
                              title: context.tr('Hoàn thành'),
                              value: '$doneTasks',
                            ),
                            _StatCard(
                              icon: Icons.event_available_rounded,
                              color: AppTheme.sky500,
                              bgColor: AppTheme.sky100,
                              title: context.tr('Chấm công tháng này'),
                              value: '$attendanceThisMonth',
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
  final String title;
  final String username;
  final int totalTasks;
  final int doneTasks;
  final int pct;
  const _HeroCard({
    required this.title,
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
                      Text(
                        title,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 18,
                          fontWeight: FontWeight.w700,
                          letterSpacing: -0.3,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        context.tr('Xin chào, {name}!',
                            {'name': username.isEmpty ? context.tr('bạn') : username}),
                        style: TextStyle(
                          color: Colors.white.withValues(alpha: 0.85),
                          fontSize: 13,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        context.tr('{total} công việc · {done} đã hoàn thành',
                            {'total': totalTasks, 'done': doneTasks}),
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
                      Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(Icons.trending_up_rounded,
                              color: Colors.white, size: 16),
                          const SizedBox(width: 6),
                          Text(
                            context.tr('HOÀN THÀNH'),
                            style: const TextStyle(
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
              Text(
                context.tr('Phân bố trạng thái công việc'),
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  fontSize: 15,
                  color: AppTheme.slate900,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          _TaskStatusRow(
              label: context.tr('Chờ xử lý'),
              count: pending,
              color: AppTheme.amber500,
              bgColor: AppTheme.amber100),
          const SizedBox(height: 8),
          _TaskStatusRow(
              label: context.tr('Đang thực hiện'),
              count: progress,
              color: AppTheme.brand600,
              bgColor: AppTheme.brand50),
          const SizedBox(height: 8),
          _TaskStatusRow(
              label: context.tr('Hoàn thành'),
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
