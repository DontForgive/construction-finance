package br.com.galsystem.construction.finance.dto.supplier;

import br.com.galsystem.construction.finance.support.JsonSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static br.com.galsystem.construction.finance.support.SupplierTestData.ADDRESS_FIELDS;
import static br.com.galsystem.construction.finance.support.SupplierTestData.repeat;
import static org.assertj.core.api.Assertions.assertThat;

/** Desserialização JSON real (Jackson) e validação do DTO de atualização. */
class SupplierUpdateDTOTest {

    private static final String BASE = "\"name\":\"Construtora Beta\",\"worker\":false";

    private static SupplierUpdateDTO json(final String extra) {
        return JsonSupport.updateFromJson("{" + BASE + (extra.isEmpty() ? "" : "," + extra) + "}");
    }

    // CT-1.1.51 (CA10/CA11): distingue ausente, null, vazio e valor
    @Test
    void ct_1_1_51_distingue_ausente_null_vazio_e_valor() {
        final SupplierUpdateDTO dto = json("\"street\":null,\"city\":\"\",\"state\":\"mg\"");

        // ausente
        assertThat(dto.isPresent(SupplierUpdateDTO.NEIGHBORHOOD)).isFalse();
        // nulo explícito
        assertThat(dto.isPresent(SupplierUpdateDTO.STREET)).isTrue();
        assertThat(dto.getStreet()).isNull();
        // vazio explícito: presente e normalizado para null
        assertThat(dto.isPresent(SupplierUpdateDTO.CITY)).isTrue();
        assertThat(dto.getCity()).isNull();
        // valor
        assertThat(dto.isPresent(SupplierUpdateDTO.STATE)).isTrue();
        assertThat(dto.getState()).isEqualTo("MG");
    }

    // CT-1.1.51 / CT-1.1.46: sem nenhum campo de endereço nenhum campo é presente
    @Test
    void ct_1_1_51_46_sem_campos_de_endereco_nenhum_e_presente() {
        final SupplierUpdateDTO dto = json("");

        for (final String campo : ADDRESS_FIELDS) {
            assertThat(dto.isPresent(campo)).as(campo).isFalse();
        }
        assertThat(JsonSupport.validate(dto)).isEmpty();
    }

    // CT-1.1.53 (CA12): null explícito marca presença de cada um dos 9 campos
    @ParameterizedTest(name = "CT-1.1.53: {0} = null é presente")
    @ValueSource(strings = {"zipCode", "street", "number", "complement", "neighborhood", "city", "state", "country",
            "reference"})
    void ct_1_1_53_null_explicito_em_cada_campo_e_presente_e_nulo(final String campo) {
        final SupplierUpdateDTO dto = json("\"" + campo + "\":null");

        for (final String outro : ADDRESS_FIELDS) {
            assertThat(dto.isPresent(outro)).as(outro).isEqualTo(outro.equals(campo));
        }
        assertThat(JsonSupport.validate(dto)).isEmpty();
    }

    // CT-1.1.16 (CA3): CEP com máscara no PUT vira 8 dígitos
    @Test
    void ct_1_1_16_cep_com_mascara_no_put_fica_so_com_digitos() {
        final SupplierUpdateDTO dto = json("\"zipCode\":\"01310-100\"");

        assertThat(dto.getZipCode()).isEqualTo("01310100");
        assertThat(JsonSupport.validate(dto)).isEmpty();
    }

    // CT-1.1.18 / CT-1.1.21 (CA4): CEP de 7 dígitos no PUT é rejeitado
    @ParameterizedTest(name = "CT-1.1.18/21: CEP \"{0}\" rejeitado no PUT")
    @ValueSource(strings = {"0131010", "0131-010", "013101001", "0131010A"})
    void ct_1_1_18_21_cep_invalido_no_put_e_rejeitado(final String cep) {
        assertThat(JsonSupport.violatedProperties(json("\"zipCode\":\"" + cep + "\"")))
                .containsExactly("zipCode");
    }

    // CT-1.1.24 (CA5): street 256 no PUT é rejeitada
    @Test
    void ct_1_1_24_street_com_256_no_put_e_rejeitada() {
        assertThat(JsonSupport.violatedProperties(json("\"street\":\"" + repeat(256) + "\"")))
                .containsExactly("street");
    }

    // CT-1.1.25 a 30 (CA5): limites no PUT
    @ParameterizedTest(name = "CT-1.1.25-30 (PUT): {0} limite {1}")
    @CsvSource({
            "street,255",
            "number,20",
            "complement,150",
            "neighborhood,100",
            "city,120",
            "reference,255"
    })
    void ct_1_1_25_a_30_limites_no_put(final String campo, final int max) {
        assertThat(JsonSupport.validate(json("\"" + campo + "\":\"" + repeat(max) + "\""))).isEmpty();
        assertThat(JsonSupport.violatedProperties(json("\"" + campo + "\":\"" + repeat(max + 1) + "\"")))
                .containsExactly(campo);
    }

    // CT-1.1.39 / CT-1.1.44 (CA6/CA7): UF e país inválidos no PUT
    @Test
    void ct_1_1_39_44_uf_e_pais_invalidos_no_put_sao_rejeitados() {
        assertThat(JsonSupport.violatedProperties(json("\"state\":\"XX\""))).containsExactly("state");
        assertThat(JsonSupport.violatedProperties(json("\"country\":\"BRA\""))).containsExactly("country");
    }

    // CT-1.1.57 (CA13): "" em CEP, UF e país limpa sem violação (vazio não é validado como valor)
    @Test
    void ct_1_1_57_vazio_em_cep_uf_e_pais_nao_gera_violacao() {
        final SupplierUpdateDTO dto = json("\"zipCode\":\"\",\"state\":\"\",\"country\":\"\"");

        assertThat(JsonSupport.validate(dto)).isEmpty();
        assertThat(dto.isPresent(SupplierUpdateDTO.ZIP_CODE)).isTrue();
        assertThat(dto.isPresent(SupplierUpdateDTO.STATE)).isTrue();
        assertThat(dto.isPresent(SupplierUpdateDTO.COUNTRY)).isTrue();
        assertThat(dto.getZipCode()).isNull();
        assertThat(dto.getState()).isNull();
        assertThat(dto.getCountry()).isNull();
    }

    // CT-1.1.58 (CA13): "", "   " e null viram null
    @ParameterizedTest(name = "CT-1.1.58 (PUT): street={0}")
    @CsvSource(value = {"\"\"", "\"   \"", "null"})
    void ct_1_1_58_vazio_em_branco_e_nulo_viram_null_no_put(final String valorJson) {
        final SupplierUpdateDTO dto = json("\"street\":" + valorJson);

        assertThat(dto.getStreet()).isNull();
        assertThat(dto.isPresent(SupplierUpdateDTO.STREET)).isTrue();
    }

    // CT-1.1.70 (RN10): nome e worker continuam obrigatórios na edição
    @Test
    void ct_1_1_70_nome_e_worker_continuam_obrigatorios_no_put() {
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson("{\"name\":\"A\"}");

        assertThat(JsonSupport.violatedProperties(dto)).containsExactlyInAnyOrder("name", "worker");
    }
}
