import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../widgets/empty_view.dart';
import '../providers/data_provider.dart';
import '../models/project.dart';
import '../widgets/loading_widget.dart';
import '../widgets/status_badge.dart';

class ProjectsScreen extends StatefulWidget {
  const ProjectsScreen({super.key});

  @override
  State<ProjectsScreen> createState() => _ProjectsScreenState();
}

class _ProjectsScreenState extends State<ProjectsScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (context.read<DataProvider>().projects.isEmpty) {
        context.read<DataProvider>().fetchProjects();
      }
    });
  }

  void _showAddDialog() {
    final nameCtrl = TextEditingController();
    final descCtrl = TextEditingController();
    final startCtrl = TextEditingController(
      text: DateTime.now().toIso8601String().substring(0, 10),
    );
    final formKey = GlobalKey<FormState>();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Thêm dự án'),
        content: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: nameCtrl,
                  decoration: const InputDecoration(labelText: 'Tên dự án *'),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Bắt buộc' : null,
                ),
                TextFormField(
                  controller: descCtrl,
                  decoration: const InputDecoration(labelText: 'Mô tả'),
                  maxLines: 3,
                ),
                TextFormField(
                  controller: startCtrl,
                  decoration:
                      const InputDecoration(labelText: 'Ngày bắt đầu (YYYY-MM-DD) *'),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Bắt buộc' : null,
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
                  await context.read<DataProvider>().addProject({
                'name': nameCtrl.text.trim(),
                if (descCtrl.text.trim().isNotEmpty)
                  'description': descCtrl.text.trim(),
                'startDate': startCtrl.text.trim(),
                'status': 'ongoing',
              });
              if (!mounted) return;
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                content: Text(result != null
                    ? 'Đã tạo dự án ${result.name}'
                    : 'Tạo dự án thất bại'),
                backgroundColor: result != null ? Colors.green : Colors.red,
              ));
            },
            child: const Text('Tạo'),
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
        title: const Text('Dự án'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => context.read<DataProvider>().fetchProjects(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddDialog,
        child: const Icon(Icons.add),
      ),
      body: data.loadingProjects
          ? const LoadingWidget(message: 'Đang tải danh sách dự án...')
          : data.projectsError != null
              ? _ErrorView(
                  error: data.projectsError!,
                  onRetry: () =>
                      context.read<DataProvider>().fetchProjects(),
                )
              : data.projects.isEmpty
                  ? const EmptyView('Chưa có dự án nào')
                  : RefreshIndicator(
                      onRefresh: () =>
                          context.read<DataProvider>().fetchProjects(),
                      child: ListView.builder(
                        itemCount: data.projects.length,
                        itemBuilder: (_, i) =>
                            _ProjectCard(project: data.projects[i]),
                      ),
                    ),
    );
  }
}

class _ProjectCard extends StatelessWidget {
  final Project project;

  const _ProjectCard({required this.project});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Colors.teal.shade100,
          child: const Icon(Icons.folder, color: Colors.teal),
        ),
        title: Text(project.name,
            style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (project.description != null)
              Text(
                project.description!,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            const SizedBox(height: 4),
            StatusBadge(status: project.status),
          ],
        ),
        isThreeLine: true,
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
            Row(
              children: [
                Expanded(
                  child: Text(project.name,
                      style: const TextStyle(
                          fontSize: 20, fontWeight: FontWeight.bold)),
                ),
                StatusBadge(status: project.status),
              ],
            ),
            const SizedBox(height: 12),
            if (project.description != null)
              Text(project.description!,
                  style: const TextStyle(color: Colors.grey)),
            const SizedBox(height: 8),
            Row(
              children: [
                const Icon(Icons.calendar_today_outlined,
                    size: 16, color: Colors.blue),
                const SizedBox(width: 6),
                Text('Bắt đầu: ${project.startDate}'),
              ],
            ),
            if (project.endDate != null) ...[
              const SizedBox(height: 4),
              Row(
                children: [
                  const Icon(Icons.event_outlined,
                      size: 16, color: Colors.blue),
                  const SizedBox(width: 6),
                  Text('Kết thúc: ${project.endDate}'),
                ],
              ),
            ],
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
