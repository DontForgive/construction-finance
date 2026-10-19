package br.com.galsystem.construction.finance.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Testes unitários da migração V1__add_address_to_supplier.sql (HU-1.1).
 * Validam o conteúdo do arquivo SQL. Os testes funcionais em PostgreSQL
 * (CT-1.1.4 a CT-1.1.12) não fazem parte desta classe.
 */
class SupplierAddressMigrationTest {

    private static final String ARQUIVO = "V1__add_address_to_supplier.sql";
    private static final String CAMINHO = "/db/migration/" + ARQUIVO;

    private static final Pattern ADD_COLUMN = Pattern.compile(
            "ALTER\\s+TABLE\\s+supplier\\s+ADD\\s+COLUMN\\s+(\\w+)\\s+([^;]+);",
            Pattern.CASE_INSENSITIVE);

    /** CT-1.1.1: sete colunas com os nomes esperados, na ordem da spec. */
    @Test
    void ct111_arquivoDeMigracaoContemSeteAddColumnComNomesEsperados() {
        String sql = lerMigracao();

        List<String> colunas = colunasAdicionadas(sql);

        assertEquals(List.of("street", "number", "neighborhood", "city", "state", "zip_code", "complement"),
                colunas, "CT-1.1.1: colunas adicionadas à tabela supplier divergem da spec");
    }

    /**
     * CT-1.1.1 (nome do arquivo): lista os arquivos reais de db/migration no classpath
     * e confere que o arquivo da HU-1.1 existe e que todos seguem o padrão V{n}__nome.sql.
     */
    @Test
    void ct111_nomeDoArquivoSegueOPadraoDeVersionamento() throws Exception {
        URL pasta = getClass().getResource("/db/migration");
        assertNotNull(pasta, "CT-1.1.1: pasta db/migration não encontrada no classpath");

        List<String> nomes;
        try (Stream<Path> arquivos = Files.list(Paths.get(pasta.toURI()))) {
            nomes = arquivos.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".sql"))
                    .toList();
        }

        assertTrue(nomes.contains(ARQUIVO),
                "CT-1.1.1: " + ARQUIVO + " ausente de db/migration. Encontrados: " + nomes);
        for (String nome : nomes) {
            assertTrue(nome.matches("V\\d+__\\w+\\.sql"),
                    "CT-1.1.1: nome fora do padrão V{n}__nome.sql: " + nome);
        }
        assertTrue(ARQUIVO.matches("V\\d+__add_address_to_supplier\\.sql"),
                "CT-1.1.1: nome do arquivo da HU-1.1 fora do padrão V{n}__add_address_to_supplier.sql");
    }

    /** CT-1.1.2: tipos e tamanhos declarados, na ordem da spec. */
    @Test
    void ct112_tiposETamanhosSaoOsDaSpec() {
        List<String> tipos = tiposAdicionados(lerMigracao());

        assertEquals(List.of(
                "varchar(255)",
                "varchar(20)",
                "varchar(100)",
                "varchar(100)",
                "varchar(2)",
                "varchar(9)",
                "varchar(100)"), tipos,
                "CT-1.1.2: tipos ou tamanhos divergem da spec (ordem: street, number, neighborhood, city, state, zip_code, complement)");
    }

    /** CT-1.1.3: sem DEFAULT, NOT NULL, UPDATE ou DELETE. */
    @Test
    void ct113_sqlNaoAlteraDadosExistentes() {
        String sql = lerMigracao().toUpperCase(Locale.ROOT);

        assertFalse(sql.contains("DEFAULT"), "CT-1.1.3: SQL contém DEFAULT");
        assertFalse(sql.contains("NOT NULL"), "CT-1.1.3: SQL contém NOT NULL");
        assertFalse(sql.contains("UPDATE"), "CT-1.1.3: SQL contém UPDATE");
        assertFalse(sql.contains("DELETE"), "CT-1.1.3: SQL contém DELETE");
    }

    /** CT-1.1.3 (complemento): apenas ALTER TABLE supplier ADD COLUMN, nada mais. */
    @Test
    void ct113_sqlContemApenasAddColumnNaTabelaSupplier() {
        String semComentarios = lerMigracao().replaceAll("(?m)--.*$", "");
        String restante = ADD_COLUMN.matcher(semComentarios).replaceAll("").trim();

        assertEquals("", restante, "CT-1.1.3: há instruções além dos ADD COLUMN em supplier");
    }

    /** CT-1.1.13 (parcial, nomes em snake_case): colunas em minúsculas com underscore. */
    @Test
    void ct1113_nomesDeColunasEmSnakeCase() {
        for (String coluna : colunasAdicionadas(lerMigracao())) {
            assertTrue(coluna.matches("[a-z]+(_[a-z]+)*"),
                    "CT-1.1.13: coluna fora do snake_case: " + coluna);
        }
    }

    private static String lerMigracao() {
        try (InputStream in = SupplierAddressMigrationTest.class.getResourceAsStream(CAMINHO)) {
            assertNotNull(in, "Migração não encontrada no classpath: " + CAMINHO);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a migração " + CAMINHO, e);
        }
    }

    private static List<String> colunasAdicionadas(String sql) {
        List<String> nomes = new ArrayList<>();
        Matcher m = ADD_COLUMN.matcher(sql);
        while (m.find()) {
            nomes.add(m.group(1));
        }
        return nomes;
    }

    private static List<String> tiposAdicionados(String sql) {
        List<String> tipos = new ArrayList<>();
        Matcher m = ADD_COLUMN.matcher(sql);
        while (m.find()) {
            tipos.add(m.group(2).trim().toLowerCase(Locale.ROOT));
        }
        return tipos;
    }
}
