package br.com.galsystem.construction.finance.controller.diagnostics;

import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Endpoint SOMENTE para gerar erros de propósito, usado para testar o agente
 * de troubleshooting (fingerprint -> memória -> investigação -> notificação).
 *
 * Cada rota dispara um tipo diferente de erro, todos passando pelos handlers
 * reais de {@code ApiExceptionHandler} / {@code GlobalExceptionHandler} — ou
 * seja, o mesmo caminho que um erro real da API percorreria em produção.
 *
 * Controlado por app.diagnostics.enabled (application.properties).
 * NUNCA habilitar em produção: desligue a propriedade (ou remova este
 * arquivo) antes de publicar fora do ambiente local.
 */
@RestController
@RequestMapping("/diagnostics/errors")
@ConditionalOnProperty(name = "app.diagnostics.enabled", havingValue = "true", matchIfMissing = false)
public class DiagnosticsErrorController {

    private static final Map<String, String> TIPOS = new LinkedHashMap<>();
    static {
        TIPOS.put("not-found", "GET /diagnostics/errors/not-found -> 404 (ResourceNotFoundException)");
        TIPOS.put("conflict", "GET /diagnostics/errors/conflict -> 409 (ConflictException)");
        TIPOS.put("validation", "POST /diagnostics/errors/validation (body {} ou campo em branco) -> 400 (MethodArgumentNotValidException)");
        TIPOS.put("type-mismatch", "GET /diagnostics/errors/type-mismatch/{id} com id não numérico (ex.: 'abc') -> 400 (MethodArgumentTypeMismatchException)");
        TIPOS.put("data-integrity", "GET /diagnostics/errors/data-integrity -> 409 (DataIntegrityViolationException)");
        TIPOS.put("access-denied", "GET /diagnostics/errors/access-denied -> 403 (AccessDeniedException)");
        TIPOS.put("null-pointer", "GET /diagnostics/errors/null-pointer -> 500 (NullPointerException)");
        TIPOS.put("generic", "GET /diagnostics/errors/generic -> 500 (RuntimeException genérica)");
        TIPOS.put("random", "GET /diagnostics/errors/random -> dispara um dos tipos acima aleatoriamente");
    }

    @GetMapping
    public ResponseEntity<Response<Map<String, String>>> listar() {
        final Response<Map<String, String>> body = new Response<>();
        body.setStatus(200);
        body.setMessage("Tipos de erro simulados disponíveis");
        body.setData(TIPOS);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/not-found")
    public void notFound() {
        throw new ResourceNotFoundException("Recurso simulado não encontrado (diagnostics)");
    }

    @GetMapping("/conflict")
    public void conflict() {
        throw new ConflictException("Conflito simulado (diagnostics)");
    }

    @PostMapping("/validation")
    public void validation(@Valid @RequestBody final ValidationPayload payload) {
        // Se o payload vier válido, não há erro para simular.
    }

    @GetMapping("/type-mismatch/{id}")
    public void typeMismatch(@PathVariable final Long id) {
        // O erro é disparado pelo próprio Spring ao converter o path variable
        // para Long, antes mesmo do método ser executado (ex.: id=abc).
    }

    @GetMapping("/data-integrity")
    public void dataIntegrity() {
        throw new DataIntegrityViolationException("Violação de integridade simulada (diagnostics): unique constraint");
    }

    @GetMapping("/access-denied")
    public void accessDenied() {
        throw new AccessDeniedException("Acesso negado simulado (diagnostics)");
    }

    @GetMapping("/null-pointer")
    public void nullPointer() {
        final String valor = null;
        valor.length(); // NullPointerException proposital
    }

    @GetMapping("/generic")
    public void generic() {
        throw new RuntimeException("Erro genérico simulado (diagnostics)");
    }

    @GetMapping("/random")
    public void random() {
        final String[] tipos = {"not-found", "conflict", "data-integrity", "access-denied", "null-pointer", "generic"};
        final String escolhido = tipos[ThreadLocalRandom.current().nextInt(tipos.length)];
        switch (escolhido) {
            case "not-found" -> notFound();
            case "conflict" -> conflict();
            case "data-integrity" -> dataIntegrity();
            case "access-denied" -> accessDenied();
            case "null-pointer" -> nullPointer();
            default -> generic();
        }
    }

    @Data
    public static class ValidationPayload {
        @NotBlank(message = "campo obrigatório para simular erro de validação")
        private String campoObrigatorio;
    }
}
