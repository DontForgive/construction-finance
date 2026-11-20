import { AbstractControl } from '@angular/forms';
import { digitsOnly } from './supplier-address';
import {
  PHONE_EMPTY_DISPLAY,
  PHONE_ERROR_MESSAGE,
  formatPhone,
  formatPhoneForDisplay,
  phoneValidator
} from './supplier-phone';

/** Executa o validador sobre um valor, como o Angular faria com o controle. */
function validar(valor: any) {
  return phoneValidator()({ value: valor } as AbstractControl);
}

describe('supplier-phone (telefone do fornecedor)', () => {

  it('CT-1.2.1 extrai somente os dígitos de um telefone com máscara', () => {
    expect(digitsOnly('(11) 90000-0001')).toBe('11900000001');
  });

  it('CT-1.2.2 aplica a máscara de celular (11 dígitos) como (XX) XXXXX-XXXX', () => {
    expect(formatPhone('11900000001')).toBe('(11) 90000-0001');
  });

  it('CT-1.2.3 aplica a máscara de fixo (10 dígitos) como (XX) XXXX-XXXX', () => {
    expect(formatPhone('1130000001')).toBe('(11) 3000-0001');
  });

  it('CT-1.2.4 limita a entrada a 11 dígitos: o 12º dígito não é aceito', () => {
    expect(formatPhone('119000000012')).toBe('(11) 90000-0001');
  });

  it('CT-1.2.5 aceita 10 dígitos (limite inferior) e 11 dígitos (limite superior)', () => {
    expect(validar('1130000001')).toBeNull();
    expect(validar('11900000001')).toBeNull();
  });

  it('CT-1.2.6 rejeita 9 dígitos, 12 dígitos e valor com letra, sem alterar o valor digitado', () => {
    const valorComLetra = '11a00000001';

    expect(validar('113000001')).toEqual({ phone: true });
    expect(validar('119000000012')).toEqual({ phone: true });
    expect(validar(valorComLetra)).toEqual({ phone: true });
    expect(valorComLetra).toBe('11a00000001');
  });

  it('CT-1.2.7 aceita campo vazio (opcional) pelo validador', () => {
    expect(validar('')).toBeNull();
    expect(validar(null)).toBeNull();
  });

  it('CT-1.2.8 exibe o travessão quando não há telefone e a máscara quando há', () => {
    expect(formatPhoneForDisplay(null)).toBe(PHONE_EMPTY_DISPLAY);
    expect(formatPhoneForDisplay('')).toBe(PHONE_EMPTY_DISPLAY);
    expect(formatPhoneForDisplay('11900000001')).toBe('(11) 90000-0001');
  });

  it('CT-1.2.6 usa a mensagem fixa de telefone inválido', () => {
    expect(PHONE_ERROR_MESSAGE).toBe('Informe um telefone válido com DDD (10 ou 11 dígitos).');
  });
});
