import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class StatusBadge extends StatelessWidget {
  final String status;

  const StatusBadge({super.key, required this.status});

  ({Color fg, Color bg, String label}) get _theme {
    switch (status.toLowerCase()) {
      case 'completed':
      case 'done':
        return (
          fg: AppTheme.emerald500,
          bg: AppTheme.emerald100,
          label: status.toLowerCase() == 'done' ? 'Xong' : 'Hoàn thành',
        );
      case 'in_progress':
        return (
          fg: AppTheme.brand600,
          bg: AppTheme.brand50,
          label: 'Đang làm',
        );
      case 'pending':
        return (
          fg: AppTheme.amber500,
          bg: AppTheme.amber100,
          label: 'Chờ xử lý',
        );
      case 'ongoing':
        return (
          fg: AppTheme.sky500,
          bg: AppTheme.sky100,
          label: 'Đang triển khai',
        );
      default:
        return (fg: AppTheme.slate500, bg: AppTheme.slate100, label: status);
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = _theme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: t.bg,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        t.label,
        style: TextStyle(
          color: t.fg,
          fontSize: 11.5,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.2,
        ),
      ),
    );
  }
}
