package br.com.galsystem.construction.finance.exception;

import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.troubleshooting.ErrorDocument;
import br.com.galsystem.construction.finance.troubleshooting.ErrorFingerprintGenerator;
import br.com.galsystem.construction.finance.troubleshooting.ErrorIndexingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice(basePackages = "br.com.galsystem.construction.finance.controller")
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private ErrorFingerprintGenerator fingerprintGenerator;
    private ErrorIndexingService indexingService;

    @Autowired(required = false)
    public void setFingerprintGenerator(ErrorFingerprintGenerator fingerprintGenerator) {
        this.fingerprintGenerator = fingerprintGenerator;
    }

    @Autowired(required = false)
    public void setIndexingService(ErrorIndexingService indexingService) {
        this.indexingService = indexingService;
    }

    // 404 - not found (suas exceções)
    @ExceptionHandler({ResourceNotFoundException.class, NotFoundException.class})
    public ResponseEntity<Response<Void>> handleNotFound(final RuntimeException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(404);
        body.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 404 - ChangeSetPersister (às vezes JPA/Spring usa essa internamente)
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<Response<Void>> handleNotFound(final ChangeSetPersister.NotFoundException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(404);
        body.setMessage("Recurso não encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 409 - conflito de negócio
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<Void>> handleConflict(final ConflictException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(409);
        body.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 400 - validação @Valid em @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidation(final MethodArgumentNotValidException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(400);
        body.setMessage("Erro de validação");
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> body.getErros().add(err.getField() + ": " + err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - JSON malformado ou tipo incompatível no corpo
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleUnreadable(final HttpMessageNotReadableException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(400);
        body.setMessage("Erro na leitura do corpo da requisição: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - tipo errado em @PathVariable/@RequestParam (ex.: id=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<Void>> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(400);
        body.setMessage("Parâmetro inválido: '" + ex.getName() + "' deve ser do tipo " +
                (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "esperado"));
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - violações de constraints de bean validation fora do @RequestBody
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolation(final ConstraintViolationException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(400);
        body.setMessage("Erro de validação");
        ex.getConstraintViolations().forEach(v ->
                body.getErros().add(v.getPropertyPath() + ": " + v.getMessage()));
        return ResponseEntity.badRequest().body(body);
    }

    // 409 (ou 400) - violação de integridade do banco (FK/unique/NOT NULL)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response<Void>> handleDataIntegrity(final DataIntegrityViolationException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(409);
        body.setMessage("Violação de integridade dos dados");
        // opcional: detalhe técnico seguro
        final String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        body.getErros().add(msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 403 - acesso negado (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Void>> handleAccessDenied(final AccessDeniedException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(403);
        body.setMessage("Acesso negado");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 500 - fallback genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGeneric(final Exception ex, final HttpServletRequest req) throws Exception {
        final String uri = req.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui") || uri.startsWith("/swagger-resources")) {
            throw ex;
        }

        logAndIndexError(ex, uri, req.getMethod(), 500);

        final List<String> erros = new ArrayList<>();
        erros.add(ex.getMessage());

        final Response<Void> body = new Response<>();
        body.setStatus(500);
        body.setMessage("Erro interno do servidor");
        body.setErros(erros);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleStaticNotFound(final NoResourceFoundException ex) {
        final Response<Void> body = new Response<>();
        body.setStatus(404);
        body.setMessage("Recurso não encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    private void logAndIndexError(Exception ex, String path, String method, int status) {
        try {
            String stacktrace = getStacktrace(ex, 10);
            log.error("Error captured: exceptionClass={}, status={}, path={}, method={}, message={}, stacktrace={}",
                    ex.getClass().getName(), status, path, method, ex.getMessage(), stacktrace);

            if (indexingService != null && fingerprintGenerator != null) {
                String fingerprint = fingerprintGenerator.generate(
                        ex.getClass().getName(),
                        path,
                        ex.getMessage() != null ? ex.getMessage() : ""
                );

                ErrorDocument errorDoc = ErrorDocument.builder()
                        .timestamp(Instant.now())
                        .correlationId(org.slf4j.MDC.get("correlationId"))
                        .fingerprint(fingerprint)
                        .exceptionClass(ex.getClass().getName())
                        .message(ex.getMessage())
                        .path(path)
                        .method(method)
                        .status(status)
                        .stacktrace(stacktrace)
                        .build();

                indexingService.indexError(errorDoc);
            }
        } catch (Exception e) {
            log.error("Erro ao logar/indexar documento de erro", e);
        }
    }

    private String getStacktrace(Exception ex, int maxLines) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");

        int lines = Math.min(maxLines, stackTrace.length);
        for (int i = 0; i < lines; i++) {
            sb.append("\tat ").append(stackTrace[i]).append("\n");
        }

        if (stackTrace.length > maxLines) {
            sb.append("\t... and ").append(stackTrace.length - maxLines).append(" more lines\n");
        }

        return sb.toString();
    }
}
