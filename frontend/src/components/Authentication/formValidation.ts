import i18n from "@/i18n";

const t = i18n.global.t;

export const baseEmailRules = [
  (email: string) => !!email || `${t('authentication.email')} ${t('validation.isRequired')}`,
  (email: string) => email.length <= 64 || `${t('authentication.email')} ${t('validation.max64Characters')}`
]

export const emailRules = baseEmailRules.concat([
  (email: string) => /.+@.+\..+/.test(email) || `${t('authentication.email')} ${t('validation.mustBeValid')}`,
]);

export const emailRulesSignIn = baseEmailRules.concat([
  (email: string) => email === 'admin' || /.+@.+\..+/.test(email) || `${t('authentication.email')} ${t('validation.mustBeValid')}`,
]);

export const newPasswordRules = [
  (password: string) => !!password || `${t('authentication.password')} ${t('validation.isRequired')}`,
  (password: string) => password.length >= 8 || `${t('authentication.password')} ${t('validation.min8Characters')}`,
  (password: string) => password.length <= 64 || `${t('authentication.password')} ${t('validation.max64Characters')}`,
  (password: string) => /(?=.*[a-z])/.test(password) || `${t('authentication.password')} ${t('validation.atLeastOneLower')}`,
  (password: string) => /(?=.*[A-Z])/.test(password) || `${t('authentication.password')} ${t('validation.atLeastOneUpper')}`,
  (password: string) => /(?=.*\d)/.test(password) || `${t('authentication.password')} ${t('validation.atLeastOneDigit')}`,
]

export const currentPasswordRules = [
  (password: string) => !!password || `${t('authentication.password')} ${t('validation.isRequired')}`,
]

export const firstNameRules = [
  (firstName: string) => !!firstName || `${t('authentication.firstName')} ${t('validation.isRequired')}`,
  (firstName: string) => firstName.length <= 64 || `${t('authentication.firstName')} ${t('validation.max64Characters')}`,
]

export const lastNameRules = [
  (lastName: string) => !!lastName || `${t('authentication.lastName')} ${t('validation.isRequired')}`,
  (lastName: string) => lastName.length <= 64 || `${t('authentication.lastName')} ${t('validation.max64Characters')}`,
]
