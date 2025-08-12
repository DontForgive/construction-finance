package br.com.galsystem.construction.finance.exception;

import br.com.galsystem.construction.finance.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Response<Void> resp = new Response<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                resp.getErros().add(err.getField() + ": " + err.getDefaultMessage()));
        resp.setStatus(400);
        resp.setMessage("Erros de validação");
        return ResponseEntity.badRequest().body(resp);
    }
}

