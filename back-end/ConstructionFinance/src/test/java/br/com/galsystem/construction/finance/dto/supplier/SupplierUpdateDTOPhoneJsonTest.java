package br.com.galsystem.construction.finance.dto.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nível de desserialização JSON do campo phone em SupplierUpdateDTO.
 * Cobre a distinção omitido / null / vazio / valor que o PUT usa. Não substitui
 * o teste funcional de API com banco (CT-1.1.25 a CT-1.1.31), que segue pendente.
 */
class SupplierUpdateDTOPhoneJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private CampoOpcional<String> phoneDoJson(String json) throws Exception {
        return mapper.readValue(json, SupplierUpdateDTO.class).phone();
    }

    @Test
    @DisplayName("CT-1.1.26 (nível JSON) PUT sem o campo phone devolve campo ausente, para manter o telefone")
    void ct_1_1_26_json_sem_phone_e_ausente() throws Exception {
        CampoOpcional<String> phone = phoneDoJson("{\"name\":\"Fornecedor Teste Alfa\",\"worker\":true}");

        assertThat(phone.isPresente()).isFalse();
    }

    @Test
    @DisplayName("CT-1.1.27 (nível JSON) PUT com phone null devolve campo presente com valor nulo, para apagar")
    void ct_1_1_27_json_phone_null_e_presente_nulo() throws Exception {
        CampoOpcional<String> phone = phoneDoJson("{\"name\":\"Fornecedor Teste Alfa\",\"phone\":null}");

        assertThat(phone.isPresente()).isTrue();
        assertThat(phone.valor()).isNull();
    }

    @Test
    @DisplayName("CT-1.1.28 (nível JSON) PUT com phone vazio devolve campo presente com string vazia, para apagar no serviço")
    void ct_1_1_28_json_phone_vazio_e_presente_vazio() throws Exception {
        CampoOpcional<String> phone = phoneDoJson("{\"name\":\"Fornecedor Teste Alfa\",\"phone\":\"\"}");

        assertThat(phone.isPresente()).isTrue();
        assertThat(phone.valor()).isEmpty();
    }

    @Test
    @DisplayName("CT-1.1.29 (nível JSON) PUT com phone mascarado preserva o valor enviado para normalização no serviço")
    void ct_1_1_29_json_phone_com_mascara_preserva_valor() throws Exception {
        CampoOpcional<String> phone = phoneDoJson("{\"name\":\"Fornecedor Teste Alfa\",\"phone\":\"(21) 3456-7890\"}");

        assertThat(phone.isPresente()).isTrue();
        assertThat(phone.valor()).isEqualTo("(21) 3456-7890");
    }
}
