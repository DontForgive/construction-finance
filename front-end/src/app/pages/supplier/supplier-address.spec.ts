import {
  enderecoLinhaPrincipal,
  enderecoLinhaSecundaria,
  formatCep,
  formatCepForDisplay
} from './supplier-address';

/** DS1 (completo) dos casos de teste aprovados da HU-1.1. */
const DS1 = {
  zipCode: '13010000',
  street: 'Rua das Flores',
  number: '120',
  complement: 'sala 3',
  neighborhood: 'Centro',
  city: 'Campinas',
  state: 'SP'
};

/** DS2 (parcial): sem número e sem complemento. */
const DS2 = { ...DS1, number: null, complement: null };

/** DS3 (só cidade). */
const DS3 = {
  zipCode: null,
  street: null,
  number: null,
  complement: null,
  neighborhood: null,
  city: 'Campinas',
  state: null
};

/** DS4 (vazio): só nome, como em findOrCreateByName. */
const DS4 = {
  zipCode: null,
  street: null,
  number: null,
  complement: null,
  neighborhood: null,
  city: null,
  state: null
};

describe('supplier-address (coluna Endereço)', () => {

  it('CT-1.1.5 monta a linha 1 como cidade/UF e a linha 2 com logradouro, número, complemento, bairro e CEP com máscara', () => {
    expect(enderecoLinhaPrincipal(DS1)).toBe('Campinas/SP');
    expect(enderecoLinhaSecundaria(DS1)).toBe('Rua das Flores, 120, sala 3 · Centro · 13010-000');
  });

  it('CT-1.1.6 omite o número vazio na linha 2, sem vírgula nem separador duplicado', () => {
    expect(enderecoLinhaSecundaria(DS2)).toBe('Rua das Flores, sala 3 · Centro · 13010-000');
  });

  it('CT-1.1.7 mostra só a cidade na linha 1, sem barra nem UF, e omite a linha 2 quando vazia', () => {
    expect(enderecoLinhaPrincipal(DS3)).toBe('Campinas');
    expect(enderecoLinhaSecundaria(DS3)).toBe('');
  });

  it('CT-1.1.8 devolve célula em branco nas duas linhas quando o fornecedor não tem endereço', () => {
    expect(enderecoLinhaPrincipal(DS4)).toBe('');
    expect(enderecoLinhaSecundaria(DS4)).toBe('');
  });

  it('CT-1.1.9 formata CEP de 8 dígitos com máscara 00000-000', () => {
    expect(formatCep('13010000')).toBe('13010-000');
    expect(formatCepForDisplay('13010000')).toBe('13010-000');
  });

  it('CT-1.1.9 exibe CEP de 7 dígitos como está (comportamento proposto, a validar)', () => {
    expect(formatCepForDisplay('1301000')).toBe('1301000');
  });

  it('CT-1.1.10 mantém logradouro, número e complemento completos, sem máscara nem ocultação', () => {
    const linha = enderecoLinhaSecundaria(DS1);
    expect(linha).toContain('Rua das Flores, 120, sala 3');
    expect(linha).not.toMatch(/\*/);
  });

  it('CT-1.1.18 mantém o logradouro longo inteiro no texto da célula (truncamento é visual)', () => {
    const longo = 'R'.repeat(200);
    const complementoLongo = 'C'.repeat(140);
    const linha = enderecoLinhaSecundaria({ ...DS1, street: longo, complement: complementoLongo });
    expect(linha).toContain(longo);
    expect(linha).toContain(complementoLongo);
  });
});
