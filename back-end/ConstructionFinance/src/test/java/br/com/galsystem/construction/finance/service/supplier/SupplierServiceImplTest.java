package br.com.galsystem.construction.finance.service.supplier;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierListDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.mapper.SupplierMapper;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupplierServiceImplTest {

    private SupplierRepository repository;
    private SupplierServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SupplierRepository.class);
        SupplierMapper mapper = Mappers.getMapper(SupplierMapper.class);
        service = new SupplierServiceImpl(repository, mapper);
    }

    /** DS1 (completo) dos casos de teste aprovados da HU-1.1. */
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

    /** Payload SupplierDTO esperado para DS1 (contrato atual, sem campos de endereço). */
    private static SupplierDTO dtoEsperadoDS1() {
        return new SupplierDTO(1L, "Fornecedor Teste Alfa", "chave-ficticia-01", true);
    }

    @Test
    @DisplayName("CT-1.1.4 listar repassa filtros, ordenação e paginação ao repositório e devolve Page<SupplierListDTO> com o mesmo total")
    void ct_1_1_4_listar_repassa_filtros_e_paginacao() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "id"));
        List<Supplier> itens = List.of(fornecedorCompleto(), Supplier.builder().id(2L).name("Beta").city("Santos").build());
        Page<Supplier> pagina = new PageImpl<>(itens, pageable, 5);
        when(repository.findByFilters(eq("Alfa"), eq("chave"), eq(true), any(Pageable.class))).thenReturn(pagina);

        Page<SupplierListDTO> result = service.listar("Alfa", "chave", true, pageable);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByFilters(eq("Alfa"), eq("chave"), eq(true), captor.capture());
        assertThat(captor.getValue()).isEqualTo(pageable);
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "id"));

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).city()).isEqualTo("Campinas");
        assertThat(result.getContent().get(0).state()).isEqualTo("SP");
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
        assertThat(result.getContent().get(1).city()).isEqualTo("Santos");
    }

    @Test
    @DisplayName("CT-1.1.16 findOrCreateByName com fornecedor existente devolve SupplierDTO sem campos de endereço")
    void ct_1_1_16_findOrCreateByName_existente_devolve_supplierDTO() {
        when(repository.findByNameIgnoreCase("Fornecedor Teste Alfa")).thenReturn(Optional.of(fornecedorCompleto()));

        SupplierDTO result = service.findOrCreateByName("Fornecedor Teste Alfa");

        assertThat(result).isEqualTo(dtoEsperadoDS1());
    }

    @Test
    @DisplayName("CT-1.1.16 findOrCreateByName sem fornecedor cria o registro e devolve SupplierDTO sem campos de endereço")
    void ct_1_1_16_findOrCreateByName_criacao_devolve_supplierDTO() {
        when(repository.findByNameIgnoreCase("Fornecedor Teste Alfa")).thenReturn(Optional.empty());
        when(repository.save(any(Supplier.class))).thenAnswer(invocacao -> {
            Supplier salvo = invocacao.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });

        SupplierDTO result = service.findOrCreateByName("Fornecedor Teste Alfa");

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Fornecedor Teste Alfa");
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Fornecedor Teste Alfa");
        assertThat(result.keyPix()).isNull();
    }

    @Test
    @DisplayName("CT-1.1.16 create devolve SupplierDTO com id, nome, chave PIX e prestador, sem campos de endereço")
    void ct_1_1_16_create_devolve_supplierDTO() {
        SupplierCreateDTO dto = new SupplierCreateDTO(
                "Fornecedor Teste Alfa", true, "chave-ficticia-01",
                "13010000", "Rua das Flores", "120", "sala 3", "Centro", "Campinas", "SP", "BR", "ref-01");
        when(repository.existsByNameIgnoreCase("Fornecedor Teste Alfa")).thenReturn(false);
        when(repository.existsByKeyPixIgnoreCase("chave-ficticia-01")).thenReturn(false);
        when(repository.save(any(Supplier.class))).thenAnswer(invocacao -> {
            Supplier salvo = invocacao.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });

        SupplierDTO result = service.create(dto);

        assertThat(result).isEqualTo(dtoEsperadoDS1());
    }

    @Test
    @DisplayName("CT-1.1.16 update devolve SupplierDTO com id, nome, chave PIX e prestador, sem campos de endereço")
    void ct_1_1_16_update_devolve_supplierDTO() {
        SupplierUpdateDTO dto = new SupplierUpdateDTO(
                "Fornecedor Teste Alfa", true, "chave-ficticia-01",
                "13010000", "Rua das Flores", "130", "sala 3", "Centro", "Campinas", "SP", "BR", "ref-01");
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedorCompleto()));
        when(repository.existsByKeyPixIgnoreCaseAndIdNot("chave-ficticia-01", 1L)).thenReturn(false);
        when(repository.save(any(Supplier.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        SupplierDTO result = service.update(1L, dto);

        assertThat(result).isEqualTo(dtoEsperadoDS1());
    }
}
