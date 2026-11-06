import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export interface SelectOption {
  code: string;
  name: string;
}

/** Campos de endereço do fornecedor (nomes iguais aos da API). */
export const ADDRESS_FIELDS = [
  'zipCode',
  'street',
  'number',
  'complement',
  'neighborhood',
  'city',
  'state',
  'country',
  'reference'
] as const;

/** Tamanhos máximos dos campos de texto livre de endereço. */
export const ADDRESS_MAX_LENGTH = {
  street: 255,
  number: 20,
  complement: 150,
  neighborhood: 100,
  city: 120,
  reference: 255
};

export const CEP_DIGITS = 8;

export const UF_OPTIONS: SelectOption[] = [
  { code: 'AC', name: 'Acre' },
  { code: 'AL', name: 'Alagoas' },
  { code: 'AP', name: 'Amapá' },
  { code: 'AM', name: 'Amazonas' },
  { code: 'BA', name: 'Bahia' },
  { code: 'CE', name: 'Ceará' },
  { code: 'DF', name: 'Distrito Federal' },
  { code: 'ES', name: 'Espírito Santo' },
  { code: 'GO', name: 'Goiás' },
  { code: 'MA', name: 'Maranhão' },
  { code: 'MT', name: 'Mato Grosso' },
  { code: 'MS', name: 'Mato Grosso do Sul' },
  { code: 'MG', name: 'Minas Gerais' },
  { code: 'PA', name: 'Pará' },
  { code: 'PB', name: 'Paraíba' },
  { code: 'PR', name: 'Paraná' },
  { code: 'PE', name: 'Pernambuco' },
  { code: 'PI', name: 'Piauí' },
  { code: 'RJ', name: 'Rio de Janeiro' },
  { code: 'RN', name: 'Rio Grande do Norte' },
  { code: 'RS', name: 'Rio Grande do Sul' },
  { code: 'RO', name: 'Rondônia' },
  { code: 'RR', name: 'Roraima' },
  { code: 'SC', name: 'Santa Catarina' },
  { code: 'SP', name: 'São Paulo' },
  { code: 'SE', name: 'Sergipe' },
  { code: 'TO', name: 'Tocantins' }
];

/** Códigos ISO 3166-1 alpha-2 (249). */
const COUNTRY_CODES: string[] = (
  'AD AE AF AG AI AL AM AO AQ AR AS AT AU AW AX AZ BA BB BD BE BF BG BH BI BJ BL BM BN BO BQ BR BS BT BV BW BY BZ ' +
  'CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CW CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FO FR ' +
  'GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IM IN IO IQ IR IS IT JE JM JO JP ' +
  'KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MF MG MH MK ML MM MN MO MP MQ MR MS MT ' +
  'MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RE RO RS RU RW ' +
  'SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR SS ST SV SX SY SZ TC TD TF TG TH TJ TK TL TM TN TO TR TT TV TW TZ ' +
  'UA UG UM US UY UZ VA VC VE VG VI VN VU WF WS YE YT ZA ZM ZW'
).split(' ');

function buildCountryOptions(): SelectOption[] {
  let displayNames: { of(code: string): string | undefined } | null = null;
  try {
    const DisplayNames = (Intl as any).DisplayNames;
    displayNames = DisplayNames ? new DisplayNames(['pt-BR'], { type: 'region' }) : null;
  } catch {
    displayNames = null;
  }

  return COUNTRY_CODES
    .map(code => {
      let name: string | undefined;
      try {
        name = displayNames?.of(code);
      } catch {
        name = undefined;
      }
      return { code, name: name || code };
    })
    .sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));
}

export const COUNTRY_OPTIONS: SelectOption[] = buildCountryOptions();

/** Mantém somente dígitos. */
export function digitsOnly(value: any): string {
  return (value ?? '').toString().replace(/\D/g, '');
}

/** Máscara visual do CEP: 00000-000. */
export function formatCep(value: any): string {
  const digits = digitsOnly(value).slice(0, CEP_DIGITS);
  return digits.length > 5 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : digits;
}

/** Texto sem espaços nas bordas; vazio vira null. */
export function textOrNull(value: any): string | null {
  if (value === null || value === undefined) {
    return null;
  }
  const trimmed = value.toString().trim();
  return trimmed === '' ? null : trimmed;
}

/** Validador: o nome não pode ser só espaços. */
export function notBlank(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (typeof value === 'string' && value.length > 0 && value.trim() === '') {
      return { required: true };
    }
    return null;
  };
}

/** Validador de tamanho máximo considerando o texto sem espaços nas bordas. */
export function maxTrimmedLength(max: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (typeof value !== 'string') {
      return null;
    }
    const length = value.trim().length;
    return length > max ? { maxlength: { requiredLength: max, actualLength: length } } : null;
  };
}

/** Validador do CEP: vazio é válido; preenchido precisa ter 8 dígitos. */
export function cepValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const digits = digitsOnly(control.value);
    return digits.length === 0 || digits.length === CEP_DIGITS ? null : { cep: true };
  };
}

export interface ParsedServerErrors {
  /** Mensagens por campo (primeira mensagem de cada campo). */
  fields: { [field: string]: string };
  /** Entradas sem campo mapeável, para mensagem geral. */
  general: string[];
}

/**
 * Interpreta `erros` ("campo: mensagem") devolvido pela API.
 * Entradas cujo campo não está em `knownFields` (ou sem ":") vão para `general`.
 */
export function parseServerErrors(erros: any, knownFields: readonly string[]): ParsedServerErrors {
  const result: ParsedServerErrors = { fields: {}, general: [] };
  if (!Array.isArray(erros)) {
    return result;
  }

  for (const entry of erros) {
    if (typeof entry !== 'string' || entry.trim() === '') {
      continue;
    }
    const separator = entry.indexOf(':');
    if (separator > 0) {
      const rawField = entry.substring(0, separator).trim();
      const field = rawField.substring(rawField.lastIndexOf('.') + 1);
      const message = entry.substring(separator + 1).trim();
      if (knownFields.indexOf(field) >= 0 && message) {
        if (!result.fields[field]) {
          result.fields[field] = message;
        }
        continue;
      }
    }
    result.general.push(entry.trim());
  }
  return result;
}
