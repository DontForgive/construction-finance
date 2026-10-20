package br.com.galsystem.construction.finance.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.com.galsystem.construction.finance.dto.supplier.SupplierCreateDTO;
import br.com.galsystem.construction.finance.dto.supplier.SupplierDTO;
import br.com.galsystem.construction.finance.models.Supplier;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários do SupplierMapper quanto ao endereço (HU-1.2).
 * Usa a implementação gerada pelo MapStruct (SupplierMapperImpl).
 */
class SupplierMapperEnderecoTest {

    private final SupplierMapper mapper = new SupplierMapperImpl();

    /** CT-1.2.8: CEP com hífen é gravado sem máscara. */
    @Test
    void ct128_cepComHifenEGravadoSomenteComDigitos() {
        Supplier entity = mapper.toEntity(comCepEstado("01310-100", null));

        assertEquals("01310100", entity.getCep(), "CT-1.2.8");
    }

    /** CT-1.2.9: CEP sem hífen é gravado sem alteração. */
    @Test
    void ct129_cepSemHifenEGravadoSemAlteracao() {
        Supplier entity = mapper.toEntity(comCepEstado("01310100", null));

        assertEquals("01310100", entity.getCep(), "CT-1.2.9");
    }

    /** CT-1.2.11: UF em minúscula é gravada em maiúsculas. */
    @Test
    void ct1211_ufEmMinusculaEGravadaEmMaiusculas() {
        Supplier entity = mapper.toEntity(comCepEstado(null, "sp"));

        assertEquals("SP", entity.getEstado(), "CT-1.2.11");
    }

    /** CT-1.2.15: cep e estado nulos permanecem nulos. */
    @Test
    void ct1215_cepEEstadoNulosPermanecemNulos() {
        Supplier entity = mapper.toEntity(comCepEstado(null, null));

        assertNull(entity.getCep(), "CT-1.2.15 (cep)");
        assertNull(entity.getEstado(), "CT-1.2.15 (estado)");
    }

    /** CT-1.2.23: endereço completo gera Supplier com os 7 campos preenchidos. */
    @Test
    void ct1223_toEntityComEnderecoCompletoPreencheTodosOsCampos() {
        SupplierCreateDTO dto = new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                "Av. Paulista", "1578", "Bela Vista", "São Paulo", "sp", "01310-100", "Sala 10");

        Supplier entity = mapper.toEntity(dto);

        assertEquals("Av. Paulista", entity.getRua(), "CT-1.2.23 (rua)");
        assertEquals("1578", entity.getNumero(), "CT-1.2.23 (numero)");
        assertEquals("Bela Vista", entity.getBairro(), "CT-1.2.23 (bairro)");
        assertEquals("São Paulo", entity.getCidade(), "CT-1.2.23 (cidade)");
        assertEquals("SP", entity.getEstado(), "CT-1.2.23 (estado)");
        assertEquals("01310100", entity.getCep(), "CT-1.2.23 (cep)");
        assertEquals("Sala 10", entity.getComplemento(), "CT-1.2.23 (complemento)");
    }

    /** CT-1.2.24: toDTO devolve os 7 campos de endereço, nulos quando não informados. */
    @Test
    void ct1224_toDTODevolveCamposDeEnderecoENulosQuandoAusentes() {
        Supplier comEndereco = Supplier.builder()
                .name("Fornecedor Teste")
                .rua("Av. Paulista").numero("1578").bairro("Bela Vista").cidade("São Paulo")
                .estado("SP").cep("01310100").complemento("Sala 10")
                .build();
        Supplier semEndereco = Supplier.builder().name("Fornecedor Antigo").build();

        SupplierDTO dtoComEndereco = mapper.toDTO(comEndereco);
        SupplierDTO dtoSemEndereco = mapper.toDTO(semEndereco);

        assertEquals("Av. Paulista", dtoComEndereco.rua(), "CT-1.2.24 (rua)");
        assertEquals("1578", dtoComEndereco.numero(), "CT-1.2.24 (numero)");
        assertEquals("Bela Vista", dtoComEndereco.bairro(), "CT-1.2.24 (bairro)");
        assertEquals("São Paulo", dtoComEndereco.cidade(), "CT-1.2.24 (cidade)");
        assertEquals("SP", dtoComEndereco.estado(), "CT-1.2.24 (estado)");
        assertEquals("01310100", dtoComEndereco.cep(), "CT-1.2.24 (cep)");
        assertEquals("Sala 10", dtoComEndereco.complemento(), "CT-1.2.24 (complemento)");

        assertNull(dtoSemEndereco.rua(), "CT-1.2.24 (rua nula)");
        assertNull(dtoSemEndereco.numero(), "CT-1.2.24 (numero nulo)");
        assertNull(dtoSemEndereco.bairro(), "CT-1.2.24 (bairro nulo)");
        assertNull(dtoSemEndereco.cidade(), "CT-1.2.24 (cidade nula)");
        assertNull(dtoSemEndereco.estado(), "CT-1.2.24 (estado nulo)");
        assertNull(dtoSemEndereco.cep(), "CT-1.2.24 (cep nulo)");
        assertNull(dtoSemEndereco.complemento(), "CT-1.2.24 (complemento nulo)");
    }

    private static SupplierCreateDTO comCepEstado(String cep, String estado) {
        return new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                null, null, null, null, estado, cep, null);
    }
}
