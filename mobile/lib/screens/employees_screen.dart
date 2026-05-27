import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../widgets/empty_view.dart';
import '../providers/data_provider.dart';
import '../models/employee.dart';
import '../theme/app_theme.dart';
import '../widgets/loading_widget.dart';

class EmployeesScreen extends StatefulWidget {
  const EmployeesScreen({super.key});

  @override
  State<EmployeesScreen> createState() => _EmployeesScreenState();
}

class _EmployeesScreenState extends State<EmployeesScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (context.read<DataProvider>().employees.isEmpty) {
        context.read<DataProvider>().fetchEmployees();
      }
    });
  }

  void _showAddDialog() {
    final firstNameCtrl = TextEditingController();
    final lastNameCtrl = TextEditingController();
    final positionCtrl = TextEditingController();
    final departmentCtrl = TextEditingController();
    final formKey = GlobalKey<FormState>();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Thêm nhân viên'),
        content: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: firstNameCtrl,
                  decoration: const InputDecoration(labelText: 'Họ *'),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Bắt buộc' : null,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: lastNameCtrl,
                  decoration: const InputDecoration(labelText: 'Tên *'),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Bắt buộc' : null,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: positionCtrl,
                  decoration: const InputDecoration(labelText: 'Chức vụ'),
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: departmentCtrl,
                  decoration: const InputDecoration(labelText: 'Phòng ban'),
                ),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Huỷ'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (!formKey.currentState!.validate()) return;
              Navigator.pop(ctx);
              final result =
                  await context.read<DataProvider>().addEmployee({
                'firstName': firstNameCtrl.text.trim(),
                'lastName': lastNameCtrl.text.trim(),
                if (positionCtrl.text.trim().isNotEmpty)
                  'position': positionCtrl.text.trim(),
                if (departmentCtrl.text.trim().isNotEmpty)
                  'department': departmentCtrl.text.trim(),
              });
              if (!mounted) return;
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                content: Text(result != null
                    ? 'Đã thêm nhân viên ${result.fullName}'
                    : 'Thêm thất bại'),
                backgroundColor:
                    result != null ? AppTheme.emerald500 : AppTheme.rose500,
              ));
            },
            child: const Text('Thêm'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final data = context.watch<DataProvider>();
    return Scaffold(
      backgroundColor: AppTheme.slate50,
      appBar: AppBar(
        title: const Text('Nhân viên'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () => context.read<DataProvider>().fetchEmployees(),
          ),
          const SizedBox(width: 4),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _showAddDialog,
        icon: const Icon(Icons.add_rounded),
        label: const Text('Thêm'),
      ),
      body: data.loadingEmployees
          ? const LoadingWidget(message: 'Đang tải danh sách nhân viên...')
          : data.employeesError != null
              ? _ErrorView(
                  error: data.employeesError!,
                  onRetry: () =>
                      context.read<DataProvider>().fetchEmployees(),
                )
              : data.employees.isEmpty
                  ? const EmptyView('Chưa có nhân viên nào')
                  : RefreshIndicator(
                      color: AppTheme.brand600,
                      onRefresh: () =>
                          context.read<DataProvider>().fetchEmployees(),
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(12, 4, 12, 96),
                        itemCount: data.employees.length,
                        itemBuilder: (_, i) =>
                            _EmployeeCard(employee: data.employees[i]),
                      ),
                    ),
    );
  }
}

class _EmployeeCard extends StatelessWidget {
  final Employee employee;

  const _EmployeeCard({required this.employee});

  // Pseudo-random tint dựa trên tên để mỗi avatar có màu nhẹ khác nhau.
  Color _tintFor(String key) {
    final palette = [
      AppTheme.brand600,
      AppTheme.sky500,
      AppTheme.emerald500,
      AppTheme.amber500,
      AppTheme.fuchsia500,
    ];
    return palette[key.hashCode.abs() % palette.length];
  }

  Color _tintBgFor(Color c) {
    if (c == AppTheme.brand600) return AppTheme.brand50;
    if (c == AppTheme.sky500) return AppTheme.sky100;
    if (c == AppTheme.emerald500) return AppTheme.emerald100;
    if (c == AppTheme.amber500) return AppTheme.amber100;
    return AppTheme.brand50;
  }

  @override
  Widget build(BuildContext context) {
    final tint = _tintFor(employee.fullName);
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 5),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppTheme.slate200),
        boxShadow: AppTheme.softShadow,
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(16),
          onTap: () => _showDetail(context),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
            child: Row(
              children: [
                Container(
                  width: 46,
                  height: 46,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: _tintBgFor(tint),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Text(
                    employee.firstName.isNotEmpty
                        ? employee.firstName[0].toUpperCase()
                        : '?',
                    style: TextStyle(
                      color: tint,
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        employee.fullName,
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 14.5,
                          color: AppTheme.slate900,
                        ),
                      ),
                      if (employee.position != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 2),
                          child: Text(
                            employee.position!,
                            style: const TextStyle(
                              color: AppTheme.slate500,
                              fontSize: 12.5,
                            ),
                          ),
                        ),
                      if (employee.department != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 4),
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(
                              color: _tintBgFor(tint),
                              borderRadius: BorderRadius.circular(999),
                            ),
                            child: Text(
                              employee.department!,
                              style: TextStyle(
                                color: tint,
                                fontSize: 11,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
                const Icon(Icons.chevron_right_rounded,
                    color: AppTheme.slate400),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _showDetail(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => Padding(
        padding: const EdgeInsets.fromLTRB(24, 18, 24, 28),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppTheme.slate200,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 18),
            Text(employee.fullName,
                style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: AppTheme.slate900,
                    letterSpacing: -0.3)),
            const SizedBox(height: 14),
            if (employee.position != null)
              _DetailRow(
                  icon: Icons.work_outline_rounded, label: employee.position!),
            if (employee.department != null)
              _DetailRow(
                  icon: Icons.business_outlined,
                  label: employee.department!),
            if (employee.hiredAt != null)
              _DetailRow(
                  icon: Icons.calendar_today_outlined,
                  label: 'Ngày vào: ${employee.hiredAt!.substring(0, 10)}'),
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;

  const _DetailRow({required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        children: [
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: AppTheme.brand50,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, size: 16, color: AppTheme.brand600),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(label,
                style: const TextStyle(
                    color: AppTheme.slate700, fontSize: 13.5)),
          ),
        ],
      ),
    );
  }
}

class _ErrorView extends StatelessWidget {
  final String error;
  final VoidCallback onRetry;

  const _ErrorView({required this.error, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 56,
              height: 56,
              decoration: BoxDecoration(
                color: AppTheme.rose500.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(18),
              ),
              child: const Icon(Icons.error_outline_rounded,
                  color: AppTheme.rose500, size: 28),
            ),
            const SizedBox(height: 12),
            Text(error,
                textAlign: TextAlign.center,
                style: const TextStyle(color: AppTheme.slate700)),
            const SizedBox(height: 14),
            ElevatedButton.icon(
                onPressed: onRetry,
                icon: const Icon(Icons.refresh_rounded, size: 18),
                label: const Text('Thử lại')),
          ],
        ),
      ),
    );
  }
}
