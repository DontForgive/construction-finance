package br.com.galsystem.construction.finance.exception;

import br.com.galsystem.construction.finance.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerPhoneTest {

    @Test
    @DisplayName("CT-1.1.17 telefone inválido gera HTTP 400 com envelope e a mensagem aprovada, sem fallback 500")
    void ct_1_1_17_telefone_invalido_gera_400_com_mensagem_aprovada() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<Response<Void>> resposta = handler.handleInvalidPhone(new InvalidPhoneException());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().getStatus()).isEqualTo(400);
        assertThat(resposta.getBody().getMessage())
                .isEqualTo("Informe um telefone válido com DDD (10 ou 11 dígitos).");
    }
}
