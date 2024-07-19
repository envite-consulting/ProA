import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import de from './locales/de.json';
import { useAppStore } from "@/store/app";

const i18n = createI18n({
  locale: 'en',
  fallbackLocale: 'en',
  messages: { en, de }
});

export default i18n;
