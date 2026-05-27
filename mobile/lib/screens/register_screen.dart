import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../i18n/language_provider.dart';
import '../providers/auth_provider.dart';
import '../theme/app_theme.dart';
import '../widgets/aurora_background.dart';
import '../widgets/gradient_button.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _usernameCtrl = TextEditingController();
  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  bool _obscurePassword = true;

  @override
  void dispose() {
    _usernameCtrl.dispose();
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final auth = context.read<AuthProvider>();
    final success = await auth.register(
      _usernameCtrl.text.trim(),
      _emailCtrl.text.trim(),
      _passwordCtrl.text,
    );
    if (!mounted) return;
    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(context.trOnce('Đăng ký thành công! Vui lòng đăng nhập.')),
          backgroundColor: AppTheme.emerald500,
        ),
      );
      Navigator.pushReplacementNamed(context, '/login');
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(auth.error ?? context.trOnce('Đăng ký thất bại')),
          backgroundColor: AppTheme.rose500,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final loading = context.watch<AuthProvider>().loading;
    return Scaffold(
      backgroundColor: AppTheme.slate950,
      body: AuroraBackground(
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 420),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Center(
                      child: Container(
                        width: 64,
                        height: 64,
                        decoration: BoxDecoration(
                          color: AppTheme.brand600,
                          borderRadius: BorderRadius.circular(18),
                          boxShadow: AppTheme.brandGlow,
                        ),
                        child: const Icon(Icons.person_add_alt_1,
                            color: Colors.white, size: 32),
                      ),
                    ),
                    const SizedBox(height: 18),
                    Text(
                      context.tr('Tạo tài khoản'),
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                        letterSpacing: -0.4,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      context.tr('Tham gia hệ thống quản lý công việc'),
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        color: Color(0xFFCBD5E1),
                        fontSize: 13.5,
                      ),
                    ),
                    const SizedBox(height: 26),
                    Container(
                      padding: const EdgeInsets.all(22),
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.96),
                        borderRadius: BorderRadius.circular(24),
                        border: Border.all(
                          color: Colors.white.withValues(alpha: 0.4),
                          width: 1,
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(alpha: 0.25),
                            blurRadius: 40,
                            offset: const Offset(0, 20),
                          ),
                        ],
                      ),
                      child: Form(
                        key: _formKey,
                        child: Column(
                          children: [
                            TextFormField(
                              controller: _usernameCtrl,
                              decoration: InputDecoration(
                                labelText: context.tr('Tên đăng nhập'),
                                prefixIcon: const Icon(Icons.person_outline),
                              ),
                              validator: (v) {
                                if (v == null || v.trim().isEmpty) {
                                  return context.trOnce('Vui lòng nhập tên đăng nhập');
                                }
                                if (v.trim().length < 3) {
                                  return context.trOnce('Tối thiểu 3 ký tự');
                                }
                                return null;
                              },
                            ),
                            const SizedBox(height: 14),
                            TextFormField(
                              controller: _emailCtrl,
                              keyboardType: TextInputType.emailAddress,
                              decoration: InputDecoration(
                                labelText: context.tr('Email'),
                                prefixIcon: const Icon(Icons.email_outlined),
                              ),
                              validator: (v) {
                                if (v == null || v.trim().isEmpty) {
                                  return context.trOnce('Vui lòng nhập email');
                                }
                                if (!v.contains('@')) {
                                  return context.trOnce('Email không hợp lệ');
                                }
                                return null;
                              },
                            ),
                            const SizedBox(height: 14),
                            TextFormField(
                              controller: _passwordCtrl,
                              obscureText: _obscurePassword,
                              decoration: InputDecoration(
                                labelText: context.tr('Mật khẩu'),
                                prefixIcon: const Icon(Icons.lock_outline),
                                helperText: context.tr(
                                    'Tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt (vd: !@#\$)'),
                                helperMaxLines: 2,
                                suffixIcon: IconButton(
                                  icon: Icon(
                                    _obscurePassword
                                        ? Icons.visibility_off_outlined
                                        : Icons.visibility_outlined,
                                    color: AppTheme.slate400,
                                  ),
                                  onPressed: () => setState(() =>
                                      _obscurePassword = !_obscurePassword),
                                ),
                              ),
                              validator: (v) {
                                // Quy tắc trùng với @Pattern trong RegisterRequestV2.java
                                if (v == null || v.isEmpty) {
                                  return context.trOnce('Vui lòng nhập mật khẩu');
                                }
                                if (v.length < 8 || v.length > 100) {
                                  return context.trOnce('Mật khẩu từ 8 đến 100 ký tự');
                                }
                                final hasLetter = RegExp(r'[A-Za-z]').hasMatch(v);
                                final hasDigit = RegExp(r'\d').hasMatch(v);
                                final hasSpecial = RegExp(
                                    r'[!@#$%^&*()_+\-=\[\]{};:' "'" r'"\\|,.<>/?`~]')
                                    .hasMatch(v);
                                if (!hasLetter || !hasDigit || !hasSpecial) {
                                  return context.trOnce('Phải có chữ, số và ký tự đặc biệt');
                                }
                                return null;
                              },
                            ),
                            const SizedBox(height: 22),
                            GradientButton(
                              label: context.tr('Đăng ký'),
                              icon: Icons.person_add_alt_1,
                              loading: loading,
                              onPressed: _submit,
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 18),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          context.tr('Đã có tài khoản?'),
                          style: const TextStyle(
                            color: Color(0xFFCBD5E1),
                            fontSize: 13.5,
                          ),
                        ),
                        TextButton(
                          onPressed: () => Navigator.pushReplacementNamed(
                              context, '/login'),
                          style: TextButton.styleFrom(
                            foregroundColor: Colors.white,
                            textStyle: const TextStyle(
                              fontWeight: FontWeight.w700,
                              fontSize: 13.5,
                            ),
                          ),
                          child: Text(context.tr('Đăng nhập')),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
