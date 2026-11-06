package br.com.galsystem.construction.finance.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AddressNormalizerTest {

    // CT-1.1.13 (CA3): normalização do CEP
    @ParameterizedTest(name = "CT-1.1.13: \"{0}\" -> \"{1}\"")
    @CsvSource({
            "01310-100,01310100",
            "01.310-100,01310100",
            "01310100,01310100"
    })
    void ct_1_1_13_cep_com_ou_sem_mascara_fica_so_com_digitos(final String entrada, final String esperado) {
        assertThat(AddressNormalizer.zipCode(entrada)).isEqualTo(esperado);
    }

    // CT-1.1.21 (CA4): "0131-010" tem 7 dígitos; a normalização não pode transformá-lo em um CEP válido
    @Test
    void ct_1_1_21_cep_mascarado_com_7_digitos_nao_vira_cep_de_8_digitos() {
        final String normalizado = AddressNormalizer.zipCode("0131-010");
        assertThat(normalizado).isNotNull();
        assertThat(normalizado.replaceAll("\\D", "")).hasSize(7);
        assertThat(normalizado).doesNotMatch("\\d{8}");
    }

    // CT-1.1.19 / CT-1.1.20 (CA4): valores de tamanho/formato errado são mantidos para a validação rejeitar
    @ParameterizedTest(name = "CT-1.1.19/20: \"{0}\" não vira 8 dígitos")
    @ValueSource(strings = {"0131010", "013101001", "0131010A", "ABCDEFGH"})
    void ct_1_1_19_20_cep_invalido_nao_e_corrigido_pela_normalizacao(final String entrada) {
        assertThat(AddressNormalizer.zipCode(entrada)).doesNotMatch("\\d{8}");
    }

    // CT-1.1.58 (CA13): "", "   " e null viram null
    @ParameterizedTest(name = "CT-1.1.58: text(\"{0}\") = null")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void ct_1_1_58_vazio_em_branco_e_nulo_viram_null(final String entrada) {
        assertThat(AddressNormalizer.text(entrada)).isNull();
        assertThat(AddressNormalizer.upper(entrada)).isNull();
        assertThat(AddressNormalizer.zipCode(entrada)).isNull();
    }

    // CT-1.1.58 / CT-1.1.38: trim e maiúsculas
    @Test
    void ct_1_1_38_trim_e_maiusculas_para_uf_e_pais() {
        assertThat(AddressNormalizer.upper(" mg ")).isEqualTo("MG");
        assertThat(AddressNormalizer.upper("br")).isEqualTo("BR");
        assertThat(AddressNormalizer.text("  Rua A  ")).isEqualTo("Rua A");
    }
}
