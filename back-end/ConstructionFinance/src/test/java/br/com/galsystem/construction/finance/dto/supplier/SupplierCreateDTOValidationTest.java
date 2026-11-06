package br.com.galsystem.construction.finance.dto.supplier;

import br.com.galsystem.construction.finance.support.JsonSupport;
import br.com.galsystem.construction.finance.support.SupplierTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static br.com.galsystem.construction.finance.support.SupplierTestData.repeat;
import static org.assertj.core.api.Assertions.assertThat;

class SupplierCreateDTOValidationTest {

    // CT-1.1.7 (CA1): sem campos de endereço, sem violações
    @Test
    void ct_1_1_7_dto_sem_endereco_passa_na_validacao() {
        final SupplierCreateDTO dto = SupplierTestData.create().name("Fornecedor Alfa").worker(true).build();

        assertThat(JsonSupport.validate(dto)).isEmpty();
        assertThat(dto.zipCode()).isNull();
        assertThat(dto.street()).isNull();
        assertThat(dto.state()).isNull();
        assertThat(dto.country()).isNull();
    }

    // CT-1.1.3 (CA2): endereço completo válido não gera violações e preserva valores
    @Test
    void ct_1_1_3_endereco_completo_valido_passa_e_preserva_valores() {
        final SupplierCreateDTO dto = SupplierTestData.create().fullAddress().build();

        assertThat(JsonSupport.validate(dto)).isEmpty();
        assertThat(dto.zipCode()).isEqualTo("01310100");
        assertThat(dto.street()).isEqualTo("Avenida Paulista");
        assertThat(dto.number()).isEqualTo("1000");
        assertThat(dto.complement()).isEqualTo("Sala 10");
        assertThat(dto.neighborhood()).isEqualTo("Bela Vista");
        assertThat(dto.city()).isEqualTo("Sao Paulo");
        assertThat(dto.state()).isEqualTo("SP");
        assertThat(dto.country()).isEqualTo("BR");
        assertThat(dto.reference()).isEqualTo("Proximo ao metro");
    }

    // CT-1.1.12 / CT-1.1.14 (CA3): CEP com e sem máscara termina com 8 dígitos
    @ParameterizedTest(name = "CT-1.1.12/14: CEP \"{0}\"")
    @ValueSource(strings = {"01310-100", "01310100"})
    void ct_1_1_12_14_cep_com_ou_sem_mascara_e_aceito_e_armazenado_sem_mascara(final String cep) {
        final SupplierCreateDTO dto = SupplierTestData.create().zipCode(cep).build();

        assertThat(JsonSupport.validate(dto)).isEmpty();
        assertThat(dto.zipCode()).isEqualTo("01310100");
    }

    // CT-1.1.15 (CA3/CA13): CEP "" ou "   " vira null
    @ParameterizedTest(name = "CT-1.1.15: CEP \"{0}\" vira null")
    @ValueSource(strings = {"", "   "})
    void ct_1_1_15_cep_vazio_vira_null(final String cep) {
        final SupplierCreateDTO dto = SupplierTestData.create().zipCode(cep).build();

        assertThat(dto.zipCode()).isNull();
        assertThat(JsonSupport.validate(dto)).isEmpty();
    }

    // CT-1.1.17, 19, 20, 21 (CA4): CEP com tamanho ou formato inválido é rejeitado em zipCode
    @ParameterizedTest(name = "CT-1.1.17/19/20/21: CEP \"{0}\" rejeitado")
    @ValueSource(strings = {"0131010", "013101001", "0131010A", "ABCDEFGH", "0131-010"})
    void ct_1_1_17_19_20_21_cep_invalido_e_rejeitado(final String cep) {
        final SupplierCreateDTO dto = SupplierTestData.create().zipCode(cep).build();

        assertThat(JsonSupport.violatedProperties(dto)).containsExactly("zipCode");
    }

    // CT-1.1.22 (CA4): 8 dígitos é aceito
    @Test
    void ct_1_1_22_cep_de_8_digitos_e_aceito() {
        assertThat(JsonSupport.validate(SupplierTestData.create().zipCode("01310100").build())).isEmpty();
    }

    // CT-1.1.23 (CA5): street 256 rejeitada com mensagem do limite 255
    @Test
    void ct_1_1_23_street_com_256_caracteres_e_rejeitada_informando_limite() {
        final SupplierCreateDTO dto = SupplierTestData.create().street(repeat(256)).build();

        assertThat(JsonSupport.validate(dto))
                .singleElement()
                .satisfies(v -> {
                    assertThat(v.getPropertyPath().toString()).isEqualTo("street");
                    assertThat(v.getMessage()).contains("255");
                });
    }

    // CT-1.1.25 a 30 (CA5): limites exatos aceitos, limite + 1 rejeitado
    @ParameterizedTest(name = "CT-1.1.25-30: {0} limite {1}")
    @CsvSource({
            "street,255",
            "number,20",
            "complement,150",
            "neighborhood,100",
            "city,120",
            "reference,255"
    })
    void ct_1_1_25_a_30_limite_de_tamanho_por_campo(final String campo, final int max) {
        assertThat(JsonSupport.validate(withText(campo, repeat(max)))).as(campo + " no limite").isEmpty();
        assertThat(JsonSupport.violatedProperties(withText(campo, repeat(max + 1))))
                .as(campo + " acima do limite").containsExactly(campo);
    }

