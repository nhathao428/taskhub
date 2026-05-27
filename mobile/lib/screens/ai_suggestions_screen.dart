import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/data_provider.dart';
import '../models/employee_suggestion.dart';
import '../theme/app_theme.dart';
import '../widgets/aurora_background.dart';
import '../widgets/loading_widget.dart';

class AiSuggestionsScreen extends StatefulWidget {
  const AiSuggestionsScreen({super.key});

  @override
  State<AiSuggestionsScreen> createState() => _AiSuggestionsScreenState();
}

class _AiSuggestionsScreenState extends State<AiSuggestionsScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleCtrl = TextEditingController();
  final _descCtrl = TextEditingController();

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  Future<void> _search() async {
    if (!_formKey.currentState!.validate()) return;
    await context.read<DataProvider>().fetchSuggestions(
          taskTitle: _titleCtrl.text.trim(),
          taskDescription:
              _descCtrl.text.trim().isEmpty ? null : _descCtrl.text.trim(),
        );
  }

  @override
  Widget build(BuildContext context) {
    final data = context.watch<DataProvider>();
    return Scaffold(
      backgroundColor: AppTheme.slate50,
      appBar: AppBar(title: const Text('AI Gợi ý nhân viên')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Hero aurora-mesh
            Container(
              height: 130,
              clipBehavior: Clip.antiAlias,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(22),
                boxShadow: AppTheme.softShadowMd,
              ),
              child: Stack(
                children: [
                  const Positioned.fill(
                    child: AuroraBackground(
                      fullScreen: true,
                      child: SizedBox.shrink(),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(18),
                    child: Row(
                      children: [
                        Container(
                          width: 50,
                          height: 50,
                          decoration: BoxDecoration(
                            color: Colors.white.withValues(alpha: 0.2),
                            borderRadius: BorderRadius.circular(14),
                            border: Border.all(
                              color: Colors.white.withValues(alpha: 0.3),
                            ),
                          ),
                          child: const Icon(Icons.auto_awesome_rounded,
                              color: Colors.white, size: 26),
                        ),
                        const SizedBox(width: 14),
                        const Expanded(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'AI gợi ý nhân viên phù hợp',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.w700,
                                  fontSize: 16,
                                  letterSpacing: -0.2,
                                ),
                              ),
                              SizedBox(height: 4),
                              Text(
                                'Phân tích kỹ năng, hiệu suất và workload',
                                style: TextStyle(
                                    color: Color(0xFFCBD5E1), fontSize: 12),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Form card
            Container(
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: AppTheme.slate200),
                boxShadow: AppTheme.softShadow,
              ),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    TextFormField(
                      controller: _titleCtrl,
                      decoration: const InputDecoration(
                        labelText: 'Tiêu đề công việc',
                        prefixIcon: Icon(Icons.assignment_outlined),
                      ),
                      validator: (v) => (v == null || v.trim().isEmpty)
                          ? 'Vui lòng nhập tiêu đề'
                          : null,
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _descCtrl,
                      decoration: const InputDecoration(
                        labelText: 'Mô tả (tùy chọn)',
                        prefixIcon: Icon(Icons.description_outlined),
                        alignLabelWithHint: true,
                      ),
                      maxLines: 3,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      icon: data.loadingSuggestions
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : const Icon(Icons.auto_awesome_rounded),
                      label: Text(
                        data.loadingSuggestions
                            ? 'Đang phân tích...'
                            : 'Phân tích bằng AI',
                      ),
                      onPressed: data.loadingSuggestions ? null : _search,
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        backgroundColor: AppTheme.brand600,
                        foregroundColor: Colors.white,
                        elevation: 0,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Results
            if (data.loadingSuggestions)
              const LoadingWidget(message: 'AI đang phân tích...')
            else if (data.suggestionsError != null)
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: AppTheme.rose500.withValues(alpha: 0.08),
                  border: Border.all(
                      color: AppTheme.rose500.withValues(alpha: 0.3)),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error_outline_rounded,
                        color: AppTheme.rose500),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        data.suggestionsError!,
                        style: const TextStyle(
                            color: AppTheme.rose500, fontSize: 13),
                      ),
                    ),
                  ],
                ),
              )
            else if (data.suggestions.isEmpty)
              const SizedBox.shrink()
            else
              ...data.suggestions.asMap().entries.map(
                    (entry) => _SuggestionCard(
                      suggestion: entry.value,
                      fallbackRank: entry.key + 1,
                    ),
                  ),
          ],
        ),
      ),
    );
  }
}

class _SuggestionCard extends StatelessWidget {
  final EmployeeSuggestion suggestion;
  final int fallbackRank;

  const _SuggestionCard(
      {required this.suggestion, required this.fallbackRank});

  @override
  Widget build(BuildContext context) {
    final rank = suggestion.rank > 0 ? suggestion.rank : fallbackRank;
    final theme = _themeFor(rank);
    final isTop = rank <= 3;

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(
          color: isTop ? theme.accent.withValues(alpha: 0.4) : AppTheme.slate200,
          width: isTop ? 1.5 : 1,
        ),
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
                // Rank badge
                Container(
                  width: 48,
                  height: 48,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: theme.bg,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Text(
                    _initials(suggestion.firstName, suggestion.lastName),
                    style: TextStyle(
                      color: theme.accent,
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
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 7, vertical: 2),
                            decoration: BoxDecoration(
                              color: theme.bg,
                              borderRadius: BorderRadius.circular(999),
                            ),
                            child: Text(
                              '#$rank',
                              style: TextStyle(
                                color: theme.accent,
                                fontSize: 11,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                          if (isTop) ...[
                            const SizedBox(width: 6),
                            Icon(Icons.emoji_events_rounded,
                                color: theme.accent, size: 16),
                          ],
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        suggestion.fullName,
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 15,
                          color: AppTheme.slate900,
                          letterSpacing: -0.2,
                        ),
                      ),
                      if (suggestion.department != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 2),
                          child: Text(
                            suggestion.department!,
                            style: const TextStyle(
                              color: AppTheme.slate500,
                              fontSize: 12,
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ],
            ),
            if (suggestion.reasoning != null &&
                suggestion.reasoning!.isNotEmpty) ...[
              const SizedBox(height: 12),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: theme.bg.withValues(alpha: 0.5),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Icon(
                      Icons.format_quote_rounded,
                      color: theme.accent.withValues(alpha: 0.5),
                      size: 20,
                    ),
                    const SizedBox(width: 6),
                    Expanded(
                      child: Text(
                        suggestion.reasoning!,
                        style: const TextStyle(
                            fontSize: 13,
                            height: 1.5,
                            color: AppTheme.slate700),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  String _initials(String first, String last) {
    final f = first.isNotEmpty ? first[0] : '';
    final l = last.isNotEmpty ? last[0] : '';
    final s = (f + l).toUpperCase();
    return s.isEmpty ? '?' : s;
  }

  _RankTheme _themeFor(int rank) {
    switch (rank) {
      case 1:
        return _RankTheme(
            accent: AppTheme.amber500, bg: AppTheme.amber100);
      case 2:
        return _RankTheme(
            accent: AppTheme.slate500, bg: AppTheme.slate100);
      case 3:
        return _RankTheme(
            accent: AppTheme.sky500, bg: AppTheme.sky100);
      default:
        return _RankTheme(
            accent: AppTheme.brand600, bg: AppTheme.brand50);
    }
  }
}

class _RankTheme {
  final Color accent;
  final Color bg;
  const _RankTheme({required this.accent, required this.bg});
}
