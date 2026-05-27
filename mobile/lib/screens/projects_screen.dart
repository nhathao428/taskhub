import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../i18n/language_provider.dart';
import '../widgets/empty_view.dart';
import '../providers/data_provider.dart';
import '../models/project.dart';
import '../theme/app_theme.dart';
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
        title: Text(ctx.tr('Thêm dự án')),
        content: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: nameCtrl,
                  decoration:
                      InputDecoration(labelText: ctx.tr('Tên dự án *')),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? ctx.trOnce('Bắt buộc') : null,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: descCtrl,
                  decoration: InputDecoration(labelText: ctx.tr('Mô tả')),
                  maxLines: 3,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: startCtrl,
                  decoration: InputDecoration(
                      labelText: ctx.tr('Ngày bắt đầu (YYYY-MM-DD) *')),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? ctx.trOnce('Bắt buộc') : null,
                ),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text(ctx.tr('Huỷ')),
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
                    ? context.trOnce('Đã tạo dự án {name}', {'name': result.name})
                    : context.trOnce('Tạo dự án thất bại')),
                backgroundColor:
                    result != null ? AppTheme.emerald500 : AppTheme.rose500,
              ));
            },
            child: Text(ctx.tr('Tạo')),
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
        title: Text(context.tr('Dự án')),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () => context.read<DataProvider>().fetchProjects(),
          ),
          const SizedBox(width: 4),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _showAddDialog,
        icon: const Icon(Icons.add_rounded),
        label: Text(context.tr('Thêm')),
      ),
      body: data.loadingProjects
          ? LoadingWidget(message: context.tr('Đang tải danh sách dự án...'))
          : data.projectsError != null
              ? _ErrorView(
                  error: data.projectsError!,
                  onRetry: () =>
                      context.read<DataProvider>().fetchProjects(),
                )
              : data.projects.isEmpty
                  ? EmptyView(context.tr('Chưa có dự án nào'))
                  : RefreshIndicator(
                      color: AppTheme.brand600,
                      onRefresh: () =>
                          context.read<DataProvider>().fetchProjects(),
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(12, 4, 12, 96),
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
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      width: 44,
                      height: 44,
                      decoration: BoxDecoration(
                        color: AppTheme.sky100,
                        borderRadius: BorderRadius.circular(13),
                      ),
                      child: const Icon(Icons.folder_rounded,
                          color: AppTheme.sky500, size: 22),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            project.name,
                            style: const TextStyle(
                              fontWeight: FontWeight.w700,
                              fontSize: 14.5,
                              color: AppTheme.slate900,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 6),
                          StatusBadge(status: project.status),
                        ],
                      ),
                    ),
                    const Icon(Icons.chevron_right_rounded,
                        color: AppTheme.slate400),
                  ],
                ),
                if (project.description != null) ...[
                  const SizedBox(height: 10),
                  Text(
                    project.description!,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: AppTheme.slate500,
                      fontSize: 12.5,
                      height: 1.4,
                    ),
                  ),
                ],
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
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Text(project.name,
                      style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: AppTheme.slate900,
                          letterSpacing: -0.3)),
                ),
                StatusBadge(status: project.status),
              ],
            ),
            if (project.description != null) ...[
              const SizedBox(height: 12),
              Text(project.description!,
                  style: const TextStyle(
                      color: AppTheme.slate500, fontSize: 13.5, height: 1.45)),
            ],
            const SizedBox(height: 14),
            _DetailRow(
                icon: Icons.calendar_today_outlined,
                label: context.trOnce('Bắt đầu: {date}', {'date': project.startDate})),
            if (project.endDate != null)
              _DetailRow(
                  icon: Icons.event_outlined,
                  label: context.trOnce('Kết thúc: {date}', {'date': project.endDate!})),
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
      padding: const EdgeInsets.symmetric(vertical: 5),
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
                label: Text(context.tr('Thử lại'))),
          ],
        ),
      ),
    );
  }
}
