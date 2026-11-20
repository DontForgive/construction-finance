package br.com.galsystem.construction.finance.service.supplier;

import br.com.galsystem.construction.finance.dto.supplier.CampoOpcional;
import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.exception.InvalidPhoneException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
        return new SupplierDTO(1L, "Fornecedor Teste Alfa", "chave-ficticia-01", true, null);
    }

    @Test
    @DisplayName("[legado, sem ID aprovado] listar repassa filtros, ordenação e paginação ao repositório e devolve Page<SupplierListDTO> com o mesmo total")
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
    @DisplayName("[legado, sem ID aprovado] findOrCreateByName com fornecedor existente devolve SupplierDTO sem campos de endereço")
    void ct_1_1_16_findOrCreateByName_existente_devolve_supplierDTO() {
        when(repository.findByNameIgnoreCase("Fornecedor Teste Alfa")).thenReturn(Optional.of(fornecedorCompleto()));

        SupplierDTO result = service.findOrCreateByName("Fornecedor Teste Alfa");

        assertThat(result).isEqualTo(dtoEsperadoDS1());
    }

    @Test
    @DisplayName("[legado, sem ID aprovado] findOrCreateByName sem fornecedor cria o registro e devolve SupplierDTO sem campos de endereço")
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
    @DisplayName("[legado, sem ID aprovado] create devolve SupplierDTO com id, nome, chave PIX, prestador e telefone nulo")
    void ct_1_1_16_create_devolve_supplierDTO() {
        SupplierCreateDTO dto = new SupplierCreateDTO(
                "Fornecedor Teste Alfa", true, "chave-ficticia-01",
                "13010000", "Rua das Flores", "120", "sala 3", "Centro", "Campinas", "SP", "BR", "ref-01", null);
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
    @DisplayName("[legado, sem ID aprovado] update devolve SupplierDTO com id, nome, chave PIX, prestador e telefone nulo")
    void ct_1_1_16_update_devolve_supplierDTO() {
        SupplierUpdateDTO dto = new SupplierUpdateDTO(
                "Fornecedor Teste Alfa", true, "chave-ficticia-01",
                "13010000", "Rua das Flores", "130", "sala 3", "Centro", "Campinas", "SP", "BR", "ref-01", CampoOpcional.ausente());
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedorCompleto()));
        when(repository.existsByKeyPixIgnoreCaseAndIdNot("chave-ficticia-01", 1L)).thenReturn(false);
        when(repository.save(any(Supplier.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        SupplierDTO result = service.update(1L, dto);

        assertThat(result).isEqualTo(dtoEsperadoDS1());
    }

    /** Fornecedor DS1 com telefone já gravado (base para as alterações de telefone). */
    private static Supplier fornecedorComTelefone() {
        Supplier entidade = fornecedorCompleto();
        entidade.setPhone("21998765432");
        return entidade;
    }

    /** Criação com o telefone informado e os demais campos fixos. */
    private static SupplierCreateDTO criacaoComTelefone(String phone) {
        return new SupplierCreateDTO("Fornecedor Novo", true, "chave-nova-01",
                null, null, null, null, null, null, null, null, null, phone);
    }

    /** Alteração com o campo phone no estado informado (ausente, null, vazio ou valor). */
    private static SupplierUpdateDTO alteracaoComTelefone(String nome, CampoOpcional<String> phone) {
        return new SupplierUpdateDTO(nome, true, "chave-ficticia-01",
                null, null, null, null, null, null, null, null, null, phone);
    }

    /** Entidade efetivamente passada ao save na criação. */
    private Supplier salvoNaCriacao(SupplierCreateDTO dto) {
        when(repository.save(any(Supplier.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        service.create(dto);
        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    /** Entidade efetivamente passada ao save na alteração do fornecedor 1. */
    private Supplier salvoNaAlteracao(Supplier existente, SupplierUpdateDTO dto) {
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Supplier.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        service.update(1L, dto);
        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("CT-1.1.1 create com telefone mascarado grava somente os dígitos")
    void ct_1_1_1_create_grava_telefone_somente_digitos() {
        Supplier gravado = salvoNaCriacao(criacaoComTelefone("(11) 91234-5678"));

        assertThat(gravado.getPhone()).isEqualTo("11912345678");
    }

    @Test
    @DisplayName("CT-1.1.2 create com telefone de 10 dígitos grava o mesmo valor")
    void ct_1_1_2_create_grava_telefone_de_10_digitos() {
        Supplier gravado = salvoNaCriacao(criacaoComTelefone("1134567890"));

        assertThat(gravado.getPhone()).isEqualTo("1134567890");
    }

    @Test
    @DisplayName("CT-1.1.3 create com telefone vazio grava telefone nulo sem erro")
    void ct_1_1_3_create_telefone_vazio_grava_nulo() {
        Supplier gravado = salvoNaCriacao(criacaoComTelefone(""));

        assertThat(gravado.getPhone()).isNull();
    }

    @Test
    @DisplayName("CT-1.1.4 create sem o campo phone grava telefone nulo sem erro")
    void ct_1_1_4_create_telefone_ausente_grava_nulo() {
        Supplier gravado = salvoNaCriacao(criacaoComTelefone(null));

        assertThat(gravado.getPhone()).isNull();
    }

    @Test
    @DisplayName("CT-1.1.5 create com telefone de 9 dígitos lança erro de telefone inválido e não grava")
    void ct_1_1_5_create_telefone_9_digitos_rejeitado() {
        assertThatThrownBy(() -> service.create(criacaoComTelefone("119123456")))
                .isInstanceOf(InvalidPhoneException.class);

        verify(repository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("CT-1.1.7 create com telefone de 12 dígitos lança erro de telefone inválido e não grava")
    void ct_1_1_7_create_telefone_12_digitos_rejeitado() {
        assertThatThrownBy(() -> service.create(criacaoComTelefone("119123456789")))
                .isInstanceOf(InvalidPhoneException.class);

        verify(repository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("CT-1.1.8 update com telefone ausente mantém o telefone atual e atualiza o nome")
    void ct_1_1_8_update_telefone_ausente_mantem_valor() {
        Supplier gravado = salvoNaAlteracao(fornecedorComTelefone(),
                alteracaoComTelefone("Fornecedor Teste Alfa Editado", CampoOpcional.ausente()));

        assertThat(gravado.getPhone()).isEqualTo("21998765432");
        assertThat(gravado.getName()).isEqualTo("Fornecedor Teste Alfa Editado");
    }

    @Test
    @DisplayName("CT-1.1.9 update com telefone null explícito apaga o telefone")
    void ct_1_1_9_update_telefone_null_apaga() {
        Supplier gravado = salvoNaAlteracao(fornecedorComTelefone(),
                alteracaoComTelefone("Fornecedor Teste Alfa", CampoOpcional.de(null)));

        assertThat(gravado.getPhone()).isNull();
    }

    @Test
    @DisplayName("CT-1.1.10 update com telefone vazio apaga o telefone")
    void ct_1_1_10_update_telefone_vazio_apaga() {
        Supplier gravado = salvoNaAlteracao(fornecedorComTelefone(),
                alteracaoComTelefone("Fornecedor Teste Alfa", CampoOpcional.de("")));

        assertThat(gravado.getPhone()).isNull();
    }

    @Test
    @DisplayName("CT-1.1.11 update com telefone mascarado grava o novo valor somente com dígitos")
    void ct_1_1_11_update_grava_novo_telefone_normalizado() {
        Supplier gravado = salvoNaAlteracao(fornecedorComTelefone(),
                alteracaoComTelefone("Fornecedor Teste Alfa", CampoOpcional.de("(21) 3456-7890")));

        assertThat(gravado.getPhone()).isEqualTo("2134567890");
    }

    @Test
    @DisplayName("CT-1.1.12 update com telefone de 9 dígitos lança erro, não grava e mantém o valor anterior")
    void ct_1_1_12_update_telefone_invalido_mantem_valor_anterior() {
        Supplier existente = fornecedorComTelefone();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.update(1L,
                alteracaoComTelefone("Fornecedor Teste Alfa", CampoOpcional.de("119123456"))))
                .isInstanceOf(InvalidPhoneException.class);

        verify(repository, never()).save(any(Supplier.class));
        assertThat(existente.getPhone()).isEqualTo("21998765432");
    }

    @Test
    @DisplayName("CT-1.1.16 erro de telefone inválido tem a mensagem aprovada e não expõe o valor enviado")
    void ct_1_1_16_mensagem_do_erro_de_telefone() {
        assertThatThrownBy(() -> service.create(criacaoComTelefone("119123456")))
                .isInstanceOf(InvalidPhoneException.class)
                .hasMessage("Informe um telefone válido com DDD (10 ou 11 dígitos).")
                .hasMessageNotContaining("119123456");
    }

    @Test
    @DisplayName("CT-1.1.18 findOrCreateByName cria fornecedor só com nome e telefone nulo sem falhar")
    void ct_1_1_18_findOrCreateByName_cria_com_telefone_nulo() {
        when(repository.findByNameIgnoreCase("Fornecedor Novo")).thenReturn(Optional.empty());
        when(repository.save(any(Supplier.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        service.findOrCreateByName("Fornecedor Novo");

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Fornecedor Novo");
        assertThat(captor.getValue().getPhone()).isNull();
    }
}
