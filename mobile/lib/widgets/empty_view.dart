import 'package:flutter/material.dart';

/// Khối "chưa có dữ liệu" — tranh minh họa + dòng chữ.
class EmptyView extends StatelessWidget {
  final String text;
  const EmptyView(this.text, {super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Image.asset(
            'assets/illustrations/empty.png',
            height: 168,
            fit: BoxFit.contain,
          ),
          const SizedBox(height: 8),
          Text(
            text,
            style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 14),
          ),
        ],
      ),
    );
  }
}
