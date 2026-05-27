import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../widgets/empty_view.dart';
import '../providers/data_provider.dart';
import '../models/task.dart';
import '../theme/app_theme.dart';
import '../widgets/loading_widget.dart';
import '../widgets/status_badge.dart';

class TasksScreen extends StatefulWidget {
  const TasksScreen({super.key});

  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

class _TasksScreenState extends State<TasksScreen> {
  String _filter = 'all';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (context.read<DataProvider>().tasks.isEmpty) {
        context.read<DataProvider>().fetchTasks();
      }
    });
  }

  List<Task> _filteredTasks(List<Task> tasks) {
    if (_filter == 'all') return tasks;
    return tasks.where((t) => t.status == _filter).toList();
  }

  void _showAddDialog() {
    final titleCtrl = TextEditingController();
    final descCtrl = TextEditingController();
    final dueDateCtrl = TextEditingController();
    final formKey = GlobalKey<FormState>();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Tạo công việc mới'),
        content: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: titleCtrl,
                  decoration: const InputDecoration(labelText: 'Tiêu đề *'),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Bắt buộc' : null,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: descCtrl,
                  decoration: const InputDecoration(labelText: 'Mô tả'),
                  maxLines: 3,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: dueDateCtrl,
                  decoration: const InputDecoration(
                      labelText: 'Hạn hoàn thành (YYYY-MM-DD)'),
                ),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx), child: const Text('Huỷ')),
          ElevatedButton(
            onPressed: () async {
              if (!formKey.currentState!.validate()) return;
              Navigator.pop(ctx);
              final result = await context.read<DataProvider>().addTask({
                'title': titleCtrl.text.trim(),
                if (descCtrl.text.trim().isNotEmpty)
                  'description': descCtrl.text.trim(),
                if (dueDateCtrl.text.trim().isNotEmpty)
                  'dueDate': dueDateCtrl.text.trim(),
                'status': 'pending',
              });
              if (!mounted) return;
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                content: Text(result != null
                    ? 'Đã tạo công việc "${result.title}"'
                    : 'Tạo công việc thất bại'),
                backgroundColor:
                    result != null ? AppTheme.emerald500 : AppTheme.rose500,
              ));
            },
            child: const Text('Tạo'),
          ),
        ],
      ),
    );
  }

  Future<void> _changeStatus(Task task) async {
    final dataProvider = context.read<DataProvider>();
    const statuses = ['pending', 'in_progress', 'completed'];
    final labels = {
      'pending': 'Chờ xử lý',
      'in_progress': 'Đang làm',
      'completed': 'Hoàn thành',
    };

    final selected = await showModalBottomSheet<String>(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 14, 20, 24),
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
            const SizedBox(height: 16),
            const Text('Cập nhật trạng thái',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                  color: AppTheme.slate900,
                )),
            const SizedBox(height: 12),
            ...statuses.map((s) {
              final isCurrent = task.status == s;
              return Container(
                margin: const EdgeInsets.only(bottom: 8),
                decoration: BoxDecoration(
                  color: isCurrent ? AppTheme.brand50 : Colors.white,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                      color:
                          isCurrent ? AppTheme.brand600 : AppTheme.slate200),
                ),
                child: ListTile(
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12)),
                  leading: Icon(
                    isCurrent
                        ? Icons.radio_button_checked
                        : Icons.radio_button_off,
                    color: isCurrent ? AppTheme.brand600 : AppTheme.slate400,
                  ),
                  title: Text(labels[s] ?? s,
                      style: TextStyle(
                        fontWeight: FontWeight.w600,
                        color: isCurrent
                            ? AppTheme.brand700
                            : AppTheme.slate900,
                      )),
                  onTap: () => Navigator.pop(ctx, s),
                ),
              );
            }),
          ],
        ),
      ),
    );

    if (selected == null || selected == task.status) return;
    final success =
        await dataProvider.updateTask(task.taskId, {'status': selected});
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(success ? 'Đã cập nhật trạng thái' : 'Cập nhật thất bại'),
      backgroundColor:
          success ? AppTheme.emerald500 : AppTheme.rose500,
    ));
  }

  @override
  Widget build(BuildContext context) {
    final data = context.watch<DataProvider>();
    final tasks = _filteredTasks(data.tasks);

    return Scaffold(
      backgroundColor: AppTheme.slate50,
      appBar: AppBar(
        title: const Text('Công việc'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () => context.read<DataProvider>().fetchTasks(),
          ),
          const SizedBox(width: 4),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(56),
          child: Container(
            color: AppTheme.slate50,
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              padding:
                  const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              child: Row(
                children: [
                  _FilterChip(
                      label: 'Tất cả',
                      count: data.tasks.length,
                      selected: _filter == 'all',
                      onTap: () => setState(() => _filter = 'all')),
                  _FilterChip(
                      label: 'Chờ xử lý',
                      count: data.tasks
                          .where((t) => t.status == 'pending')
                          .length,
                      selected: _filter == 'pending',
                      onTap: () => setState(() => _filter = 'pending')),
                  _FilterChip(
                      label: 'Đang làm',
                      count: data.tasks
                          .where((t) => t.status == 'in_progress')
                          .length,
                      selected: _filter == 'in_progress',
                      onTap: () => setState(() => _filter = 'in_progress')),
                  _FilterChip(
                      label: 'Hoàn thành',
                      count: data.tasks
                          .where((t) => t.status == 'completed')
                          .length,
                      selected: _filter == 'completed',
                      onTap: () => setState(() => _filter = 'completed')),
                ],
              ),
            ),
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _showAddDialog,
        icon: const Icon(Icons.add_rounded),
        label: const Text('Tạo'),
      ),
      body: data.loadingTasks
          ? const LoadingWidget(message: 'Đang tải danh sách công việc...')
          : data.tasksError != null
              ? _ErrorView(
                  error: data.tasksError!,
                  onRetry: () => context.read<DataProvider>().fetchTasks(),
                )
              : tasks.isEmpty
                  ? const EmptyView('Không có công việc nào')
                  : RefreshIndicator(
                      color: AppTheme.brand600,
                      onRefresh: () =>
                          context.read<DataProvider>().fetchTasks(),
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(12, 4, 12, 96),
                        itemCount: tasks.length,
                        itemBuilder: (_, i) => _TaskCard(
                          task: tasks[i],
                          onChangeStatus: () => _changeStatus(tasks[i]),
                        ),
                      ),
                    ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  final String label;
  final int count;
  final bool selected;
  final VoidCallback onTap;

  const _FilterChip({
    required this.label,
    required this.count,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(999),
          onTap: onTap,
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            padding:
                const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
            decoration: BoxDecoration(
              color: selected ? AppTheme.brand600 : Colors.white,
              borderRadius: BorderRadius.circular(999),
              border: Border.all(
                  color: selected ? AppTheme.brand600 : AppTheme.slate200),
              boxShadow: selected ? AppTheme.brandGlow : null,
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(label,
                    style: TextStyle(
                      color: selected ? Colors.white : AppTheme.slate700,
                      fontWeight: FontWeight.w700,
                      fontSize: 12.5,
                    )),
                const SizedBox(width: 6),
                Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 6, vertical: 1),
                  decoration: BoxDecoration(
                    color: selected
                        ? Colors.white.withValues(alpha: 0.25)
                        : AppTheme.slate100,
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text('$count',
                      style: TextStyle(
                        color:
                            selected ? Colors.white : AppTheme.slate500,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      )),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _TaskCard extends StatelessWidget {
  final Task task;
  final VoidCallback onChangeStatus;

  const _TaskCard({required this.task, required this.onChangeStatus});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 5),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppTheme.slate200),
        boxShadow: AppTheme.softShadow,
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Text(
                    task.title,
                    style: const TextStyle(
                      fontWeight: FontWeight.w700,
                      fontSize: 14.5,
                      color: AppTheme.slate900,
                      letterSpacing: -0.2,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                StatusBadge(status: task.status),
              ],
            ),
            if (task.description != null) ...[
              const SizedBox(height: 6),
              Text(
                task.description!,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  color: AppTheme.slate500,
                  fontSize: 12.5,
                  height: 1.4,
                ),
              ),
            ],
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 6,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                if (task.projectName != null)
                  _ChipPill(
                    icon: Icons.folder_rounded,
                    text: task.projectName!,
                    color: AppTheme.sky500,
                    bg: AppTheme.sky100,
                  ),
                if (task.dueDate != null)
                  _ChipPill(
                    icon: Icons.event_outlined,
                    text: task.dueDate!,
                    color: AppTheme.slate500,
                    bg: AppTheme.slate100,
                  ),
              ],
            ),
            const SizedBox(height: 10),
            Align(
              alignment: Alignment.centerRight,
              child: TextButton.icon(
                onPressed: onChangeStatus,
                icon: const Icon(Icons.sync_rounded, size: 16),
                label: const Text('Đổi trạng thái',
                    style: TextStyle(fontSize: 12.5)),
                style: TextButton.styleFrom(
                  foregroundColor: AppTheme.brand600,
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10, vertical: 6),
                  visualDensity: VisualDensity.compact,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ChipPill extends StatelessWidget {
  final IconData icon;
  final String text;
  final Color color;
  final Color bg;
  const _ChipPill({
    required this.icon,
    required this.text,
    required this.color,
    required this.bg,
  });
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 12, color: color),
          const SizedBox(width: 4),
          Text(text,
              style: TextStyle(
                fontSize: 11.5,
                fontWeight: FontWeight.w600,
                color: color,
              )),
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
