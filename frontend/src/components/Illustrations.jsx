// Tranh minh họa vector phẳng (flat) — tự vẽ, chủ đề công việc.
// Không vướng bản quyền. Tô theo bảng màu của ứng dụng (indigo / purple).

export function WorkspaceIllustration({ className = '' }) {
  return (
    <svg viewBox="0 0 460 360" className={className} fill="none"
         xmlns="http://www.w3.org/2000/svg" role="img" aria-hidden="true">
      {/* nền */}
      <ellipse cx="230" cy="322" rx="190" ry="28" fill="#E0E7FF" />
      <circle cx="338" cy="120" r="92" fill="#EEF2FF" />
      <circle cx="92" cy="226" r="54" fill="#EEF2FF" />

      {/* cửa sổ ứng dụng */}
      <rect x="70" y="74" width="262" height="196" rx="18" fill="#FFFFFF"
            stroke="#E2E8F0" strokeWidth="2" />
      <path d="M70 92a18 18 0 0 1 18-18h226a18 18 0 0 1 18 18v20H70z" fill="#4F46E5" />
      <circle cx="92" cy="93" r="5" fill="#FFFFFF" fillOpacity="0.55" />
      <circle cx="110" cy="93" r="5" fill="#FFFFFF" fillOpacity="0.55" />
      <circle cx="128" cy="93" r="5" fill="#FFFFFF" fillOpacity="0.55" />

      {/* biểu đồ cột */}
      <rect x="96" y="208" width="26" height="42" rx="5" fill="#A5B4FC" />
      <rect x="132" y="180" width="26" height="70" rx="5" fill="#818CF8" />
      <rect x="168" y="222" width="26" height="28" rx="5" fill="#A5B4FC" />
      <rect x="204" y="160" width="26" height="90" rx="5" fill="#6366F1" />
      <line x1="90" y1="250" x2="312" y2="250" stroke="#E2E8F0" strokeWidth="2" />

      {/* danh sách công việc mini */}
      <rect x="248" y="128" width="70" height="58" rx="9" fill="#F8FAFC"
            stroke="#E2E8F0" strokeWidth="1.5" />
      <circle cx="262" cy="144" r="6.5" fill="#10B981" />
      <path d="M258.5 144l2.7 2.7 4.3-5" stroke="#FFFFFF" strokeWidth="2"
            strokeLinecap="round" strokeLinejoin="round" />
      <rect x="274" y="141" width="34" height="6" rx="3" fill="#CBD5E1" />
      <circle cx="262" cy="166" r="6.5" fill="#C7D2FE" />
      <rect x="274" y="163" width="26" height="6" rx="3" fill="#E2E8F0" />

      {/* người */}
      <ellipse cx="358" cy="300" rx="44" ry="10" fill="#C7D2FE" fillOpacity="0.7" />
      <rect x="338" y="232" width="40" height="58" rx="18" fill="#7C3AED" />
      <rect x="344" y="276" width="12" height="34" rx="6" fill="#312E81" />
      <rect x="360" y="276" width="12" height="34" rx="6" fill="#312E81" />
      <rect x="372" y="240" width="12" height="40" rx="6" fill="#7C3AED"
            transform="rotate(28 378 260)" />
      <circle cx="358" cy="214" r="20" fill="#FCD9B8" />
      <path d="M338 210a20 20 0 0 1 40 0c0-14-9-22-20-22s-20 8-20 22z" fill="#1E293B" />

      {/* phù hiệu dấu tích */}
      <circle cx="120" cy="120" r="22" fill="#10B981" />
      <path d="M110 120l7 7 13-15" stroke="#FFFFFF" strokeWidth="4.5"
            strokeLinecap="round" strokeLinejoin="round" />

      {/* chấm trang trí */}
      <circle cx="402" cy="208" r="9" fill="#F472B6" />
      <circle cx="60" cy="120" r="7" fill="#FBBF24" />
      <circle cx="406" cy="96" r="6" fill="#22D3EE" />
    </svg>
  )
}

export function EmptyIllustration({ className = '' }) {
  return (
    <svg viewBox="0 0 240 200" className={className} fill="none"
         xmlns="http://www.w3.org/2000/svg" role="img" aria-hidden="true">
      <ellipse cx="120" cy="178" rx="78" ry="13" fill="#E0E7FF" />
      <circle cx="120" cy="94" r="66" fill="#EEF2FF" />

      {/* bảng ghi chú */}
      <rect x="80" y="50" width="84" height="104" rx="12" fill="#FFFFFF"
            stroke="#C7D2FE" strokeWidth="3" />
      <rect x="103" y="42" width="38" height="18" rx="6" fill="#6366F1" />
      <rect x="94" y="78" width="56" height="8" rx="4" fill="#E0E7FF" />
      <rect x="94" y="96" width="42" height="8" rx="4" fill="#E0E7FF" />
      <rect x="94" y="114" width="50" height="8" rx="4" fill="#E0E7FF" />

      {/* kính lúp */}
      <circle cx="150" cy="126" r="22" fill="#FFFFFF" stroke="#A78BFA" strokeWidth="5" />
      <line x1="166" y1="142" x2="180" y2="156" stroke="#A78BFA" strokeWidth="8"
            strokeLinecap="round" />

      {/* chấm trang trí */}
      <circle cx="58" cy="62" r="7" fill="#F472B6" />
      <circle cx="188" cy="66" r="9" fill="#FBBF24" />
      <circle cx="62" cy="126" r="6" fill="#22D3EE" />
    </svg>
  )
}

// Khối "chưa có dữ liệu" — tranh + dòng chữ, dùng trong bảng/biểu đồ rỗng.
export function EmptyState({ text, className = '' }) {
  return (
    <div className={`flex flex-col items-center justify-center py-6 ${className}`}>
      <EmptyIllustration className="w-36 h-auto" />
      <p className="mt-2 text-sm text-gray-400">{text}</p>
    </div>
  )
}
