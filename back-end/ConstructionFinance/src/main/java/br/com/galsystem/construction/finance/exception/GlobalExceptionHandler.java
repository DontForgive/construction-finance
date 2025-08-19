package br.com.galsystem.construction.finance.exception;


import br.com.galsystem.construction.finance.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura quando a rota não existe
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Response<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        Response<Void> body = new Response<>();
        body.setStatus(404);
        body.setMessage("Rota não encontrada: " + ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
