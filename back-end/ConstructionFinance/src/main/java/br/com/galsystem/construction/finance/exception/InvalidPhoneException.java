package br.com.galsystem.construction.finance.exception;

/**
 * Telefone do fornecedor fora do formato aceito. A mensagem não inclui o valor enviado.
 */
public class InvalidPhoneException extends RuntimeException {

    public static final String MENSAGEM = "Informe um telefone válido com DDD (10 ou 11 dígitos).";

    public InvalidPhoneException() {
        super(MENSAGEM);
    }
}
