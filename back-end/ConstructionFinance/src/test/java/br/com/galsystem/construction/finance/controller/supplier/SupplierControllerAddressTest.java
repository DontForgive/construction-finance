package br.com.galsystem.construction.finance.controller.supplier;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.exception.ApiExceptionHandler;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.service.supplier.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static br.com.galsystem.construction.finance.support.SupplierTestData.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de contrato HTTP do controller com o {@link ApiExceptionHandler} real e Bean Validation real.
 * O serviço é simulado: não comprovam persistência nem integração com banco.
 */
@ExtendWith(MockitoExtension.class)
class SupplierControllerAddressTest {

    @Mock
    private SupplierService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SupplierController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private static String body(final String extra) {
        return "{\"name\":\"Construtora Beta\",\"worker\":false" + (extra.isEmpty() ? "" : "," + extra) + "}";
    }

    // CT-1.1.1 (CA1): POST sem endereço -> 201 com SupplierDTO, sem campos de endereço
    @Test
    void ct_1_1_1_4_post_sem_endereco_retorna_201_e_supplier_dto() throws Exception {
        when(service.create(any(SupplierCreateDTO.class)))
                .thenReturn(new SupplierDTO(5L, "Fornecedor Alfa", "", true));

        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fornecedor Alfa\",\"worker\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("Fornecedor Alfa"))
                .andExpect(jsonPath("$.data.worker").value(true))
                .andExpect(jsonPath("$.data.street").doesNotExist())
                .andExpect(jsonPath("$.data.zipCode").doesNotExist());
    }

    // CT-1.1.3 (CA2): GET /supplier/{id} devolve os 9 campos de endereço
    @Test
    void ct_1_1_3_get_por_id_devolve_endereco_completo() throws Exception {
        when(service.findById(1L)).thenReturn(new SupplierDetailDTO(1L, "Construtora Beta", "pix", false,
                "01310100", "Avenida Paulista", "1000", "Sala 10", "Bela Vista", "Sao Paulo", "SP", "BR",
                "Proximo ao metro"));

        mvc.perform(get("/supplier/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zipCode").value("01310100"))
                .andExpect(jsonPath("$.data.street").value("Avenida Paulista"))
                .andExpect(jsonPath("$.data.number").value("1000"))
                .andExpect(jsonPath("$.data.complement").value("Sala 10"))
                .andExpect(jsonPath("$.data.neighborhood").value("Bela Vista"))
                .andExpect(jsonPath("$.data.city").value("Sao Paulo"))
                .andExpect(jsonPath("$.data.state").value("SP"))
                .andExpect(jsonPath("$.data.country").value("BR"))
                .andExpect(jsonPath("$.data.reference").value("Proximo ao metro"));
    }

