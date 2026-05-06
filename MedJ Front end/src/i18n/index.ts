import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import en from './en.json';
import bg from './bg.json';

i18n
  .use(LanguageDetector)        // detects language from browser / localStorage
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      bg: { translation: bg },
    },
    fallbackLng: 'en',
    supportedLngs: ['en', 'bg'],
    interpolation: {
      escapeValue: false,        // React already escapes values
    },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],  // remembers the user's choice across sessions
      lookupLocalStorage: 'medj_lang',
    },
  });

export default i18n;
