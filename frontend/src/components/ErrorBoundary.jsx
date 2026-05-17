import { Component } from 'react'

// ErrorBoundary nằm ngoài LanguageProvider nên không dùng được hook —
// đọc ngôn ngữ trực tiếp từ localStorage để chọn nội dung.
const TEXT = {
  vi: {
    title: 'Đã xảy ra lỗi',
    desc: 'Ứng dụng gặp sự cố không mong muốn. Vui lòng tải lại trang.',
    reload: 'Tải lại trang',
  },
  en: {
    title: 'An error occurred',
    desc: 'The application ran into an unexpected problem. Please reload the page.',
    reload: 'Reload page',
  },
}

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, info) {
    console.error('ErrorBoundary caught an error:', error, info)
  }

  render() {
    if (this.state.hasError) {
      const lang = localStorage.getItem('app_lang') === 'en' ? 'en' : 'vi'
      const tx = TEXT[lang]
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center p-8 bg-white rounded-xl shadow-md max-w-md w-full">
            <div className="text-5xl mb-4">⚠️</div>
            <h1 className="text-xl font-bold text-gray-800 mb-2">{tx.title}</h1>
            <p className="text-gray-500 text-sm mb-6">{tx.desc}</p>
            <button
              onClick={() => window.location.reload()}
              className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg text-sm font-medium transition-colors"
            >
              {tx.reload}
            </button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
