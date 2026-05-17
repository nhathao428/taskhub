import { useTranslation } from '../context/LanguageContext'

// Cờ vẽ bằng SVG inline — KHÔNG dùng emoji cờ vì Windows không render emoji cờ.
function VietnamFlag({ className = '' }) {
  return (
    <svg viewBox="0 0 30 20" className={className} aria-hidden="true">
      <rect width="30" height="20" fill="#da251d" />
      <polygon
        points="15,4 16.41,8.06 20.71,8.15 17.28,10.74 18.53,14.85 15,12.4 11.47,14.85 12.72,10.74 9.29,8.15 13.59,8.06"
        fill="#ffff00"
      />
    </svg>
  )
}

function UKFlag({ className = '' }) {
  return (
    <svg viewBox="0 0 60 30" className={className} aria-hidden="true">
      <clipPath id="ls-uk-clip">
        <rect width="60" height="30" />
      </clipPath>
      <g clipPath="url(#ls-uk-clip)">
        <rect width="60" height="30" fill="#012169" />
        <path d="M0,0 L60,30 M60,0 L0,30" stroke="#ffffff" strokeWidth="6" />
        <path d="M0,0 L60,30 M60,0 L0,30" stroke="#c8102e" strokeWidth="3.5" />
        <path d="M30,0 V30 M0,15 H60" stroke="#ffffff" strokeWidth="10" />
        <path d="M30,0 V30 M0,15 H60" stroke="#c8102e" strokeWidth="6" />
      </g>
    </svg>
  )
}

const LANGS = [
  { code: 'vi', label: 'VI', Flag: VietnamFlag },
  { code: 'en', label: 'EN', Flag: UKFlag },
]

// variant: 'light' (nền sáng) | 'dark' (nền tối, vd trang đăng nhập có gradient)
export default function LanguageSwitcher({ variant = 'light', className = '' }) {
  const { lang, setLang } = useTranslation()
  const dark = variant === 'dark'

  const wrapCls = dark ? 'bg-white/15 backdrop-blur' : 'bg-gray-100 border border-gray-200'
  const activeCls = dark
    ? 'bg-white text-indigo-700 shadow-sm'
    : 'bg-white text-indigo-600 shadow-sm'
  const idleCls = dark ? 'text-white/80 hover:text-white' : 'text-gray-500 hover:text-gray-800'

  return (
    <div className={`inline-flex items-center gap-0.5 rounded-lg p-0.5 ${wrapCls} ${className}`}>
      {LANGS.map(({ code, label, Flag }) => (
        <button
          key={code}
          type="button"
          onClick={() => setLang(code)}
          aria-pressed={lang === code}
          title={code === 'vi' ? 'Tiếng Việt' : 'English'}
          className={`flex items-center gap-1.5 px-2 py-1 rounded-md text-xs font-bold transition-colors ${
            lang === code ? activeCls : idleCls
          }`}
        >
          <Flag className="w-5 h-[14px] rounded-sm shadow-sm ring-1 ring-black/10" />
          {label}
        </button>
      ))}
    </div>
  )
}
