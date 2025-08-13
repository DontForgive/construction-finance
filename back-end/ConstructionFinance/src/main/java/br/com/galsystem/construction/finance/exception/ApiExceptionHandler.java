package br.com.galsystem.construction.finance.exception;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ProblemDetail;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@ComponentScan
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final ZoneId ZONE_BR = ZoneId.of("America/Sao_Paulo");

    @ExceptionHandler({
            EntityNotFoundException.class,           // jakarta.persistence.*
            NoSuchElementException.class,            // ex.: Optional.get() sem valor
            EmptyResultDataAccessException.class     // ex.: deleteById(id inexistente)
    })
    public ResponseEntity<ProblemDetail> handleNotFound(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not Found");
        pd.setType(URI.create("https://docs.sua-api.dev/errors/not-found"));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", OffsetDateTime.now(ZONE_BR));
        pd.setProperty("code", "RESOURCE_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    // Fallback pra garantir que você veja o novo formato se algo escapar
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflict");
        pd.setDetail(ex.getMessage()); // 👈 SUA mensagem vai aqui
        pd.setType(URI.create("https://docs.sua-api.dev/errors/payer-name-conflict"));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")));
        pd.setProperty("code", "PAYER_NAME_CONFLICT");
        // opcional: ajuda o front a apontar o campo
        pd.setProperty("fields", List.of(Map.of("field", "name", "message", ex.getMessage())));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }
}
