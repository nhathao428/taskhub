import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/data_provider.dart';
import '../models/task.dart';
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
                TextFormField(
                  controller: descCtrl,
                  decoration: const InputDecoration(labelText: 'Mô tả'),
                  maxLines: 3,
                ),
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
                backgroundColor: result != null ? Colors.green : Colors.red,
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

    final selected = await showDialog<String>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('Cập nhật trạng thái'),
        children: statuses
            .map(
              (s) => SimpleDialogOption(
                onPressed: () => Navigator.pop(ctx, s),
                child: Row(
                  children: [
                    if (task.status == s)
                      const Icon(Icons.check, color: Colors.blue, size: 18),
                    const SizedBox(width: 8),
                    Text(labels[s] ?? s),
                  ],
                ),
              ),
            )
            .toList(),
      ),
    );

    if (selected == null || selected == task.status) return;
    final success = await dataProvider.updateTask(task.taskId, {'status': selected});
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(
          success ? 'Đã cập nhật trạng thái' : 'Cập nhật thất bại'),
      backgroundColor: success ? Colors.green : Colors.red,
    ));
  }

  @override
  Widget build(BuildContext context) {
    final data = context.watch<DataProvider>();
    final tasks = _filteredTasks(data.tasks);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Công việc'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => context.read<DataProvider>().fetchTasks(),
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(48),
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            child: Row(
              children: [
                _FilterChip(
                    label: 'Tất cả',
                    selected: _filter == 'all',
                    onSelected: (_) => setState(() => _filter = 'all')),
                _FilterChip(
                    label: 'Chờ xử lý',
                    selected: _filter == 'pending',
                    onSelected: (_) => setState(() => _filter = 'pending')),
                _FilterChip(
                    label: 'Đang làm',
                    selected: _filter == 'in_progress',
                    onSelected: (_) =>
                        setState(() => _filter = 'in_progress')),
                _FilterChip(
                    label: 'Hoàn thành',
                    selected: _filter == 'completed',
                    onSelected: (_) =>
                        setState(() => _filter = 'completed')),
              ],
            ),
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddDialog,
        child: const Icon(Icons.add),
      ),
      body: data.loadingTasks
          ? const LoadingWidget(message: 'Đang tải danh sách công việc...')
          : data.tasksError != null
              ? _ErrorView(
                  error: data.tasksError!,
                  onRetry: () => context.read<DataProvider>().fetchTasks(),
                )
              : tasks.isEmpty
                  ? const Center(child: Text('Không có công việc nào'))
                  : RefreshIndicator(
                      onRefresh: () =>
                          context.read<DataProvider>().fetchTasks(),
                      child: ListView.builder(
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
  final bool selected;
  final ValueChanged<bool> onSelected;

  const _FilterChip(
      {required this.label,
      required this.selected,
      required this.onSelected});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 6),
      child: FilterChip(
        label: Text(label),
        selected: selected,
        onSelected: onSelected,
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
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(task.title,
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 15)),
                ),
                StatusBadge(status: task.status),
              ],
            ),
            if (task.description != null) ...[
              const SizedBox(height: 4),
              Text(task.description!,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Colors.grey, fontSize: 13)),
            ],
            const SizedBox(height: 8),
            Row(
              children: [
                if (task.projectName != null) ...[
                  const Icon(Icons.folder_outlined, size: 14, color: Colors.teal),
                  const SizedBox(width: 4),
                  Text(task.projectName!,
                      style: const TextStyle(fontSize: 12, color: Colors.teal)),
                  const SizedBox(width: 12),
                ],
                if (task.dueDate != null) ...[
                  const Icon(Icons.event_outlined, size: 14, color: Colors.grey),
                  const SizedBox(width: 4),
                  Text(task.dueDate!,
                      style: const TextStyle(fontSize: 12, color: Colors.grey)),
                ],
                const Spacer(),
                TextButton(
                  onPressed: onChangeStatus,
                  child: const Text('Đổi trạng thái',
                      style: TextStyle(fontSize: 12)),
                ),
              ],
            ),
          ],
        ),
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