    // CT-1.1.31 (CA5): dois campos acima do limite geram dois erros
    @Test
    void ct_1_1_31_mais_de_um_campo_acima_do_limite_reporta_todos() {
        final SupplierCreateDTO dto = SupplierTestData.create().street(repeat(256)).city(repeat(121)).build();

        assertThat(JsonSupport.violatedProperties(dto)).containsExactlyInAnyOrder("street", "city");
    }

    // CT-1.1.32 (CA5): todos os textos exatamente no limite são aceitos
    @Test
    void ct_1_1_32_todos_os_campos_de_texto_no_limite_sao_aceitos() {
        final SupplierCreateDTO dto = SupplierTestData.create()
                .street(repeat(255)).number(repeat(20)).complement(repeat(150))
                .neighborhood(repeat(100)).city(repeat(120)).reference(repeat(255)).build();

        assertThat(JsonSupport.validate(dto)).isEmpty();
        assertThat(dto.street()).hasSize(255);
        assertThat(dto.city()).hasSize(120);
    }

    // CT-1.1.33 (CA5): limite avaliado após o trim (comportamento ainda a confirmar com o time)
    @Test
    void ct_1_1_33_limite_e_avaliado_apos_o_trim() {
        final SupplierCreateDTO dto = SupplierTestData.create().street("  " + repeat(255) + "  ").build();

        assertThat(dto.street()).hasSize(255);
        assertThat(JsonSupport.validate(dto)).isEmpty();
    }

    // CT-1.1.34 / CT-1.1.38 (CA6): "mg" e " mg " viram "MG"
    @ParameterizedTest(name = "CT-1.1.34/38: UF \"{0}\"")
    @ValueSource(strings = {"mg", " mg ", "MG"})
    void ct_1_1_34_38_uf_e_normalizada_para_maiusculas(final String uf) {
        final SupplierCreateDTO dto = SupplierTestData.create().state(uf).build();

        assertThat(dto.state()).isEqualTo("MG");
        assertThat(JsonSupport.validate(dto)).isEmpty();
    }

    // CT-1.1.35 (CA6): "XX" rejeitada em state
    @Test
    void ct_1_1_35_uf_xx_e_rejeitada() {
        assertThat(JsonSupport.violatedProperties(SupplierTestData.create().state("XX").build()))
                .containsExactly("state");
    }

    // CT-1.1.40 (CA7): "br" vira "BR"
    @Test
    void ct_1_1_40_pais_br_minusculo_vira_maiusculo() {
        final SupplierCreateDTO dto = SupplierTestData.create().country("br").build();

        assertThat(dto.country()).isEqualTo("BR");
        assertThat(JsonSupport.validate(dto)).isEmpty();
    }

    // CT-1.1.41 (CA7): "BRA" rejeitado em country
    @Test
    void ct_1_1_41_pais_bra_e_rejeitado() {
        assertThat(JsonSupport.violatedProperties(SupplierTestData.create().country("BRA").build()))
                .containsExactly("country");
    }

    // CT-1.1.55 / CT-1.1.58 (CA13): todos os campos "" viram null e passam na validação
    @Test
    void ct_1_1_55_58_todos_os_campos_de_endereco_vazios_viram_null() {
        final SupplierCreateDTO dto = new SupplierCreateDTO("Construtora Beta", false, null,
                "", "", "", "", "", "", "", "", "");

        assertThat(JsonSupport.validate(dto)).isEmpty();
        assertThat(dto.zipCode()).isNull();
        assertThat(dto.street()).isNull();
        assertThat(dto.number()).isNull();
        assertThat(dto.complement()).isNull();
        assertThat(dto.neighborhood()).isNull();
        assertThat(dto.city()).isNull();
        assertThat(dto.state()).isNull();
        assertThat(dto.country()).isNull();
        assertThat(dto.reference()).isNull();
    }

    // CT-1.1.70 (RN10): regras de nome e worker não mudaram
    @Test
    void ct_1_1_70_regras_de_nome_e_worker_continuam_validas() {
        assertThat(JsonSupport.violatedProperties(SupplierTestData.create().worker(null).build()))
                .containsExactly("worker");
        assertThat(JsonSupport.violatedProperties(SupplierTestData.create().name("  ").build()))
                .contains("name");
        assertThat(JsonSupport.violatedProperties(SupplierTestData.create().name("A").build()))
                .containsExactly("name");
    }

    private static SupplierCreateDTO withText(final String campo, final String valor) {
        final SupplierTestData.Builder b = SupplierTestData.create();
        switch (campo) {
            case "street" -> b.street(valor);
            case "number" -> b.number(valor);
            case "complement" -> b.complement(valor);
            case "neighborhood" -> b.neighborhood(valor);
            case "city" -> b.city(valor);
            case "reference" -> b.reference(valor);
            default -> throw new IllegalArgumentException(campo);
        }
        return b.build();
    }
}
