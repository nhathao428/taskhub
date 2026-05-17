import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../widgets/empty_view.dart';
import '../providers/data_provider.dart';
import '../models/employee.dart';
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
                TextFormField(
                  controller: lastNameCtrl,
                  decoration: const InputDecoration(labelText: 'Tên *'),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Bắt buộc' : null,
                ),
                TextFormField(
                  controller: positionCtrl,
                  decoration: const InputDecoration(labelText: 'Chức vụ'),
                ),
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
                backgroundColor: result != null ? Colors.green : Colors.red,
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
      appBar: AppBar(
        title: const Text('Nhân viên'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => context.read<DataProvider>().fetchEmployees(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddDialog,
        child: const Icon(Icons.add),
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
                      onRefresh: () =>
                          context.read<DataProvider>().fetchEmployees(),
                      child: ListView.builder(
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

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Colors.blue.shade100,
          child: Text(
            employee.firstName.isNotEmpty ? employee.firstName[0] : '?',
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
        ),
        title: Text(employee.fullName,
            style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (employee.position != null)
              Text(employee.position!,
                  style: const TextStyle(color: Colors.grey)),
            if (employee.department != null)
              Text(
                employee.department!,
                style:
                    const TextStyle(color: Colors.blue, fontSize: 12),
              ),
          ],
        ),
        isThreeLine:
            employee.position != null && employee.department != null,
        trailing: const Icon(Icons.chevron_right),
        onTap: () => _showDetail(context),
      ),
    );
  }

  void _showDetail(BuildContext context) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (_) => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(employee.fullName,
                style: const TextStyle(
                    fontSize: 20, fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            if (employee.position != null)
              _DetailRow(
                  icon: Icons.work_outline, label: employee.position!),
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
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Icon(icon, size: 18, color: Colors.blue),
          const SizedBox(width: 8),
          Text(label),
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
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.error_outline, color: Colors.red, size: 48),
          const SizedBox(height: 8),
          Text(error, textAlign: TextAlign.center),
          const SizedBox(height: 12),
          ElevatedButton(onPressed: onRetry, child: const Text('Thử lại')),
        ],
      ),
    );
  }
}
