import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

/// Khối "chưa có dữ liệu" — icon + dòng chữ.
class EmptyView extends StatelessWidget {
  final String text;
  const EmptyView(this.text, {super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 76,
              height: 76,
              decoration: BoxDecoration(
                color: AppTheme.brand50,
                borderRadius: BorderRadius.circular(24),
              ),
              child: const Icon(Icons.inbox_outlined,
                  color: AppTheme.brand600, size: 36),
            ),
            const SizedBox(height: 14),
            Text(
              text,
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: AppTheme.slate500,
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