    // CT-1.1.4 (P2): PUT devolve SupplierDTO sem endereço
    @Test
    void ct_1_1_4_put_retorna_200_e_supplier_dto_sem_endereco() throws Exception {
        when(service.update(eq(1L), any(SupplierUpdateDTO.class)))
                .thenReturn(new SupplierDTO(1L, "Construtora Beta", "pix", false));

        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON).content(body("\"city\":\"X\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Construtora Beta"))
                .andExpect(jsonPath("$.data.city").doesNotExist());
    }

    // CT-1.1.10 (CA1/CA2): campos de endereço nulos aparecem como null (chave presente)
    @Test
    void ct_1_1_10_get_de_fornecedor_antigo_devolve_nove_campos_null() throws Exception {
        when(service.findById(2L)).thenReturn(new SupplierDetailDTO(2L, "Fornecedor Antigo Ltda", "", true,
                null, null, null, null, null, null, null, null, null));

        mvc.perform(get("/supplier/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zipCode").isEmpty())
                .andExpect(jsonPath("$.data.street").isEmpty())
                .andExpect(jsonPath("$.data.state").isEmpty())
                .andExpect(jsonPath("$.data.country").isEmpty())
                .andExpect(jsonPath("$.data.reference").isEmpty());
    }

    // CT-1.1.11: listagem continua com SupplierDTO e size limitado a 100
    @Test
    void ct_1_1_11_listagem_sem_endereco_e_size_maximo_100() throws Exception {
        when(service.listar(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(new SupplierDTO(1L, "Construtora Beta", "pix", false))));

        mvc.perform(get("/supplier").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Construtora Beta"))
                .andExpect(jsonPath("$.data.content[0].street").doesNotExist());

        final ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(service).listar(any(), any(), any(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    // CT-1.1.17 (CA4): POST com CEP de 7 dígitos -> 400 com "zipCode: ..."; serviço não chamado
    @Test
    void ct_1_1_17_post_com_cep_de_7_digitos_retorna_400_e_nao_cria() throws Exception {
        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON).content(body("\"zipCode\":\"0131010\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("zipCode: "))));

        verifyNoInteractions(service);
    }

    // CT-1.1.18 (CA4): PUT com CEP de 7 dígitos -> 400; serviço não chamado
    @Test
    void ct_1_1_18_put_com_cep_de_7_digitos_retorna_400_e_nao_altera() throws Exception {
        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON).content(body("\"zipCode\":\"0131010\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("zipCode: "))));

        verifyNoInteractions(service);
    }

    // CT-1.1.22 (CA4): CEP de 8 dígitos válido continua aceito
    @Test
    void ct_1_1_22_post_com_cep_de_8_digitos_e_aceito() throws Exception {
        when(service.create(any())).thenReturn(new SupplierDTO(1L, "Construtora Beta", "", false));

        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON).content(body("\"zipCode\":\"01310100\"")))
                .andExpect(status().isCreated());
    }

    // CT-1.1.12 (CA3): CEP com máscara chega ao serviço com 8 dígitos
    @Test
    void ct_1_1_12_post_com_cep_mascarado_chega_ao_servico_sem_mascara() throws Exception {
        when(service.create(any())).thenReturn(new SupplierDTO(1L, "Construtora Beta", "", false));

        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON).content(body("\"zipCode\":\"01310-100\"")))
                .andExpect(status().isCreated());

        final ArgumentCaptor<SupplierCreateDTO> captor = ArgumentCaptor.forClass(SupplierCreateDTO.class);
        verify(service).create(captor.capture());
        assertThat(captor.getValue().zipCode()).isEqualTo("01310100");
    }

    // CT-1.1.23 (CA5): POST street 256 -> 400 informando 255
    @Test
    void ct_1_1_23_post_com_street_de_256_retorna_400_informando_limite() throws Exception {
        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"street\":\"" + repeat(256) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("street: "))))
                .andExpect(jsonPath("$.erros[0]").value(org.hamcrest.Matchers.containsString("255")));

        verify(service, never()).create(any());
    }

    // CT-1.1.24 (CA5): PUT street 256 -> 400
    @Test
    void ct_1_1_24_put_com_street_de_256_retorna_400() throws Exception {
        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"street\":\"" + repeat(256) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("street: "))));

        verify(service, never()).update(any(), any());
    }

    // CT-1.1.31 (CA5): dois campos acima do limite -> dois erros
    @Test
    void ct_1_1_31_post_com_dois_campos_acima_do_limite_lista_os_dois() throws Exception {
        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"street\":\"" + repeat(256) + "\",\"city\":\"" + repeat(121) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("street: "))))
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("city: "))))
                .andExpect(jsonPath("$.erros.length()").value(2));
    }

    // CT-1.1.35 / CT-1.1.39 (CA6): UF "XX" -> 400 em POST e PUT
    @Test
    void ct_1_1_35_39_uf_xx_retorna_400_em_post_e_put() throws Exception {
        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON).content(body("\"state\":\"XX\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("state: "))));
        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON).content(body("\"state\":\"XX\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("state: "))));

        verifyNoInteractions(service);
    }

    // CT-1.1.41 / CT-1.1.44 (CA7): país "BRA" -> 400 em POST e PUT
    @Test
    void ct_1_1_41_44_pais_bra_retorna_400_em_post_e_put() throws Exception {
        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON).content(body("\"country\":\"BRA\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("country: "))));
        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON).content(body("\"country\":\"BRA\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("country: "))));

        verifyNoInteractions(service);
    }

    // CT-1.1.34 / CT-1.1.40 (CA6/CA7): "mg" e "br" chegam ao serviço normalizados
    @Test
    void ct_1_1_34_40_uf_e_pais_minusculos_chegam_normalizados() throws Exception {
        when(service.create(any())).thenReturn(new SupplierDTO(1L, "Construtora Beta", "", false));

        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"state\":\"mg\",\"country\":\"br\"")))
                .andExpect(status().isCreated());

        final ArgumentCaptor<SupplierCreateDTO> captor = ArgumentCaptor.forClass(SupplierCreateDTO.class);
        verify(service).create(captor.capture());
        assertThat(captor.getValue().state()).isEqualTo("MG");
        assertThat(captor.getValue().country()).isEqualTo("BR");
    }

    // CT-1.1.60 (CA11): PUT com um campo inválido -> 400 e nada é aplicado
    @Test
    void ct_1_1_60_put_com_campo_invalido_junto_de_campo_valido_retorna_400_sem_alterar() throws Exception {
        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"city\":\"Valida\",\"state\":\"XX\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("state: "))));

        verify(service, never()).update(any(), any());
    }

    // CT-1.1.61 / CT-1.1.63 (CA14): conflito -> 409 como hoje
    @Test
    void ct_1_1_61_63_conflito_de_nome_retorna_409() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Já existe"));
        when(service.update(eq(1L), any())).thenThrow(new ConflictException("Já existe"));

        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON).content(body("\"city\":\"X\"")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON).content(body("\"city\":\"X\"")))
                .andExpect(status().isConflict());
    }

    // CT-1.1.70 (RN10): worker ausente e nome inválido -> 400 como antes
    @ParameterizedTest(name = "CT-1.1.70: {0}")
    @ValueSource(strings = {"{\"name\":\"Construtora Beta\"}", "{\"name\":\" \",\"worker\":true}",
            "{\"name\":\"A\",\"worker\":true}"})
    void ct_1_1_70_post_com_nome_ou_worker_invalido_retorna_400(final String json) throws Exception {
        mvc.perform(post("/supplier").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // CT-1.1.71 (RN10): PUT em ID inexistente -> 404
    @Test
    void ct_1_1_71_put_em_id_inexistente_retorna_404() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new NotFoundException("Fornecedor com o ID 99 não encontrado"));

        mvc.perform(put("/supplier/99").contentType(MediaType.APPLICATION_JSON).content(body("\"city\":\"X\"")))
                .andExpect(status().isNotFound());
        mvc.perform(put("/supplier/99").contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isNotFound());
    }

    // CT-1.1.52 (CA12): o corpo com "neighborhood": null chega ao serviço marcado como presente e nulo
    @Test
    void ct_1_1_52_put_com_null_explicito_chega_ao_servico_como_presente() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(new SupplierDTO(1L, "Construtora Beta", "", false));

        mvc.perform(put("/supplier/1").contentType(MediaType.APPLICATION_JSON).content(body("\"neighborhood\":null")))
                .andExpect(status().isOk());

        final ArgumentCaptor<SupplierUpdateDTO> captor = ArgumentCaptor.forClass(SupplierUpdateDTO.class);
        verify(service).update(eq(1L), captor.capture());
        assertThat(captor.getValue().isPresent(SupplierUpdateDTO.NEIGHBORHOOD)).isTrue();
        assertThat(captor.getValue().getNeighborhood()).isNull();
        assertThat(captor.getValue().isPresent(SupplierUpdateDTO.CITY)).isFalse();
    }
}
