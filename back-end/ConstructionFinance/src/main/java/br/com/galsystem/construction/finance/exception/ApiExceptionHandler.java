package br.com.galsystem.construction.finance.exception;

import br.com.galsystem.construction.finance.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice(basePackages = "br.com.galsystem.construction.finance.controller")
public class ApiExceptionHandler {

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
        // TODO: log detalhado aqui com um correlationId
        final String uri = req.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui") || uri.startsWith("/swagger-resources")) {
            throw ex; // deixa o springdoc tratar
        }
        final List<String> erros = new ArrayList<>();
        erros.add(ex.getMessage());

        final Response<Void> body = new
                Response<>();
        
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
}
