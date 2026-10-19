package br.com.galsystem.construction.finance.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários da migração Flyway da HU-1.1 (endereço na tabela supplier).
 * Validam o arquivo SQL, sem banco de dados.
 * Casos: CT-1.1.1, CT-1.1.2, CT-1.1.3 e CT-1.1.13 (parte de nome/versão).
 */
class SupplierAddressMigrationTest {

    private static final Path MIGRATION_DIR = Path.of("src/main/resources/db/migration");
    private static final Pattern MIGRATION_FILE = Pattern.compile("V(\\d+)__add_address_to_supplier\\.sql");
    private static final Pattern ANY_MIGRATION_FILE = Pattern.compile("V(\\d+)__.+\\.sql");
    private static final Pattern ADD_COLUMN =
            Pattern.compile("(?i)ALTER\\s+TABLE\\s+supplier\\s+ADD\\s+COLUMN\\s+(\\w+)\\s+(\\S+?)\\s*;");

    private static final List<String> EXPECTED_COLUMNS = List.of(
            "street", "number", "neighborhood", "city", "state", "zip_code", "complement");
    private static final List<String> EXPECTED_TYPES = List.of(
            "varchar(255)", "varchar(20)", "varchar(100)", "varchar(100)",
            "varchar(2)", "varchar(9)", "varchar(100)");

    private static String sqlSemComentarios;

    @BeforeAll
    static void carregarMigracao() throws IOException {
        assertThat(Files.isDirectory(MIGRATION_DIR))
                .as("Pasta de migrações Flyway %s deve existir", MIGRATION_DIR)
                .isTrue();
        Path arquivo;
        try (Stream<Path> arquivos = Files.list(MIGRATION_DIR)) {
            arquivo = arquivos
                    .filter(p -> MIGRATION_FILE.matcher(p.getFileName().toString()).matches())
                    .findFirst()
                    .orElse(null);
        }
        assertThat(arquivo)
                .as("Arquivo V{n}__add_address_to_supplier.sql deve existir em %s", MIGRATION_DIR)
                .isNotNull();
        String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        sqlSemComentarios = conteudo.replaceAll("(?m)--.*$", "");
    }

    private static List<String[]> colunasAdicionadas() {
        List<String[]> colunas = new ArrayList<>();
        Matcher m = ADD_COLUMN.matcher(sqlSemComentarios);
        while (m.find()) {
            colunas.add(new String[] {m.group(1), m.group(2)});
        }
        return colunas;
    }

    @Test
    void ct_1_1_1_migracaoAdicionaSeteColunasComNomesEsperados() {
        // CT-1.1.1 (CA1): sete ALTER TABLE supplier ADD COLUMN com os nomes da spec
        List<String[]> colunas = colunasAdicionadas();
        assertThat(colunas).as("CT-1.1.1: quantidade de ADD COLUMN em supplier").hasSize(7);
        assertThat(colunas.stream().map(c -> c[0]).toList())
                .as("CT-1.1.1: nomes das colunas")
                .containsExactlyInAnyOrderElementsOf(EXPECTED_COLUMNS);
    }

    @Test
    void ct_1_1_2_tiposSaoVarcharNosTamanhosDaSpec() {
        // CT-1.1.2 (CA1): tipos e tamanhos na ordem da spec; qualquer divergência reprova
        List<String[]> colunas = colunasAdicionadas();
        assertThat(colunas.stream().map(c -> c[1].toLowerCase()).toList())
                .as("CT-1.1.2: tipos na ordem street, number, neighborhood, city, state, zip_code, complement")
                .containsExactlyElementsOf(EXPECTED_TYPES);
    }

    @Test
    void ct_1_1_3_migracaoNaoTemDefaultNotNullNemAlteraDados() {
        // CT-1.1.3 (CA2): sem DEFAULT, sem NOT NULL e sem UPDATE/DELETE
        assertThat(sqlSemComentarios)
                .as("CT-1.1.3: SQL não pode conter DEFAULT")
                .doesNotContainPattern("(?i)\\bDEFAULT\\b");
        assertThat(sqlSemComentarios)
                .as("CT-1.1.3: SQL não pode conter NOT NULL")
                .doesNotContainPattern("(?i)\\bNOT\\s+NULL\\b");
        assertThat(sqlSemComentarios)
                .as("CT-1.1.3: SQL não pode conter UPDATE nem DELETE")
                .doesNotContainPattern("(?i)\\b(UPDATE|DELETE)\\b");
    }

    @Test
    void ct_1_1_13_versaoNaoColideEColunasEmSnakeCase() throws IOException {
        // CT-1.1.13 (CA4), parte verificável sem banco: nome V{n}__... e snake_case.
        // A comparação com versões já aplicadas no banco não é feita aqui.
        List<Path> arquivos;
        try (Stream<Path> s = Files.list(MIGRATION_DIR)) {
            arquivos = s.filter(p -> ANY_MIGRATION_FILE.matcher(p.getFileName().toString()).matches()).toList();
        }
        List<String> versoes = arquivos.stream()
                .map(p -> ANY_MIGRATION_FILE.matcher(p.getFileName().toString()))
                .peek(Matcher::matches)
                .map(m -> m.group(1))
                .toList();
        assertThat(versoes)
                .as("CT-1.1.13: nenhuma versão de migração repetida na pasta")
                .doesNotHaveDuplicates();
        colunasAdicionadas().forEach(c ->
                assertThat(c[0])
                        .as("CT-1.1.13: coluna %s deve estar em snake_case", c[0])
                        .matches("[a-z]+(_[a-z]+)*"));
    }
}
