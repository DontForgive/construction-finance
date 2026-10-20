package br.com.galsystem.construction.finance.dto.supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes unitários de validação do SupplierCreateDTO (HU-1.2).
 * Cobrem os casos CT-1.2.1, CT-1.2.5 a CT-1.2.7, CT-1.2.10 a CT-1.2.18.
 */
class SupplierCreateDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /** CT-1.2.1: os 7 campos de endereço válidos não geram violação. */
    @Test
    void ct121_enderecoCompletoValidoNaoGeraViolacao() {
        SupplierCreateDTO dto = new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                "Av. Paulista", "1578", "Bela Vista", "São Paulo", "SP", "01310-100", "Sala 10");

        assertSemViolacao(dto, "CT-1.2.1");
    }

    /** CT-1.2.5: sem campos de endereço, com name, worker e keyPix válidos, não gera violação. */
    @Test
    void ct125_semEnderecoNaoGeraViolacao() {
        SupplierCreateDTO dto = semEndereco("Fornecedor Teste", false, "chave@pix.com");

        assertSemViolacao(dto, "CT-1.2.5");
    }

    /** CT-1.2.6: name em branco continua gerando violação de NotBlank. */
    @Test
    void ct126_nomeEmBrancoGeraViolacao() {
        SupplierCreateDTO dto = semEndereco("   ", false, "chave@pix.com");

        assertViolacaoEm(dto, "name", "CT-1.2.6");
    }

    /** CT-1.2.7: keyPix nulo continua gerando violação de NotNull. */
    @Test
    void ct127_keyPixNuloGeraViolacao() {
        SupplierCreateDTO dto = semEndereco("Fornecedor Teste", false, null);

        assertViolacaoEm(dto, "keyPix", "CT-1.2.7");
    }

    /** CT-1.2.10: CEPs inválidos geram violação de Pattern. */
    @ParameterizedTest
    @ValueSource(strings = {"0131-0100", "1234567", "123456789", "abcdefgh", "01310-1000"})
    void ct1210_cepInvalidoGeraViolacao(String cep) {
        SupplierCreateDTO dto = comCepEstado(cep, null);

        assertViolacaoEm(dto, "cep", "CT-1.2.10 (cep=" + cep + ")");
    }

    /** CT-1.2.9: CEP com 8 dígitos sem hífen é aceito. */
    @Test
    void ct129_cepSemHifenEAceito() {
        assertSemViolacao(comCepEstado("01310100", null), "CT-1.2.9");
    }

    /** CT-1.2.8 (parte de validação): CEP com hífen é aceito pelo Pattern. */
    @Test
    void ct128_cepComHifenEAceitoPelaValidacao() {
        assertSemViolacao(comCepEstado("01310-100", null), "CT-1.2.8");
    }

    /** CT-1.2.11 (parte de validação): UF em minúscula é aceita. */
    @Test
    void ct1211_ufEmMinusculaEAceita() {
        assertSemViolacao(comCepEstado(null, "sp"), "CT-1.2.11");
    }

    /** CT-1.2.12: UF inexistente gera violação. */
    @Test
    void ct1212_ufInexistenteGeraViolacao() {
        assertViolacaoEm(comCepEstado(null, "XX"), "estado", "CT-1.2.12");
    }

    /** CT-1.2.13: estado com 1 ou 3 caracteres gera violação. */
    @ParameterizedTest
    @ValueSource(strings = {"S", "SPX"})
    void ct1213_estadoComTamanhoErradoGeraViolacao(String estado) {
        assertViolacaoEm(comCepEstado(null, estado), "estado", "CT-1.2.13 (estado=" + estado + ")");
    }

    /** CT-1.2.14: as 27 siglas de UF são aceitas. */
    @ParameterizedTest
    @ValueSource(strings = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"})
    void ct1214_todasAsUfsValidasSaoAceitas(String uf) {
        assertSemViolacao(comCepEstado(null, uf), "CT-1.2.14 (uf=" + uf + ")");
    }

    /** CT-1.2.15: cep e estado nulos não geram violação. */
    @Test
    void ct1215_cepEEstadoNulosNaoGeramViolacao() {
        assertSemViolacao(comCepEstado(null, null), "CT-1.2.15");
    }

    /** CT-1.2.16: rua com 150 caracteres é aceita; com 151 gera violação. */
    @Test
    void ct1216_ruaNoLimiteEForaDoLimite() {
        assertSemViolacao(comRua("R".repeat(150)), "CT-1.2.16 (150 caracteres)");
        assertViolacaoEm(comRua("R".repeat(151)), "rua", "CT-1.2.16 (151 caracteres)");
    }

    /** CT-1.2.17: numero com 10 caracteres é aceito; com 11 gera violação. */
    @Test
    void ct1217_numeroNoLimiteEForaDoLimite() {
        assertSemViolacao(comNumero("12A"), "CT-1.2.17 ('12A')");
        assertSemViolacao(comNumero("S/N"), "CT-1.2.17 ('S/N')");
        assertSemViolacao(comNumero("1234567890"), "CT-1.2.17 (10 caracteres)");
        assertViolacaoEm(comNumero("12345678901"), "numero", "CT-1.2.17 (11 caracteres)");
    }

    /** CT-1.2.18: bairro, cidade e complemento com 100 caracteres são aceitos; com 101 geram violação. */
    @Test
    void ct1218_bairroCidadeComplementoNoLimiteEForaDoLimite() {
        String limite = "B".repeat(100);
        String acima = "B".repeat(101);

        assertSemViolacao(dto(null, null, limite, null, null, null), "CT-1.2.18 (bairro 100)");
        assertViolacaoEm(dto(null, null, acima, null, null, null), "bairro", "CT-1.2.18 (bairro 101)");

        assertSemViolacao(dto(null, limite, null, null, null, null), "CT-1.2.18 (cidade 100)");
        assertViolacaoEm(dto(null, acima, null, null, null, null), "cidade", "CT-1.2.18 (cidade 101)");

        assertSemViolacao(dto(null, null, null, limite, null, null), "CT-1.2.18 (complemento 100)");
        assertViolacaoEm(dto(null, null, null, acima, null, null), "complemento", "CT-1.2.18 (complemento 101)");
    }

    // ---------- auxiliares ----------

    private static SupplierCreateDTO semEndereco(String name, Boolean worker, String keyPix) {
        return new SupplierCreateDTO(name, worker, keyPix, null, null, null, null, null, null, null);
    }

    private static SupplierCreateDTO comCepEstado(String cep, String estado) {
        return new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                null, null, null, null, estado, cep, null);
    }

    private static SupplierCreateDTO comRua(String rua) {
        return new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                rua, null, null, null, null, null, null);
    }

    private static SupplierCreateDTO comNumero(String numero) {
        return new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                null, numero, null, null, null, null, null);
    }

    /** Monta DTO válido com os campos de texto informados (bairro, cidade, complemento). */
    private static SupplierCreateDTO dto(String rua, String cidade, String bairro, String complemento,
            String cep, String estado) {
        return new SupplierCreateDTO("Fornecedor Teste", false, "chave@pix.com",
                rua, null, bairro, cidade, estado, cep, complemento);
    }

    private static void assertSemViolacao(SupplierCreateDTO dto, String id) {
        Set<ConstraintViolation<SupplierCreateDTO>> violacoes = validator.validate(dto);
        assertTrue(violacoes.isEmpty(), id + ": esperava nenhuma violação, veio " + descrever(violacoes));
    }

    private static void assertViolacaoEm(SupplierCreateDTO dto, String campo, String id) {
        Set<ConstraintViolation<SupplierCreateDTO>> violacoes = validator.validate(dto);
        Set<String> campos = violacoes.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
        assertFalse(campos.isEmpty(), id + ": esperava violação em '" + campo + "', mas não houve nenhuma");
        assertTrue(campos.contains(campo), id + ": esperava violação em '" + campo + "', veio em " + campos);
    }

    private static String descrever(Set<ConstraintViolation<SupplierCreateDTO>> violacoes) {
        return violacoes.stream()
                .map(v -> v.getPropertyPath() + " (" + v.getMessage() + ")")
                .collect(Collectors.joining(", "));
    }
}
