package br.com.galsystem.construction.finance.service.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.mapper.SupplierMapperImpl;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitários de SupplierServiceImpl quanto ao endereço (HU-1.2).
 * Os casos aqui são a versão em nível de serviço (repositório mockado).
 * Os testes HTTP, de cache e de banco (CT-1.2.2, 3, 4, 19 a 22, 26 a 28, 29 parte HTTP, 30) não fazem parte desta classe.
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceImplEnderecoTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupplierServiceImpl(supplierRepository, new SupplierMapperImpl());
    }

    /** CT-1.2.25: create com endereço chama save com os campos e devolve SupplierDTO com o endereço. */
    @Test
    void ct1225_createComEnderecoChamaSaveComOsCamposEMantemRetorno() {
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));
        SupplierCreateDTO dto = new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                "Av. Paulista", "1578", "Bela Vista", "São Paulo", "sp", "01310-100", "Sala 10");

        SupplierDTO retorno = service.create(dto);

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        Supplier salvo = captor.getValue();
        assertEquals("Av. Paulista", salvo.getRua(), "CT-1.2.25 (rua salva)");
        assertEquals("1578", salvo.getNumero(), "CT-1.2.25 (numero salvo)");
        assertEquals("Bela Vista", salvo.getBairro(), "CT-1.2.25 (bairro salvo)");
        assertEquals("São Paulo", salvo.getCidade(), "CT-1.2.25 (cidade salva)");
        assertEquals("SP", salvo.getEstado(), "CT-1.2.25 (estado salvo em maiúsculas)");
        assertEquals("01310100", salvo.getCep(), "CT-1.2.25 (cep salvo sem máscara)");
        assertEquals("Sala 10", salvo.getComplemento(), "CT-1.2.25 (complemento salvo)");

        assertEquals("Fornecedor Teste", retorno.name(), "CT-1.2.25 (retorno name)");
        assertEquals("Av. Paulista", retorno.rua(), "CT-1.2.25 (retorno rua)");
        assertEquals("01310100", retorno.cep(), "CT-1.2.25 (retorno cep)");
    }

    /**
     * CT-1.2.21 e CT-1.2.22 (nível serviço): worker = true sem endereço e com endereço completo são aceitos.
     */
    @Test
    void ct1221_e_ct1222_workerTrueComOuSemEnderecoEAceito() {
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierDTO semEndereco = service.create(
                new SupplierCreateDTO("Trabalhador Sem Endereco", true, "pix-1", null, null, null, null, null, null, null));
        SupplierDTO comEndereco = service.create(
                new SupplierCreateDTO("Trabalhador Com Endereco", true, "pix-2",
                        "Rua A", "10", "Centro", "Recife", "PE", "50010-000", null));

        assertEquals(Boolean.TRUE, semEndereco.worker(), "CT-1.2.21 (worker)");
        assertNull(semEndereco.cep(), "CT-1.2.21 (cep nulo)");
        assertEquals(Boolean.TRUE, comEndereco.worker(), "CT-1.2.22 (worker)");
        assertEquals("50010000", comEndereco.cep(), "CT-1.2.22 (cep sem máscara)");
        assertEquals("PE", comEndereco.estado(), "CT-1.2.22 (estado)");
    }

    /** CT-1.2.28 (nível serviço): nome já existente mantém o erro de duplicidade atual. */
    @Test
    void ct1228_nomeDuplicadoMantemErroDeDuplicidade() {
        when(supplierRepository.existsByNameIgnoreCase("Fornecedor Teste")).thenReturn(true);
        SupplierCreateDTO dto = new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                "Av. Paulista", null, null, null, null, null, null);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.create(dto), "CT-1.2.28");
        assertEquals("Já existe um Fornecedor com o nome 'Fornecedor Teste'", ex.getMessage(), "CT-1.2.28 (mensagem)");
        verify(supplierRepository, org.mockito.Mockito.never()).save(any(Supplier.class));
    }

    /** CT-1.2.28 (nível serviço): chave Pix já existente mantém o erro de duplicidade atual. */
    @Test
    void ct1228_chavePixDuplicadaMantemErroDeDuplicidade() {
        when(supplierRepository.existsByKeyPixIgnoreCase("chave@pix.com")).thenReturn(true);
        SupplierCreateDTO dto = new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                null, null, null, null, null, null, null);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.create(dto), "CT-1.2.28 (pix)");
        assertEquals("CHAVE PIX já Cadastrada", ex.getMessage(), "CT-1.2.28 (pix, mensagem)");
        verify(supplierRepository, org.mockito.Mockito.never()).save(any(Supplier.class));
    }

    /**
     * CT-1.2.29 (nível serviço, parte sem HTTP): update não grava endereço, pois SupplierUpdateDTO não o recebe.
     * O endereço gravado é mantido e devolvido.
     */
    @Test
    void ct1229_updateNaoAlteraEnderecoGravado() {
        Supplier existente = Supplier.builder()
                .id(7L).name("Fornecedor Teste").worker(false).keyPix("chave@pix.com")
                .rua("Av. Paulista").numero("1578").bairro("Bela Vista").cidade("São Paulo")
                .estado("SP").cep("01310100").complemento("Sala 10")
                .build();
        when(supplierRepository.findById(7L)).thenReturn(java.util.Optional.of(existente));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierDTO retorno = service.update(7L, new SupplierUpdateDTO("Fornecedor Teste", false, "chave@pix.com"));

        assertEquals("Av. Paulista", retorno.rua(), "CT-1.2.29 (rua mantida)");
        assertEquals("01310100", retorno.cep(), "CT-1.2.29 (cep mantido)");
        assertEquals("SP", retorno.estado(), "CT-1.2.29 (estado mantido)");
    }

    /**
     * CT-1.2.31 (proxy): criação com endereço não interage com nenhuma dependência além do repositório.
     * O serviço não possui cliente de CEP; a verificação é de que só o repositório é chamado.
     */
    @Test
    void ct1231_criacaoComEnderecoNaoChamaOutrasDependencias() {
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));
        SupplierCreateDTO dto = new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                "Av. Paulista", "1578", "Bela Vista", "São Paulo", "SP", "01310-100", "Sala 10");

        service.create(dto);

        verify(supplierRepository).existsByNameIgnoreCase("Fornecedor Teste");
        verify(supplierRepository).existsByKeyPixIgnoreCase("chave@pix.com");
        verify(supplierRepository).save(any(Supplier.class));
        verifyNoMoreInteractions(supplierRepository);
    }
}
