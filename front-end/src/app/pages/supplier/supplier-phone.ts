import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { digitsOnly } from './supplier-address';

export const PHONE_MIN_DIGITS = 10;
export const PHONE_MAX_DIGITS = 11;
export const PHONE_ERROR_MESSAGE = 'Informe um telefone válido com DDD (10 ou 11 dígitos).';
export const PHONE_EMPTY_DISPLAY = '—';

/**
 * Máscara visual do telefone enquanto o usuário digita.
 * Até 10 dígitos: (XX) XXXX-XXXX. Com 11 dígitos: (XX) XXXXX-XXXX. Máximo de 11 dígitos.
 */
export function formatPhone(value: any): string {
  const digits = digitsOnly(value).slice(0, PHONE_MAX_DIGITS);
  if (digits.length === 0) {
    return '';
  }
  if (digits.length <= 2) {
    return `(${digits}`;
  }
  const ddd = digits.slice(0, 2);
  const rest = digits.slice(2);
  const prefixLength = digits.length > PHONE_MIN_DIGITS ? 5 : 4;
  if (rest.length <= prefixLength) {
    return `(${ddd}) ${rest}`;
  }
  return `(${ddd}) ${rest.slice(0, prefixLength)}-${rest.slice(prefixLength)}`;
}

/** Telefone para exibição na grade e no detalhe: máscara quando há dígitos; senão, o travessão. */
export function formatPhoneForDisplay(value: any): string {
  return digitsOnly(value) === '' ? PHONE_EMPTY_DISPLAY : formatPhone(value);
}

/** Validador: vazio é válido; preenchido precisa ter 10 ou 11 dígitos. */
export function phoneValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const length = digitsOnly(control.value).length;
    return length === 0 || (length >= PHONE_MIN_DIGITS && length <= PHONE_MAX_DIGITS)
      ? null
      : { phone: true };
  };
}
