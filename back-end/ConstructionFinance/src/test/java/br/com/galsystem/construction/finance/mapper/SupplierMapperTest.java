package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.supplier.CampoOpcional;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
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
    @DisplayName("[legado, sem ID aprovado] toListDTO expõe id, nome, chave PIX, prestador e os 7 campos de endereço do fornecedor completo")
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
    @DisplayName("[legado, sem ID aprovado] SupplierListDTO não contém country nem reference")
    void ct_1_1_1_supplierListDTO_nao_tem_country_nem_reference() {
        List<String> componentes = Arrays.stream(SupplierListDTO.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(componentes).containsExactly(
                "id", "name", "keyPix", "worker",
                "zipCode", "street", "number", "complement", "neighborhood", "city", "state", "phone");
    }

    @Test
    @DisplayName("[legado, sem ID aprovado] toListDTO de fornecedor sem endereço devolve os 7 campos de endereço nulos")
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
    @DisplayName("[legado, sem ID aprovado] toDTO devolve id, nome, chave PIX, prestador e telefone nulo quando o fornecedor não tem telefone")
    void ct_1_1_3_toDTO_mantem_contrato_atual() {
        SupplierDTO dto = mapper.toDTO(fornecedorCompleto());

        assertThat(dto).isEqualTo(new SupplierDTO(1L, "Fornecedor Teste Alfa", "chave-ficticia-01", true, null));
        List<String> componentes = Arrays.stream(SupplierDTO.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
        assertThat(componentes).containsExactly("id", "name", "keyPix", "worker", "phone");
    }

    @Test
    @DisplayName("CT-1.1.13 updateEntity com telefone ausente atualiza endereço e prestador e não altera o telefone")
    void ct_1_1_13_updateEntity_nao_altera_telefone_e_atualiza_endereco() {
        Supplier entidade = fornecedorCompleto();
        entidade.setPhone("21998765432");
        SupplierUpdateDTO dto = new SupplierUpdateDTO("Fornecedor Teste Alfa", false, "chave-ficticia-01",
                "01001000", "Avenida Nova", "500", "andar 2", "Sé", "São Paulo", "SP", "BR", "ref-02",
                CampoOpcional.ausente());

        mapper.updateEntity(entidade, dto);

        assertThat(entidade.getZipCode()).isEqualTo("01001000");
        assertThat(entidade.getStreet()).isEqualTo("Avenida Nova");
        assertThat(entidade.getNumber()).isEqualTo("500");
        assertThat(entidade.getComplement()).isEqualTo("andar 2");
        assertThat(entidade.getNeighborhood()).isEqualTo("Sé");
        assertThat(entidade.getCity()).isEqualTo("São Paulo");
        assertThat(entidade.getState()).isEqualTo("SP");
        assertThat(entidade.getCountry()).isEqualTo("BR");
        assertThat(entidade.getReference()).isEqualTo("ref-02");
        assertThat(entidade.getWorker()).isFalse();
        assertThat(entidade.getPhone()).isEqualTo("21998765432");
    }

    @Test
    @DisplayName("CT-1.1.14 toDTO devolve o telefone gravado com somente dígitos")
    void ct_1_1_14_toDTO_devolve_telefone() {
        Supplier entidade = fornecedorCompleto();
        entidade.setPhone("11912345678");

        assertThat(mapper.toDTO(entidade).phone()).isEqualTo("11912345678");
    }

    @Test
    @DisplayName("CT-1.1.15 toListDTO e toDetailDTO devolvem o telefone ou null, somente dígitos")
    void ct_1_1_15_listDTO_e_detailDTO_devolvem_telefone_ou_null() {
        Supplier comTelefone = fornecedorCompleto();
        comTelefone.setPhone("11912345678");
        Supplier semTelefone = fornecedorSoNome();

        assertThat(mapper.toListDTO(comTelefone).phone()).isEqualTo("11912345678");
        assertThat(mapper.toListDTO(semTelefone).phone()).isNull();
        assertThat(mapper.toDetailDTO(comTelefone).phone()).isEqualTo("11912345678");
        assertThat(mapper.toDetailDTO(semTelefone).phone()).isNull();
    }
}
