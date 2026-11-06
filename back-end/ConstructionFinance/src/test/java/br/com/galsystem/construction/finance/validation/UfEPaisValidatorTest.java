package br.com.galsystem.construction.finance.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UfEPaisValidatorTest {

    private static final List<String> UFS = List.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private final UfValidator ufValidator = new UfValidator();
    private final PaisValidator paisValidator = new PaisValidator();

    // CT-1.1.36 (CA6): as 27 UFs, em maiúsculas e minúsculas
    @Test
    void ct_1_1_36_aceita_as_27_ufs_em_maiusculas_e_minusculas() {
        assertThat(UFS).hasSize(27);
        for (final String uf : UFS) {
            assertThat(ufValidator.isValid(uf, null)).as("UF " + uf).isTrue();
            assertThat(ufValidator.isValid(uf.toLowerCase(), null)).as("UF " + uf.toLowerCase()).isTrue();
        }
    }

    // CT-1.1.37 (CA6): rejeita 1 caractere, 3 caracteres e números; CT-1.1.35: "XX"
    @ParameterizedTest(name = "CT-1.1.35/37: UF \"{0}\" rejeitada")
    @ValueSource(strings = {"M", "MGG", "12", "XX"})
    void ct_1_1_35_37_rejeita_uf_invalida(final String uf) {
        assertThat(ufValidator.isValid(uf, null)).isFalse();
    }

    // O vazio não é validado como valor (CT-1.1.57); null é aceito (campo opcional)
    @Test
    void ct_1_1_57_uf_e_pais_nulos_sao_aceitos_por_serem_opcionais() {
        assertThat(ufValidator.isValid(null, null)).isTrue();
        assertThat(paisValidator.isValid(null, null)).isTrue();
    }

    // CT-1.1.42 (CA7): aceita BR, US, PT, JP
    @ParameterizedTest(name = "CT-1.1.42: país \"{0}\" aceito")
    @ValueSource(strings = {"BR", "US", "PT", "JP"})
    void ct_1_1_42_aceita_paises_validos(final String pais) {
        assertThat(paisValidator.isValid(pais, null)).isTrue();
    }

    // CT-1.1.42 (CA7): rejeita ZZ, B, BRA, 1A
    @ParameterizedTest(name = "CT-1.1.42/41: país \"{0}\" rejeitado")
    @ValueSource(strings = {"ZZ", "B", "BRA", "1A"})
    void ct_1_1_42_rejeita_paises_invalidos(final String pais) {
        assertThat(paisValidator.isValid(pais, null)).isFalse();
    }

    // CT-1.1.43 (CA7): extremidades da lista ISO 3166-1 alpha-2
    @Test
    void ct_1_1_43_cobre_extremidades_da_lista_iso() {
        assertThat(paisValidator.isValid("AD", null)).isTrue();
        assertThat(paisValidator.isValid("ZW", null)).isTrue();
    }
}
