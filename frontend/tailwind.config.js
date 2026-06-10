/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      // Audit (taste-skill / redesign): bỏ Inter (font AI default), thay Figtree Variable
      // — modern geometric sans, có nét character riêng. Mono dùng JetBrains cho data + code.
      fontFamily: {
        sans: ['"Figtree Variable"', 'ui-sans-serif', 'system-ui', '-apple-system', 'Segoe UI', 'sans-serif'],
        mono: ['"JetBrains Mono Variable"', 'ui-monospace', 'SFMono-Regular', 'Menlo', 'monospace'],
      },
      letterSpacing: {
        tightest: '-0.04em',
      },
      // Brand FROM indigo (AI gradient cliché) → teal: calm, ít saturate, less generic.
      colors: {
        brand: {
          50:  '#eefaf9',
          100: '#d4f1ee',
          200: '#ace3df',
          300: '#75cec8',
          400: '#3fb5af',
          500: '#1f9a96',
          600: '#147c79',
          700: '#136462',
          800: '#135150',
          900: '#0f4242',
          950: '#062322',
        },
        ink: {
          50:  '#fafaf9',
          100: '#f2f2f0',
          200: '#e7e7e3',
          300: '#cbcbc4',
          400: '#9d9d94',
          500: '#74746a',
          600: '#54544b',
          700: '#3f3f38',
          800: '#28282a',
          900: '#1a1a1c',
          950: '#0d0d0f',
        },
      },
      // Tinted shadows (audit: bỏ generic black box-shadow).
      boxShadow: {
        soft:      '0 1px 2px 0 rgba(20,124,121,0.06), 0 1px 3px 0 rgba(20,124,121,0.05)',
        'soft-md': '0 6px 16px -4px rgba(20,124,121,0.10), 0 2px 6px -2px rgba(20,124,121,0.06)',
        'soft-lg': '0 18px 40px -12px rgba(20,124,121,0.18), 0 6px 16px -6px rgba(20,124,121,0.10)',
        'brand-glow': '0 14px 32px -10px rgba(31,154,150,0.45)',
        'ring-soft': 'inset 0 0 0 1px rgba(20,124,121,0.10)',
      },
      // Radii khác nhau cho inner vs container.
      borderRadius: {
        'lg': '0.625rem',
        'xl': '0.875rem',
        '2xl': '1.125rem',
        '3xl': '1.5rem',
      },
      keyframes: {
        'fade-in-up': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'shimmer': {
          '100%': { transform: 'translateX(100%)' },
        },
      },
      animation: {
        'fade-in-up': 'fade-in-up 0.4s cubic-bezier(0.16, 1, 0.3, 1) both',
        'shimmer': 'shimmer 1.4s infinite',
      },
    },
  },
  plugins: [],
}
