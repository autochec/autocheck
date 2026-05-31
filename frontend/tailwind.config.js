/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary:   { DEFAULT: '#2563EB', dark: '#1D4ED8' },
        secondary: { DEFAULT: '#64748B' },
        success:   { DEFAULT: '#16A34A', light: '#DCFCE7' },
        warning:   { DEFAULT: '#D97706', light: '#FEF3C7' },
        error:     { DEFAULT: '#DC2626', light: '#FEE2E2' },
        neutral:   { DEFAULT: '#6B7280', 50: '#F9FAFB', 100: '#F3F4F6', 900: '#111827' },
      },
      fontFamily: {
        sans: ['"DM Sans"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
}
