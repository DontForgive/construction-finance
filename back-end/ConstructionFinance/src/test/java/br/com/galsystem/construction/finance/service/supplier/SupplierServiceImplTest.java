package br.com.galsystem.construction.finance.service.supplier;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDetailDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierUpdateDTO;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.NotFoundException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.mapper.SupplierMapper;
import br.com.galsystem.construction.finance.mapper.SupplierMapperImpl;
import br.com.galsystem.construction.finance.models.Supplier;
import br.com.galsystem.construction.finance.repository.SupplierRepository;
import br.com.galsystem.construction.finance.support.JsonSupport;
import br.com.galsystem.construction.finance.support.SupplierTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.Map;
import java.util.Optional;

import static br.com.galsystem.construction.finance.support.SupplierTestData.addressOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes de unidade do serviço. Repositório simulado (fronteira de persistência); mapper e DTOs reais.
 * As anotações de cache (@CacheEvict/@CachePut) só atuam com proxy Spring e NÃO são exercitadas aqui.
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository repository;

    private final SupplierMapper mapper = new SupplierMapperImpl();
    private ConcurrentMapCacheManager cacheManager;
    private SupplierServiceImpl service;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("supplierByName", "supplierList");
        service = new SupplierServiceImpl(repository, mapper, cacheManager);
    }

    private static SupplierUpdateDTO updateJson(final String extra) {
        return JsonSupport.updateFromJson("{\"name\":\"Construtora Beta\",\"worker\":false,\"keyPix\":\"pix-beta\""
                + (extra.isEmpty() ? "" : "," + extra) + "}");
    }

    private Supplier stubExisting(final Supplier existing) {
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));
        return existing;
    }

    // ---------- Criação ----------

    // CT-1.1.1 (CA1): criação só com nome e worker grava os 9 campos de endereço como null
    @Test
    void ct_1_1_1_criacao_sem_endereco_grava_endereco_vazio() {
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> {
            final Supplier s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        final SupplierDTO result = service.create(SupplierTestData.create().name("Fornecedor Alfa").worker(true).build());

        final ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Fornecedor Alfa");
        assertThat(captor.getValue().getWorker()).isTrue();
        assertThat(addressOf(captor.getValue()).values()).containsOnlyNulls();
        assertThat(result.name()).isEqualTo("Fornecedor Alfa");
    }

    // CT-1.1.2 (CA1): PIX preservado
    @Test
    void ct_1_1_2_criacao_com_pix_sem_endereco_preserva_pix() {
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        final SupplierDTO result = service.create(SupplierTestData.create().keyPix("pix-1").build());

        assertThat(result.keyPix()).isEqualTo("pix-1");
    }

    // CT-1.1.3 / CT-1.1.9 (CA2): endereço completo é copiado para a entidade salva
    @Test
    void ct_1_1_3_9_criacao_com_endereco_completo_persiste_cada_campo() {
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(SupplierTestData.create().fullAddress().zipCode("01310-100").state("sp").country("br").build());

        final ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        assertThat(addressOf(captor.getValue())).containsExactlyInAnyOrderEntriesOf(Map.of(
                "zipCode", "01310100", "street", "Avenida Paulista", "number", "1000",
                "complement", "Sala 10", "neighborhood", "Bela Vista", "city", "Sao Paulo",
                "state", "SP", "country", "BR", "reference", "Proximo ao metro"));
    }

    // CT-1.1.4 (CA2/P2): a resposta do create é SupplierDTO (id, name, keyPix, worker)
    @Test
    void ct_1_1_4_resposta_do_create_e_supplier_dto_sem_endereco() {
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        final Object result = service.create(SupplierTestData.create().fullAddress().build());

        assertThat(result).isInstanceOf(SupplierDTO.class);
        assertThat(SupplierDTO.class.getRecordComponents()).extracting("name")
                .containsExactly("id", "name", "keyPix", "worker");
    }

    // CT-1.1.5 (CA8): só cidade
    @Test
    void ct_1_1_5_6_endereco_parcial_grava_so_os_campos_informados() {
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(SupplierTestData.create().city("Sao Paulo").build());

        final ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        final Map<String, String> address = addressOf(captor.getValue());
        assertThat(address.get("city")).isEqualTo("Sao Paulo");
        address.remove("city");
        assertThat(address.values()).hasSize(8).containsOnlyNulls();
    }

    // CT-1.1.55 (CA13): todos "" -> null persistido
    @Test
    void ct_1_1_55_criacao_com_todos_os_campos_vazios_persiste_null() {
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(new SupplierCreateDTO("Construtora Beta", false, null, "", "", "", "", "", "", "", "", ""));

        final ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        assertThat(addressOf(captor.getValue()).values()).containsOnlyNulls();
    }

    // CT-1.1.61 (CA14): nome duplicado na criação -> conflito, sem save
    @Test
    void ct_1_1_61_criacao_com_nome_duplicado_lanca_conflito_sem_salvar() {
        when(repository.existsByNameIgnoreCase("Construtora Beta")).thenReturn(true);

        assertThatThrownBy(() -> service.create(SupplierTestData.create().fullAddress().build()))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    // CT-1.1.62 (CA14): PIX duplicado na criação -> conflito, sem save
    @Test
    void ct_1_1_62_criacao_com_pix_duplicado_lanca_conflito_sem_salvar() {
        when(repository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsByKeyPixIgnoreCase("pix-x")).thenReturn(true);

        assertThatThrownBy(() -> service.create(SupplierTestData.create().keyPix("pix-x").fullAddress().build()))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    // CT-1.1.72 (RN10): linha de base - PIX ausente vira "" antes da checagem de unicidade
    @Test
    void ct_1_1_72_linha_de_base_pix_ausente_vira_string_vazia() {
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(SupplierTestData.create().build());

        verify(repository).existsByKeyPixIgnoreCase("");
        final ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getKeyPix()).isEmpty();
    }

    // ---------- Consulta ----------

    // CT-1.1.8 / CT-1.1.3 (CA2): findById devolve os 9 campos de endereço
    @Test
    void ct_1_1_8_find_by_id_devolve_os_nove_campos_de_endereco() {
        final Supplier s = SupplierTestData.supplierWithAddress();
        when(repository.findById(1L)).thenReturn(Optional.of(s));

        final SupplierDetailDTO dto = service.findById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Construtora Beta");
        assertThat(dto.worker()).isFalse();
        assertThat(dto.keyPix()).isEqualTo("pix-beta");
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

    // CT-1.1.8 / CT-1.1.10 (CA2): entidade sem endereço -> 9 campos null, sem erro
    @Test
    void ct_1_1_8_10_find_by_id_de_fornecedor_antigo_devolve_endereco_vazio() {
        when(repository.findById(2L)).thenReturn(Optional.of(SupplierTestData.supplierWithoutAddress()));

        final SupplierDetailDTO dto = service.findById(2L);

        assertThat(dto.name()).isEqualTo("Fornecedor Antigo Ltda");
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

    @Test
    void find_by_id_inexistente_lanca_not_found() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- Edição ----------

    // CT-1.1.45 (CA9): fornecedor antigo editado sem endereço continua sem endereço
    @Test
    void ct_1_1_45_edicao_de_fornecedor_antigo_sem_endereco_nao_cria_endereco() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithoutAddress());
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Fornecedor Antigo Ltda\",\"worker\":false,\"keyPix\":\"novo-pix\"}");

        service.update(2L, dto);

        assertThat(s.getWorker()).isFalse();
        assertThat(s.getKeyPix()).isEqualTo("novo-pix");
        assertThat(addressOf(s).values()).containsOnlyNulls();
    }

    // CT-1.1.46 / CT-1.1.50 (CA10): edição sem campos de endereço preserva o endereço
    @Test
    void ct_1_1_46_50_edicao_sem_campos_de_endereco_preserva_endereco() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());
        final Map<String, String> antes = addressOf(SupplierTestData.supplierWithAddress());

        service.update(1L, updateJson(""));

        assertThat(addressOf(s)).containsExactlyEntriesOf(antes);
    }

    // CT-1.1.47 (CA10): altera só nome e worker; endereço permanece
    @Test
    void ct_1_1_47_edicao_de_nome_e_worker_mantem_endereco() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());
        final Map<String, String> antes = addressOf(SupplierTestData.supplierWithAddress());
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Beta Novo\",\"worker\":true,\"keyPix\":\"pix-beta\"}");

        service.update(1L, dto);

        assertThat(s.getName()).isEqualTo("Beta Novo");
        assertThat(s.getWorker()).isTrue();
        assertThat(addressOf(s)).containsExactlyEntriesOf(antes);
    }

    // CT-1.1.48 (CA11): fornecedor sem endereço, PUT com city e state
    @Test
    void ct_1_1_48_edicao_com_cidade_e_uf_grava_so_esses_campos() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithoutAddress());
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Fornecedor Antigo Ltda\",\"worker\":true,\"keyPix\":\"pix-antigo\","
                        + "\"city\":\"Belo Horizonte\",\"state\":\"mg\"}");

        service.update(2L, dto);

        final Map<String, String> address = addressOf(s);
        assertThat(address.get("city")).isEqualTo("Belo Horizonte");
        assertThat(address.get("state")).isEqualTo("MG");
        address.remove("city");
        address.remove("state");
        assertThat(address.values()).hasSize(7).containsOnlyNulls();
    }

    // CT-1.1.49 (CA11): só a cidade muda; os outros 8 campos permanecem
    @Test
    void ct_1_1_49_edicao_so_de_cidade_mantem_os_outros_campos() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());
        final Map<String, String> antes = addressOf(SupplierTestData.supplierWithAddress());

        service.update(1L, updateJson("\"city\":\"Outra Cidade\""));

        final Map<String, String> depois = addressOf(s);
        assertThat(depois.get("city")).isEqualTo("Outra Cidade");
        antes.remove("city");
        depois.remove("city");
        assertThat(depois).containsExactlyEntriesOf(antes);
    }

    // CT-1.1.52 / CT-1.1.53 (CA12): null em um campo limpa só ele (cada um dos 9)
    @ParameterizedTest(name = "CT-1.1.52/53: {0} = null limpa só {0}")
    @ValueSource(strings = {"zipCode", "street", "number", "complement", "neighborhood", "city", "state", "country",
            "reference"})
    void ct_1_1_52_53_null_limpa_somente_o_campo_enviado(final String campo) {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());
        final Map<String, String> antes = addressOf(SupplierTestData.supplierWithAddress());

        service.update(1L, updateJson("\"" + campo + "\":null"));

        final Map<String, String> depois = addressOf(s);
        assertThat(depois.get(campo)).isNull();
        antes.remove(campo);
        depois.remove(campo);
        assertThat(depois).containsExactlyEntriesOf(antes);
    }

    // CT-1.1.54 (CA12): null em campo já vazio -> sem erro e sem alteração
    @Test
    void ct_1_1_54_null_em_campo_ja_vazio_nao_altera_nada() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithoutAddress());
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Fornecedor Antigo Ltda\",\"worker\":true,\"keyPix\":\"pix-antigo\",\"neighborhood\":null}");

        service.update(2L, dto);

        assertThat(addressOf(s).values()).containsOnlyNulls();
    }

    // CT-1.1.56 (CA13): "" limpa street; os demais permanecem
    @Test
    void ct_1_1_56_string_vazia_limpa_somente_o_campo() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());
        final Map<String, String> antes = addressOf(SupplierTestData.supplierWithAddress());

        service.update(1L, updateJson("\"street\":\"\""));

        final Map<String, String> depois = addressOf(s);
        assertThat(depois.get("street")).isNull();
        antes.remove("street");
        depois.remove("street");
        assertThat(depois).containsExactlyEntriesOf(antes);
    }

    // CT-1.1.57 (CA13): "" em CEP, UF e país limpa os três
    @Test
    void ct_1_1_57_vazio_em_cep_uf_e_pais_limpa_os_tres() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());

        service.update(1L, updateJson("\"zipCode\":\"\",\"state\":\"\",\"country\":\"\""));

        assertThat(s.getZipCode()).isNull();
        assertThat(s.getState()).isNull();
        assertThat(s.getCountry()).isNull();
        assertThat(s.getStreet()).isEqualTo("Avenida Paulista");
    }

    // CT-1.1.16 (CA3): CEP com máscara no PUT é gravado com 8 dígitos
    @Test
    void ct_1_1_16_cep_com_mascara_no_put_e_gravado_sem_mascara() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithoutAddress());
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Fornecedor Antigo Ltda\",\"worker\":true,\"keyPix\":\"pix-antigo\",\"zipCode\":\"01310-100\"}");

        service.update(2L, dto);

        assertThat(s.getZipCode()).isEqualTo("01310100");
    }

    // CT-1.1.59 (CA11/CA12): mesmo PUT repetido produz o mesmo estado
    @Test
    void ct_1_1_59_put_repetido_e_idempotente() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());

        service.update(1L, updateJson("\"city\":\"Outra\",\"state\":\"rj\",\"street\":null"));
        final Map<String, String> primeira = addressOf(s);
        service.update(1L, updateJson("\"city\":\"Outra\",\"state\":\"rj\",\"street\":null"));

        assertThat(addressOf(s)).containsExactlyEntriesOf(primeira);
    }

    // CT-1.1.63 (CA14): nome de outro fornecedor -> conflito, endereço e save intactos
    @Test
    void ct_1_1_63_edicao_com_nome_de_outro_fornecedor_lanca_conflito_sem_alterar_endereco() {
        final Supplier s = SupplierTestData.supplierWithAddress();
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        when(repository.existsByNameIgnoreCase("Outro Nome")).thenReturn(true);
        final Map<String, String> antes = addressOf(SupplierTestData.supplierWithAddress());
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Outro Nome\",\"worker\":false,\"keyPix\":\"pix-beta\",\"city\":\"Nova\"}");

        assertThatThrownBy(() -> service.update(1L, dto)).isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
        assertThat(addressOf(s)).containsExactlyEntriesOf(antes);
        assertThat(s.getName()).isEqualTo("Construtora Beta");
    }

    // CT-1.1.64 / CT-1.1.66 (CA14): PIX de outro fornecedor -> conflito antes do save
    @Test
    void ct_1_1_64_66_edicao_com_pix_de_outro_fornecedor_lanca_conflito_antes_do_save() {
        final Supplier s = SupplierTestData.supplierWithAddress();
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        when(repository.existsByKeyPixIgnoreCaseAndIdNot("pix-de-outro", 1L)).thenReturn(true);
        final Map<String, String> antes = addressOf(SupplierTestData.supplierWithAddress());
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Construtora Beta\",\"worker\":false,\"keyPix\":\"pix-de-outro\",\"city\":\"Nova\"}");

        assertThatThrownBy(() -> service.update(1L, dto)).isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
        assertThat(addressOf(s)).containsExactlyEntriesOf(antes);
    }

    // CT-1.1.65 (CA14): o próprio nome e o próprio PIX não geram conflito
    @Test
    void ct_1_1_65_edicao_com_proprio_nome_e_pix_nao_gera_conflito() {
        final Supplier s = stubExisting(SupplierTestData.supplierWithAddress());

        service.update(1L, updateJson("\"city\":\"Nova\""));

        assertThat(s.getCity()).isEqualTo("Nova");
        verify(repository, never()).existsByNameIgnoreCase(anyString());
        verify(repository).save(s);
    }

    // CT-1.1.71 (RN10): PUT em ID inexistente -> NotFound, com ou sem endereço
    @Test
    void ct_1_1_71_edicao_de_id_inexistente_lanca_not_found() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, updateJson("\"city\":\"X\"")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.update(99L, updateJson("")))
                .isInstanceOf(NotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ---------- findOrCreateByName ----------

    // CT-1.1.67 (CA15): nome inexistente -> cria só com nome
    @Test
    void ct_1_1_67_find_or_create_com_nome_inexistente_cria_so_com_nome() {
        when(repository.findByNameIgnoreCase("Novo Fornecedor")).thenReturn(Optional.empty());
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        final SupplierDTO result = service.findOrCreateByName("  Novo Fornecedor ");

        final ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Novo Fornecedor");
        assertThat(addressOf(captor.getValue()).values()).containsOnlyNulls();
        assertThat(result.name()).isEqualTo("Novo Fornecedor");
    }

    // CT-1.1.69 (CA15): nome existente (case-insensitive) -> devolve o existente, sem criar
    @Test
    void ct_1_1_69_find_or_create_com_nome_existente_nao_cria_outro() {
        final Supplier existing = SupplierTestData.supplierWithAddress();
        when(repository.findByNameIgnoreCase("construtora beta")).thenReturn(Optional.of(existing));

        final SupplierDTO result = service.findOrCreateByName("construtora beta");

        verify(repository, never()).save(any());
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Construtora Beta");
    }

    // ---------- Cache (CA16) ----------

    // CT-1.1.77 (CA16, parte programática): update invalida a chave do NOME ANTIGO em supplierByName
    @Test
    void ct_1_1_77_update_com_troca_de_nome_invalida_a_chave_do_nome_antigo() {
        stubExisting(SupplierTestData.supplierWithAddress());
        final Cache cache = cacheManager.getCache("supplierByName");
        cache.put("construtora beta", "valor-antigo");
        cache.put("outro", "mantido");
        final SupplierUpdateDTO dto = JsonSupport.updateFromJson(
                "{\"name\":\"Beta Novo\",\"worker\":false,\"keyPix\":\"pix-beta\",\"city\":\"Nova\"}");

        service.update(1L, dto);

        assertThat(cache.get("construtora beta")).as("chave do nome antigo").isNull();
        assertThat(cache.get("outro")).as("outras chaves não são afetadas").isNotNull();
    }
}
