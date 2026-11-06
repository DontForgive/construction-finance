package br.com.galsystem.construction.finance.mapper;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.support.SupplierTestData;
import org.junit.jupiter.api.Test;

import static br.com.galsystem.construction.finance.support.SupplierTestData.addressOf;
import static org.assertj.core.api.Assertions.assertThat;

class SupplierMapperTest {

    private final SupplierMapper mapper = new SupplierMapperImpl();

    // CT-1.1.8 (CA2): toDetailDTO mapeia os 9 campos de endereço
    @Test
    void ct_1_1_8_to_detail_dto_mapeia_os_nove_campos() {
        final SupplierDetailDTO dto = mapper.toDetailDTO(SupplierTestData.supplierWithAddress());

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Construtora Beta");
        assertThat(dto.keyPix()).isEqualTo("pix-beta");
        assertThat(dto.worker()).isFalse();
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

    // CT-1.1.8 (CA1): entidade sem endereço -> todos null
    @Test
    void ct_1_1_8_to_detail_dto_sem_endereco_devolve_nulls() {
        final SupplierDetailDTO dto = mapper.toDetailDTO(SupplierTestData.supplierWithoutAddress());

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

    // CT-1.1.9 (CA2): toEntity(SupplierCreateDTO) copia os 9 campos
    @Test
    void ct_1_1_9_to_entity_copia_os_nove_campos() {
        final SupplierCreateDTO dto = SupplierTestData.create().fullAddress().build();

        final Supplier entity = mapper.toEntity(dto);

        assertThat(addressOf(entity)).containsEntry("zipCode", "01310100")
                .containsEntry("street", "Avenida Paulista")
                .containsEntry("number", "1000")
                .containsEntry("complement", "Sala 10")
                .containsEntry("neighborhood", "Bela Vista")
                .containsEntry("city", "Sao Paulo")
                .containsEntry("state", "SP")
                .containsEntry("country", "BR")
                .containsEntry("reference", "Proximo ao metro");
    }

    // CT-1.1.11 (P2): a listagem continua com SupplierDTO sem endereço
    @Test
    void ct_1_1_11_to_dto_continua_sem_campos_de_endereco() {
        assertThat(br.com.galsystem.construction.finance.dto.supplier.SupplierDTO.class.getRecordComponents())
                .extracting("name").containsExactly("id", "name", "keyPix", "worker");
        assertThat(mapper.toDTO(SupplierTestData.supplierWithAddress()).name()).isEqualTo("Construtora Beta");
    }
}
