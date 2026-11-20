import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { digitsOnly } from './supplier-address';

export const PHONE_MIN_DIGITS = 10;
export const PHONE_MAX_DIGITS = 11;
export const PHONE_ERROR_MESSAGE = 'Informe um telefone válido com DDD (10 ou 11 dígitos).';
export const PHONE_EMPTY_DISPLAY = '—';

/** Caracteres aceitos na digitação: dígitos, espaço, parênteses e hífen (usados pela máscara). */
const PHONE_ALLOWED_CHARS = /^[\d\s()-]*$/;

/**
 * Máscara visual do telefone enquanto o usuário digita.
 * Até 10 dígitos: (XX) XXXX-XXXX. Com 11 dígitos: (XX) XXXXX-XXXX.
 * Não remove nem corta nada: se houver letra, símbolo ou mais de 11 dígitos,
 * devolve o texto como foi digitado para que o validador o rejeite com mensagem.
 */
export function formatPhone(value: string | null | undefined): string {
  const raw = value ?? '';
  if (!PHONE_ALLOWED_CHARS.test(raw)) {
    return raw;
  }
  const digits = digitsOnly(raw);
  if (digits.length === 0 || digits.length > PHONE_MAX_DIGITS) {
    return raw;
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
export function formatPhoneForDisplay(value: string | null | undefined): string {
  return digitsOnly(value) === '' ? PHONE_EMPTY_DISPLAY : formatPhone(value);
}

/**
 * Validador: vazio é válido. Preenchido precisa conter só dígitos, espaço, parênteses e hífen,
 * com 10 ou 11 dígitos. Letras, sinais (como "+") e 12 dígitos são rejeitados.
 */
export function phoneValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const raw = String(control.value ?? '');
    if (raw === '') {
      return null;
    }
    if (!PHONE_ALLOWED_CHARS.test(raw)) {
      return { phone: true };
    }
    const length = digitsOnly(raw).length;
    return length === 0 || (length >= PHONE_MIN_DIGITS && length <= PHONE_MAX_DIGITS)
      ? null
      : { phone: true };
  };
}
