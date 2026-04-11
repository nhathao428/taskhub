import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/data_provider.dart';
import '../models/employee_suggestion.dart';
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
  final _skillsCtrl = TextEditingController();

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    _skillsCtrl.dispose();
    super.dispose();
  }

  Future<void> _search() async {
    if (!_formKey.currentState!.validate()) return;
    final skills = _skillsCtrl.text
        .trim()
        .split(',')
        .map((s) => s.trim())
        .where((s) => s.isNotEmpty)
        .toList();

    await context.read<DataProvider>().fetchSuggestions(
          taskTitle: _titleCtrl.text.trim(),
          taskDescription:
              _descCtrl.text.trim().isEmpty ? null : _descCtrl.text.trim(),
          requiredSkills: skills,
        );
  }

  @override
  Widget build(BuildContext context) {
    final data = context.watch<DataProvider>();
    return Scaffold(
      appBar: AppBar(
        title: const Text('AI Gợi ý Nhân viên'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Input form
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const Text(
                        'Tìm nhân viên phù hợp',
                        style: TextStyle(
                            fontWeight: FontWeight.bold, fontSize: 16),
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _titleCtrl,
                        decoration: const InputDecoration(
                          labelText: 'Tiêu đề công việc *',
                          hintText: 'Vd: Phát triển API backend',
                          border: OutlineInputBorder(),
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
                          border: OutlineInputBorder(),
                        ),
                        maxLines: 3,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _skillsCtrl,
                        decoration: const InputDecoration(
                          labelText: 'Kỹ năng yêu cầu (phân cách bằng dấu phẩy)',
                          hintText: 'Vd: Java, Spring Boot, PostgreSQL',
                          border: OutlineInputBorder(),
                        ),
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton.icon(
                        icon: const Icon(Icons.auto_awesome),
                        label: const Text('Tìm kiếm AI'),
                        onPressed:
                            data.loadingSuggestions ? null : _search,
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 14),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Results
            if (data.loadingSuggestions)
              const LoadingWidget(message: 'AI đang phân tích...')
            else if (data.suggestionsError != null)
              Card(
                color: Colors.red.shade50,
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      const Icon(Icons.error_outline, color: Colors.red),
                      const SizedBox(width: 8),
                      Expanded(
                          child: Text(data.suggestionsError!,
                              style: const TextStyle(color: Colors.red))),
                    ],
                  ),
                ),
              )
            else if (data.suggestions.isNotEmpty) ...[
              Row(
                children: [
                  const Icon(Icons.star, color: Colors.amber, size: 20),
                  const SizedBox(width: 6),
                  Text(
                    'Top ${data.suggestions.length} nhân viên đề xuất',
                    style: const TextStyle(
                        fontWeight: FontWeight.bold, fontSize: 16),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              ...data.suggestions.asMap().entries.map(
                    (entry) => _SuggestionCard(
                      rank: entry.key + 1,
                      suggestion: entry.value,
                    ),
                  ),
            ],
          ],
        ),
      ),
    );
  }
}

class _SuggestionCard extends StatelessWidget {
  final int rank;
  final EmployeeSuggestion suggestion;

  const _SuggestionCard({required this.rank, required this.suggestion});

  @override
  Widget build(BuildContext context) {
    final pct = (suggestion.overallScore * 100).toStringAsFixed(1);
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 6),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  backgroundColor: _rankColor(rank),
                  child: Text('$rank',
                      style: const TextStyle(
                          color: Colors.white, fontWeight: FontWeight.bold)),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(suggestion.fullName,
                          style: const TextStyle(
                              fontWeight: FontWeight.bold, fontSize: 15)),
                      if (suggestion.department != null)
                        Text(suggestion.department!,
                            style: const TextStyle(
                                color: Colors.grey, fontSize: 12)),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text('$pct%',
                        style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
                            color: _rankColor(rank))),
                    const Text('Điểm tổng',
                        style: TextStyle(color: Colors.grey, fontSize: 11)),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 8),
            // Score breakdown
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children: [
                _ScoreChip(
                    label: 'Kỹ năng',
                    score: suggestion.skillMatchScore),
                _ScoreChip(
                    label: 'Khối lượng',
                    score: suggestion.workloadScore),
                _ScoreChip(
                    label: 'Hiệu suất',
                    score: suggestion.performanceScore),
                _ScoreChip(
                    label: 'Chuyên cần',
                    score: suggestion.attendanceScore),
              ],
            ),
            if (suggestion.reasoning != null &&
                suggestion.reasoning!.isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(suggestion.reasoning!,
                  style: const TextStyle(
                      color: Colors.grey, fontSize: 12,
                      fontStyle: FontStyle.italic)),
            ],
          ],
        ),
      ),
    );
  }

  Color _rankColor(int rank) {
    switch (rank) {
      case 1:
        return Colors.amber.shade700;
      case 2:
        return Colors.blueGrey.shade400;
      case 3:
        return Colors.brown.shade400;
      default:
        return Colors.blue;
    }
  }
}

class _ScoreChip extends StatelessWidget {
  final String label;
  final double score;

  const _ScoreChip({required this.label, required this.score});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text('$label: ${(score * 100).toStringAsFixed(0)}%',
          style: const TextStyle(fontSize: 11, color: Colors.blue)),
    );
  }
}
