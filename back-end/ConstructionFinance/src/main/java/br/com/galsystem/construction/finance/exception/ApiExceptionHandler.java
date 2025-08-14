package br.com.galsystem.construction.finance.exception;

import br.com.galsystem.construction.finance.response.Response;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ ResourceNotFoundException.class, NotFoundException.class })
    public ResponseEntity<Response<Void>> handleNotFound(RuntimeException ex) {
        Response<Void> body = new Response<>();
        body.setStatus(404);
        body.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<Response<Void>> handleNotFound(ChangeSetPersister.NotFoundException ex) {
        Response<Void> body = new Response<>();
        body.setStatus(404);
        body.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<Void>> handleConflict(ConflictException ex) {
        Response<Void> body = new Response<>();
        body.setStatus(409);
        body.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Response<Void> body = new Response<>();
        body.setStatus(400);
        body.setMessage("Erro de validação");
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> body.getErros().add(err.getField() + ": " + err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGeneric(Exception ex) {
        Response<Void> body = new Response<>();
        body.setStatus(500);
        body.setMessage("Erro interno do servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
