import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { en } from '../i18n/translations'

const LanguageContext = createContext(null)

const STORAGE_KEY = 'app_lang'

function getInitialLang() {
  const saved = localStorage.getItem(STORAGE_KEY)
  return saved === 'en' || saved === 'vi' ? saved : 'vi'
}

export function LanguageProvider({ children }) {
  const [lang, setLangState] = useState(getInitialLang)

  const setLang = useCallback((next) => {
    localStorage.setItem(STORAGE_KEY, next)
    setLangState(next)
  }, [])

  // t(key, params): key là chuỗi tiếng Việt gốc. {placeholder} được thay bằng params.
  const t = useCallback(
    (key, params) => {
      let str = lang === 'en' ? en[key] ?? key : key
      if (params) {
        for (const [name, value] of Object.entries(params)) {
          str = str.split(`{${name}}`).join(String(value))
        }
      }
      return str
    },
    [lang],
  )

  const value = useMemo(() => ({ lang, setLang, t }), [lang, setLang, t])

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>
}

export function useTranslation() {
  const ctx = useContext(LanguageContext)
  if (!ctx) {
    throw new Error('useTranslation phải được dùng bên trong <LanguageProvider>')
  }
  return ctx
}
