package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierListDTO;
import br.com.galsystem.construction.finance.models.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierMapperTest {

    private final SupplierMapper mapper = Mappers.getMapper(SupplierMapper.class);

    /** DS1 (completo) das casos de teste aprovados da HU-1.1. */
    private static Supplier fornecedorCompleto() {
        return Supplier.builder()
                .id(1L)
                .name("Fornecedor Teste Alfa")
                .keyPix("chave-ficticia-01")
                .worker(true)
                .zipCode("13010000")
                .street("Rua das Flores")
                .number("120")
                .complement("sala 3")
                .neighborhood("Centro")
                .city("Campinas")
                .state("SP")
                .country("BR")
                .reference("ref-01")
                .build();
    }

    /** DS4 (vazio) das casos de teste aprovados: só o nome, como em findOrCreateByName. */
    private static Supplier fornecedorSoNome() {
        return Supplier.builder().id(4L).name("Fornecedor Criado Pelo Nome").build();
    }

    @Test
    @DisplayName("CT-1.1.1 toListDTO expõe id, nome, chave PIX, prestador e os 7 campos de endereço do fornecedor completo")
    void ct_1_1_1_toListDTO_expoe_campos_de_endereco() {
        SupplierListDTO dto = mapper.toListDTO(fornecedorCompleto());

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Fornecedor Teste Alfa");
        assertThat(dto.keyPix()).isEqualTo("chave-ficticia-01");
        assertThat(dto.worker()).isTrue();
        assertThat(dto.zipCode()).isEqualTo("13010000");
        assertThat(dto.street()).isEqualTo("Rua das Flores");
        assertThat(dto.number()).isEqualTo("120");
        assertThat(dto.complement()).isEqualTo("sala 3");
        assertThat(dto.neighborhood()).isEqualTo("Centro");
        assertThat(dto.city()).isEqualTo("Campinas");
        assertThat(dto.state()).isEqualTo("SP");
    }

    @Test
    @DisplayName("CT-1.1.1 SupplierListDTO não contém country nem reference")
    void ct_1_1_1_supplierListDTO_nao_tem_country_nem_reference() {
        List<String> componentes = Arrays.stream(SupplierListDTO.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(componentes).containsExactly(
                "id", "name", "keyPix", "worker",
                "zipCode", "street", "number", "complement", "neighborhood", "city", "state");
    }

    @Test
    @DisplayName("CT-1.1.2 toListDTO de fornecedor sem endereço devolve os 7 campos de endereço nulos")
    void ct_1_1_2_toListDTO_sem_endereco_devolve_campos_nulos() {
        SupplierListDTO dto = mapper.toListDTO(fornecedorSoNome());

        assertThat(dto.name()).isEqualTo("Fornecedor Criado Pelo Nome");
        assertThat(dto.zipCode()).isNull();
        assertThat(dto.street()).isNull();
        assertThat(dto.number()).isNull();
        assertThat(dto.complement()).isNull();
        assertThat(dto.neighborhood()).isNull();
        assertThat(dto.city()).isNull();
        assertThat(dto.state()).isNull();
    }

    @Test
    @DisplayName("CT-1.1.3 toDTO continua devolvendo somente id, nome, chave PIX e prestador (contrato atual)")
    void ct_1_1_3_toDTO_mantem_contrato_atual() {
        SupplierDTO dto = mapper.toDTO(fornecedorCompleto());

        assertThat(dto).isEqualTo(new SupplierDTO(1L, "Fornecedor Teste Alfa", "chave-ficticia-01", true));
        List<String> componentes = Arrays.stream(SupplierDTO.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
        assertThat(componentes).containsExactly("id", "name", "keyPix", "worker");
    }
}
